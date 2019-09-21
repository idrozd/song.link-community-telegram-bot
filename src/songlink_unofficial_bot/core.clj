(ns songlink-unofficial-bot.core
  (:gen-class)
  (:require [clojure.core.async :refer [<!!]]
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
            [songlink-unofficial-bot.songlink-api :as sl]
            ))

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
(def songlinkable-url-pattern #"open.spotify.com|deezer.com/track|music.amazon.com|play.google.com/music|music.apple.com|music.yandex|music.youtube.com/watch")
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

(declare keyboard keyboard-redirect-blindly redirect-link)


(defn platform-link [text]
  (let [text (or text "")
        link (or (re-find url-pattern text) "")]
    (when
      (and (re-find songlinkable-url-pattern link) (not (re-find #"song.link" link)))
      link)))
(defn text-to-songlink [text]
  (when-let [link (platform-link text)] (str "https://song.link/" link)))


(h/defhandler songbot
  (h/command-fn "start"
    (fn [{{id :id :as chat} :chat}]
      (t/send-text token id about)))

  (h/command-fn "help"
    (fn [{{id :id :as chat} :chat}]
      (t/send-text token id about)))

  (h/message-fn
   (fn [{text :text {id :id} :chat}]
     (when-let* [songlink (text-to-songlink text)
                 direct-link-platforms ["spotify"  "appleMusic" "youtube" "youtubeMusic" "google" "amazonMusic" "yandex" "itunes"]
                 kbd (-> text sl/fetch-links sl/platforms-to-urls (select-keys direct-link-platforms) keyboard)]
       (t/send-text token id {:disable_notification true :reply_markup {:inline_keyboard kbd}} songlink))))

  (h/inline-fn
   (fn [{id :id term :query offset :offset}]
     ;; (println "Received inline: " id term offset)
     (t/answer-inline token id {} (->>
                                   {:term term :entity "song"}
                                   apple/search
                                   ;; (mapcat (juxt apple/song-result->inline-article apple/song-result->audio))))))
                                   (map apple/song-result->inline-article))))))


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
      :thumb_url "https://is3-ssl.mzstatic.com/image/thumb/Music4/v4/ab/d9/14/abd914fe-aba8-0ad9-176b-6cd7ecc5dfed/source/60x60bb.jpg"}]

    )

  )




(comment (when-let* [text "https://play.google.com/music/m/Bpke6amq6qoeewwhm5nsnkvswb4?t=All_My_Heroes_Are_Cornballs_-_JPEGMAFIA"
             songlink (text-to-songlink text)
             direct-link-platforms ["spotify"  "appleMusic" "youtube" "youtubeMusic" "google" "amazonMusic" "yandex" "itunes"]
             kbd (-> text sl/fetch-links sl/platforms-to-urls (select-keys direct-link-platforms) keyboard)]
   (t/send-text token (env :test-chat-id) {:disable_notification true :reply_markup {:inline_keyboard kbd}} songlink)))

(comment

  (songbot {:update_id 1005009999
             :message {;;:date 1441645532,
                       :chat {:last_name "Test Lastname", :id (env :test-chat-id), :first_name "Test", :username "Test"},
                       :message_id 1365,
                       :from {:last_name "Test Lastname", :id 1111111, :first_name "Test", :username "Test"},
                       :text "https://play.google.com/music/m/Bzfnl3fgkfta3eq5zouiax4n7mq?t=Aquamarine_-_Ash_Walker"}})

  (->>
   {:term "hello" :entity "song,album"}
   apple/search
   ;; (mapcat (juxt apple/song-result->inline-article apple/song-result->audio))))))
   (mapcat (juxt apple/song-result->inline-article)))

  (songbot inline-update-ex)

  (def inline-update-example
    {:update_id 646911422,
     :inline_query
     {:id "432549315453598698",
      :from {:id "10000xxxx", :is_bot false, :first_name "Jiayu", :username "jiayu", :language_code "en-US"},
      :query "Queen - Innuendo",
      :offset ""}})

  )

(comment
  ;;  cider workbench
  (do
    (def token (-> "profiles.clj"
                   slurp
                   read-string
                   :dev-local
                   :env
                   :telegram-token))
    (try (clj-http.client/get (str morse.api/base-url token "/deleteWebhook")) (catch Exception e (println e)))
    (def poller (p/start token songbot {:timeout 25})))

  (clj-http.client/get (str morse.api/base-url token "/getUpdates"))


  ;;RESTART
  (do (clojure.core.async/close! poller)
      ;; (use songlink-unofficial-bot.core :reload-all)
      (def token (-> "profiles.clj" slurp read-string :dev-local :env :telegram-token))
      (def poller (p/start token songbot {:timeout 25})))


  (do
    (require '[clojure.tools.namespace.repl :refer [refresh]])
    (refresh))

)
