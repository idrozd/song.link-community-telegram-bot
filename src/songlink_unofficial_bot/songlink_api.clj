(ns songlink-unofficial-bot.songlink-api
  (:require [cheshire.core :as json]
            [clj-http.util :refer [url-encode] ]))



(defn fetch-links [songlinkable]
  (->> songlinkable
   (url-encode)
   (str "https://api.song.link/v1-alpha.1/links?url=")
   slurp
   json/parse-string))


(defn platforms-to-urls [links-response]
  (->> (get links-response "linksByPlatform")
      (map (fn [[platform {url "url"}]] {platform url}))
      (into {})))


(defn meta
  ([links] (meta links "appleMusic"))
  ([links platform]
   (let [unique-id (get-in links ["linksByPlatform" platform "entityUniqueId"])
         entity-id (clojure.string/split unique-id #"::" )]
     (merge {:id entity-id} (clojure.walk/keywordize-keys (get-in links ["entitiesByUniqueId" unique-id]))))))



;; CIDER workbench

(comment

  (get-in rapsody ["linksByPlatform"])

  ;; result shortened
  (def rapsody {"entityUniqueId" "ITUNES_ALBUM::722399370",
                "userCountry" "US",
                "pageUrl" "https://song.link/album/ru/i/722399370",
                "entitiesByUniqueId"
                {"ITUNES_ALBUM::722399370"
                 {"artistName" "Jazzwerkstatt Bern",
                  "id" "722399370",
                  "thumbnailHeight" 512,
                  "thumbnailWidth" 512,
                  "thumbnailUrl" "https://is3-ssl.mzstatic.com/image/thumb/Music4/v4/ab/d9/14/abd914fe-aba8-0ad9-176b-6cd7ecc5dfed/source/512x512bb.jpg",
                  "apiProvider" "itunes",
                  "title" "Live 2012",
                  "platforms" ["appleMusic" "itunes"],
                  "type" "album"},
                 "SPOTIFY_ALBUM::6c8cFNdrK64laCDjFe9Q0h"
                 {"artistName" "Jazzwerkstatt Bern",
                  "id" "alb.131557069",
                  "thumbnailHeight" 385,
                  "thumbnailWidth" 385,
                  "thumbnailUrl" "https://direct.rhapsody.com/imageserver/images/alb.131557069/385x385.jpeg",
                  "apiProvider" "napster",
                  "title" "Live 2012",
                  "platforms" ["napster"],
                  "type" "album"}},
                "linksByPlatform"
                {"google"
                 {"url" "https://play.google.com/music/m/B4ulimbglqngmskfx6fsf2mlogi?signup_if_needed=1",
                  "entityUniqueId" "GOOGLE_ALBUM::B4ulimbglqngmskfx6fsf2mlogi"},
                 "appleMusic"
                 {"url" "https://geo.music.apple.com/ru/album/_/722399370?mt=1&app=music&at=1000lHKX",
                  "nativeAppUriMobile" "music://itunes.apple.com/ru/album/_/722399370?mt=1&app=music&at=1000lHKX",
                  "nativeAppUriDesktop" "itmss://itunes.apple.com/ru/album/_/722399370?mt=1&app=music&at=1000lHKX",
                  "entityUniqueId" "ITUNES_ALBUM::722399370"},
                 }})

  )
