(ns songlink-unofficial-bot.core
  (:gen-class)
  (:require [clj-http.client :as http]
            [clojure.core.async :as async :refer [<!!]]
            [clojure.string :as str]
            [compojure.core :as cmpj]
            [compojure.route :as route]
            [environ.core :refer [env]]
            [morse.api :as t]
            [morse.handlers :as h]
            [morse.polling :as p]
            [ring.adapter.jetty :as jtt]
            [ring.logger :as logger]
            [ring.middleware.json :refer [wrap-json-body]]
            [songlink-unofficial-bot.apple-music :as apple]
            [songlink-unofficial-bot.songlink-api :as sl]))

;; why isn't this in clojure.core?


(defmacro when-let*
  ([bindings & body]
   (if (seq bindings)
     `(when-let [~(first bindings) ~(second bindings)]
        (when-let* ~(drop 2 bindings) ~@body))
     `(do ~@body))))

(def about "This is unofficial song.link bot / weekend project.
I don't know any commands yet.
You can add me to your group chat, and I will respond with song.link  to \"Share this song\" links from Google play / iTunes / yandex.music.
No spying!
Also, can use me just like @gif - inline search, but for music. Start typing:
@songlbot swans - screen shot
and wait couple sec for suggestions.
Your phone app should remember me after first use and add me ro autocompleted usernames list.
")

(def token (env :telegram-token))
(def url-pattern #"(?i)(?:(?:https?|ftp)://)(?:\S+(?::\S*)?@)?(?:(?!(?:10|127)(?:\.\d{1,3}){3})(?!(?:169\.254|192\.168)(?:\.\d{1,3}){2})(?!172\.(?:1[6-9]|2\d|3[0-1])(?:\.\d{1,3}){2})(?:[1-9]\d?|1\d\d|2[01]\d|22[0-3])(?:\.(?:1?\d{1,2}|2[0-4]\d|25[0-5])){2}(?:\.(?:[1-9]\d?|1\d\d|2[0-4]\d|25[0-4]))|(?:(?:[a-z\u00a1-\uffff0-9]-*)*[a-z\u00a1-\uffff0-9]+)(?:\.(?:[a-z\u00a1-\uffff0-9]-*)*[a-z\u00a1-\uffff0-9]+)*(?:\.(?:[a-z\u00a1-\uffff]{2,}))\.?)(?::\d{2,5})?(?:[/?#]\S*)?")
(def songlinkable-url-pattern #"open.spotify.com|deezer.com/track|music.amazon.com|play.google.com/music|music.apple.com|music.yandex|music.youtube.com/watch|soundcloud.com")
(def supported-services
  ["spotify"
   "appleMusic"
   "youtube"
   "youtubeMusic"
   "google"
   "pandora"
   "deezer"
   "tidal"
   "amazonMusic"
   "soundcloud"
   "napster"
   "yandex"
   "spinrilla"
   "itunes"
   "googleStore"
   "amazonStore"])

(declare keyboard keyboard-redirect-blindly redirect-link telegram responses inline-results)

(defn platform-link [text]
  (let [text (or text "")
        link (or (re-find url-pattern text) "")]
    (when
     (and (re-find songlinkable-url-pattern link) (not (re-find #"song.link" link)))
      link)))

(defn text-to-songlink [text]
  (when-let [link (platform-link text)] (str "https://song.link/" link)))

(defn responses
  {:test #(do (assert (responses "https://music.apple.com/us/album/screen-shot/836834698?i=836834718&ign-mpt=uo%3D4")))}
  [text]
  (when-let* [songlinkable (platform-link text)
              songlink (text-to-songlink songlinkable)
              sldata (sl/fetch-links songlinkable)
              direct-link-platforms ["spotify"  "appleMusic" "youtube" "youtubeMusic" "google" "amazonMusic" "yandex" "itunes" "soundcloud"]
              kbd (-> sldata sl/platforms-to-urls (select-keys direct-link-platforms) keyboard)]

             [{:text songlink, :disable_notification true, :reply_markup {:inline_keyboard kbd}}]))

(h/defhandler songbot
  (h/command-fn "start"
    (fn [{{id :id :as chat} :chat}]
      (t/send-text token id about)))

  (h/command-fn "help"
    (fn [{{id :id :as chat} :chat}]
      (t/send-text token id about)))

  (h/message-fn
   (fn [{text :text {id :id} :chat}]
     (let [respond-closure #(doseq [r (responses text)] (telegram token id r))]
       (do
         (async/go (async/thread-call respond-closure))
         "OK"))))

  (h/inline-fn
   (fn [{id :id term :query offset :offset}]
     (t/answer-inline token id (inline-results term)))))

(defn keyboard-article [song]
  (let [direct-link-platforms ["spotify"  "appleMusic" "youtube" "youtubeMusic" "google" "amazonMusic" "yandex" "itunes"]
        kbd (-> (:trackViewUrl song) sl/fetch-links sl/platforms-to-urls (select-keys direct-link-platforms) keyboard)]
    (merge (apple/article song)
           {:reply_markup {:inline_keyboard kbd}})))

(defn inline-results [term]
  (let [songs (apple/search {:term term :entity "song"})
        best-matches (->> songs (take 2) (map keyboard-article))
        rest-matches (->> songs (drop 2) (map #(merge (apple/article %) {:reply_markup {:inline_keyboard (keyboard-redirect-blindly (:trackViewUrl %))}})))]
    (into best-matches rest-matches)))

(defn telegram
  "Sends generic message to the chat
   morse 0.4.3 does not have generic message "
  ([token chat-id message] (telegram token chat-id message "/sendMessage"))
  ([token chat-id message tg-method]
   (let [url  (str morse.api/base-url token tg-method)
         body (into {:chat_id chat-id} message)
         resp (http/post url {:content-type :json
                              :as           :json
                              :form-params  body})]
     (-> resp :body))))


;; Redirect URLs
;; We support "redirect URLs" which do not load the Songlink page, but instead redirect the user to a specific music streaming service, as defined by the to query param. A Songlink redirect URL can be composed easily with the following format:
;; https://song.link/redirect?url=encodedUrl&to=streamingService
;; More info about each query param:
;; url: the URL of the streaming entity, e.g. a spotify song URL. It's best if it's encoded, but it can be the plain URL, too. If using JavaScript or Node.js you can use encodeURIComponent()
;; to: the streaming service to which you'd like to redirect the user. Can be one of: spotify, appleMusic, youtube, youtubeMusic, google, pandora, deezer, tidal, amazonMusic, soundcloud, napster, yandex, spinrilla, itunes, googleStore or amazonStore. If the song is not available in the user's country on the specified streaming service, the Songlink page will load instead.
;; Here's an example redirect URL:
;; https://song.link/redirect?url=https%3A%2F%2Fitun.es%2Fus%2F5Gb0-%3Fi%3D1053825088&to=spotify
;; The "input" provided was the Apple Music track https://itun.es/us/5Gb0-?i=1053825088 and it redirects the user to said track on Spotify.
;; https://core.telegram.org/bots/api#inlinekeyboardbutton
(defn redirect-link [songlinkable service]
  (str "https://song.link/redirect?url=" (ring.util.codec/form-encode songlinkable) "&to=" service))

(defn keyboard-redirect-blindly
  ([songlinkable] (keyboard-redirect-blindly songlinkable ["spotify" "appleMusic" "google" "yandex" "itunes"]))
  ([songlinkable platforms]
   (->> platforms
        (map (fn [platform] {:text platform :url (redirect-link songlinkable platform)}))
        (partition-all 3))))

(defn keyboard [platform-url-pairs]
  (->> platform-url-pairs
       (map (fn [[platform url]] {:text platform :url url}))
       (partition 3)))

(cmpj/defroutes app-routes
  (cmpj/POST "/handler" {tg-update :body} (do #_(println (-> tg-update :message :text)) (songbot tg-update) "OK"))
  (cmpj/GET "/hello" {} "howdy")
  (route/not-found (do (println "N/F") "Not Found")))

(def app
  (->
   app-routes
   (wrap-json-body {:keywords? true})
   logger/wrap-with-logger))

(defn -main [port]
  (when (str/blank? token)
    (println "Please provde token in TELEGRAM_TOKEN environment variable!")
    (System/exit 1))

  (println "Starting the song.link unofficial bot")

  (if (= "long-polling" (env :mode))
    (do
      (try (clj-http.client/get (str morse.api/base-url token "/deleteWebhook")) (catch Exception e (println e)))
      (<!! (p/start token songbot {:timeout 25})))
    (try (t/set-webhook token (str (env :server-url) "/handler") ) (catch Exception e (println e))))

  ((let [port (Integer. (or port (System/getenv "PORT")))]
     (jtt/run-jetty app {:port port :host "0.0.0.0"}))))



;;  CIDER workbench

(comment

  (require '[clojure.tools.logging :refer [spy]])

  (for [r (responses text)] (telegram token (env :test-chat-id) r))

  (telegram token (env :test-chat-id) {:text "https://music.apple.com/us/album/screen-shot/836834698?i=836834718&ign-mpt=uo%3D4"})

  (t/send-text token (env :test-chat-id) {:text "https://music.apple.com/us/album/screen-shot/836834698?i=836834718&ign-mpt=uo%3D4"} "aaka")

  ;; (for [r (responses "https://music.apple.com/us/album/screen-shot/836834698?i=836834718&ign-mpt=uo%3D4")] (println (spy :info r)))


  ;; (t/send-text token (env :test-chat-id) ((comp apple/audio first apple/lookup) {:id 836834718}) "swans" )

  (telegram token (env :test-chat-id) aud "/sendAudio")

  ((comp apple/audio first apple/lookup) {:id 836834718})

  ;; (t/send-audio token (env :test-chat-id) {:title "ahah" :performer "huhu"} (clojure.java.io/file "/Users/atitov/Downloads/DESTINYS CHILD - Lose My Breath (Four Tet Remix).mp3"))

  (def aud {:id "song-preview-836834718",
    ;; :audio "https://audio-ssl.itunes.apple.com/itunes-assets/AudioPreview71/v4/2c/b5/44/2cb54444-5a1d-fffd-38eb-3e0eb3f7a686/mzaf_4387625555806532278.plus.aac.p.m4a",
    ;; :audio "https://p.scdn.co/mp3-preview/3eb16018c2a700240e9dfb8817b6f2d041f15eb1?cid=774b29d4f13844c495f206cafdad9c86",
            :audio "http://www.largesound.com/ashborytour/sound/brobob.mp3",
            :title "Screen Shot",
            :caption "Screen Shot",
            :performer "Swans"
            :thumb "https://is4-ssl.mzstatic.com/image/thumb/Music6/v4/c6/e6/ec/c6e6ecca-e3b5-6036-65c6-25e430db8802/source/30x30bb.jpg"}))

(comment
  (text-to-songlink  "Hey there https://play.google.com/music/m/Bzfnl3fgkfta3eq5zouiax4n7mq?t=Aquamarine_-_Ash_Walker")

  (text-to-songlink  "https://music.youtube.com/playlist?list=OLAK5uy_ljyHMKPSgmFBdsKR7pC6Ht52ngcPWlgQw") ;; won't work

  (text-to-songlink  "Hey there ")

  (def results-example
    [{:type "article"
      :id 100500
      :url "https://music.apple.com/ru/album/bohemian-rapsody-feat-ghost-town-trio-live/722399370?i=722399676&uo=4"
      :title "Some-album"
      :hide_url false
      :description "Bohemian Rapsody (feat. Ghost Town Trio)"
      :message_text "https://music.apple.com/ru/album/bohemian-rapsody-feat-ghost-town-trio-live/722399370?i=722399676&uo=4"
      :thumb_url "https://is3-ssl.mzstatic.com/image/thumb/Music4/v4/ab/d9/14/abd914fe-aba8-0ad9-176b-6cd7ecc5dfed/source/60x60bb.jpg"}]))

(comment

  ;; (songbot (merge msg {:text "/help"}))
  (songbot msg)

  (def msg {:update_id 1005009999
            :message {;;:date 1441645532,
                      :chat {:last_name "Test Lastname", :id (env :test-chat-id), :first_name "Test", :username "Test"},
                      :message_id 1365,
                      :from {:last_name "Test Lastname", :id 1111111, :first_name "Test", :username "Test"},
                      :text "https://play.google.com/music/m/T2rvt35lcfnvkis57qcx66hfpie?t=Do_It_Without_You"}})

  (def rrr (responses "https://play.google.com/music/m/Tj3dcgqkouvia6wd7bi36w2quwu?t=Rosie_-_DJ_Shadow"))

  (telegram token (env :test-chat-id) (first rrr))

  (do
    (t/send-text token (env :test-chat-id) "hello")
    (telegram token (env :test-chat-id) {:text "telegram for you sir"})
    (t/send-text token (env :test-chat-id) "hey"))

  (env :test-chat-id)

  (->>
   {:term "hello" :entity "song,album"}
   apple/search
   ;; (mapcat (juxt apple/inline-article apple/song-result->audio))))))
   (mapcat (juxt apple/inline-article)))

  (songbot inline-update-example)

  (def inline-update-example
    {:update_id 529813125,
     :inline_query {:id 155931020654442318,
                    :query "queen",
                    :offset nil
                    :from {:id 36305519,
                           :is_bot false,
                           :first_name "Andrei",
                           :username "le_gif_whisperer",
                           :language_code "en"}}}))

(comment
  ;;  cider workbench
  (do
    (def token (-> "profiles.clj"
                   slurp
                   read-string
                   :dev-local-polling
                   :env
                   :telegram-token))
    (try (clj-http.client/get (str morse.api/base-url token "/deleteWebhook")) (catch Exception e (println e)))

    (def poller (p/start token songbot {:timeout 25})))

  (clj-http.client/get (str morse.api/base-url token "/getUpdates"))

  ;;RESTART
  (do (async/close! poller)
      ;; (use songlink-unofficial-bot.core :reload-all)
      (def token (-> "profiles.clj" slurp read-string :dev-local-polling :env :telegram-token))
      (def poller (do (p/start token songbot {:timeout 25}) (println "poller restarted")))
      (println "REHELLO"))



  (do
    (require '[clojure.tools.namespace.repl :refer [refresh]])
    (refresh)))

(comment
  ;; TESTS
  (do
    (test #'responses))

  )

(comment

  (songbot msg)

  (let [id (env :test-chat-id)
        text  "https://play.google.com/music/m/T2rvt35lcfnvkis57qcx66hfpie?t=Do_It_Without_You"
        rs (responses text)
        respond-closure #(for [r rs] (telegram token id r))]
    (do
      (telegram token id {:text "hello"})
      ;; (telegram token id (first rs))
      ;; (println rs)
      (doseq [r rs] (telegram token id r))
      ;; (respond-closure)
      ;; (async/go (async/thread-call respond-closure))
      "OK"))

  (def msg {:update_id 1005009999
            :message {;;:date 1441645532,
                      ;; :chat {:last_name "Test Lastname", :id (env :test-chat-id), :first_name "Test", :username "Test"},
                      ;; :message_id 1365,
                      ;; :from {:last_name "Test Lastname", :id 1111111, :first_name "Test", :username "Test"},
                      :text "https://play.google.com/music/m/T2rvt35lcfnvkis57qcx66hfpie?t=Do_It_Without_You"}}))
