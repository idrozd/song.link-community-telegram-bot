(ns songlink-unofficial-bot.songlink-api
  (:require [cheshire.core :as json]
            [clj-http.util :refer [url-encode] ]
            [clojure.spec.alpha :as s]
            ))



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


(comment

  (-> "https://music.apple.com/ru/album/screen-shot/838349816?i=838349849&l=en&uo=4" fetch-links platforms-to-urls )
;; => {"youtube" "https://www.youtube.com/watch?v=6qDq9eGUmMI",
;;     "google" "https://play.google.com/music/m/Tnslfroplfeqtpcey6nksqpu5ce?signup_if_needed=1",
;;     "appleMusic" "https://geo.music.apple.com/ru/album/_/838349816?i=838349849&mt=1&app=music&at=1000lHKX",
;;     "deezer" "https://www.deezer.com/track/77782878",
;;     "tidal" "https://listen.tidal.com/track/28901964",
;;     "soundcloud" "https://soundcloud.com/muterecords/swans-screen-shot-1",
;;     "youtubeMusic" "https://music.youtube.com/watch?v=6qDq9eGUmMI",
;;     "amazonStore" "https://amazon.com/dp/B00JRIVXR0?tag=songlink0d-20",
;;     "spotify" "https://open.spotify.com/track/23cFxNXbwYcb6gfSOzL55H",
;;     "yandex" "https://music.yandex.ru/track/46161418",
;;     "pandora" "https://www.pandora.com/artist/swans/to-be-kind-explicit/screen-shot/TRwVZkvZZpctlVc",
;;     "amazonMusic" "https://music.amazon.com/albums/B00JRIVVNG?trackAsin=B00JRIVXR0&do=play",
;;     "googleStore" "https://play.google.com/store/music/album?id=Botkjiaxuzocozbj4ftssdz4swi&tid=song-Tnslfroplfeqtpcey6nksqpu5ce",
;;     "itunes" "https://geo.music.apple.com/ru/album/_/838349816?i=838349849&mt=1&app=itunes&at=1000lHKX",
;;     "napster" "http://napster.com/artist/art.5983/album/alb.143722465/track/tra.143722466"}

  )



;; (def rapsody {"entityUniqueId" "ITUNES_ALBUM::722399370",
;;     "userCountry" "US",
;;     "pageUrl" "https://song.link/album/ru/i/722399370",
;;     "entitiesByUniqueId"
;;     {"ITUNES_ALBUM::722399370"
;;      {"artistName" "Jazzwerkstatt Bern",
;;       "id" "722399370",
;;       "thumbnailHeight" 512,
;;       "thumbnailWidth" 512,
;;       "thumbnailUrl"
;;       "https://is3-ssl.mzstatic.com/image/thumb/Music4/v4/ab/d9/14/abd914fe-aba8-0ad9-176b-6cd7ecc5dfed/source/512x512bb.jpg",
;;       "apiProvider" "itunes",
;;       "title" "Live 2012",
;;       "platforms" ["appleMusic" "itunes"],
;;       "type" "album"},
;;      "SPOTIFY_ALBUM::6c8cFNdrK64laCDjFe9Q0h"
;;      {"artistName" "Jazzwerkstatt Bern",
;;       "id" "6c8cFNdrK64laCDjFe9Q0h",
;;       "thumbnailHeight" 640,
;;       "thumbnailWidth" 640,
;;       "thumbnailUrl" "https://i.scdn.co/image/cb798a0699d50beaabd84e2cac4d82c41193026f",
;;       "apiProvider" "spotify",
;;       "title" "Live 2012",
;;       "platforms" ["spotify"],
;;       "type" "album"},
;;      "GOOGLE_ALBUM::B4ulimbglqngmskfx6fsf2mlogi"
;;      {"artistName" "Jazzwerkstatt Bern",
;;       "id" "B4ulimbglqngmskfx6fsf2mlogi",
;;       "thumbnailHeight" 512,
;;       "thumbnailWidth" 512,
;;       "thumbnailUrl" "https://lh6.ggpht.com/BByGAMIopPYNvUAYutTv4TbSW3QkyOARSCunUHVaeFqKeRlUQKS11iaXR-4th4PtYjcudwRHBg",
;;       "apiProvider" "google",
;;       "title" "Live 2012",
;;       "platforms" ["google" "googleStore"],
;;       "type" "album"},
;;      "AMAZON_ALBUM::B00FN5I3X8"
;;      {"artistName" "Jazzwerkstatt Bern",
;;       "id" "B00FN5I3X8",
;;       "thumbnailHeight" 500,
;;       "thumbnailWidth" 500,
;;       "thumbnailUrl" "https://m.media-amazon.com/images/I/41cJg3X3jsL._AA500.jpg",
;;       "apiProvider" "amazon",
;;       "title" "Live 2012",
;;       "platforms" ["amazonMusic" "amazonStore"],
;;       "type" "album"},
;;      "TIDAL_ALBUM::22921361"
;;      {"artistName" "Jazzwerkstatt Bern",
;;       "id" "22921361",
;;       "thumbnailHeight" 640,
;;       "thumbnailWidth" 640,
;;       "thumbnailUrl" "https://resources.tidal.com/images/eb248b92/b76b/4734/a62e/59c6afc4e166/640x640.jpg",
;;       "apiProvider" "tidal",
;;       "title" "Live 2012",
;;       "platforms" ["tidal"],
;;       "type" "album"},
;;      "NAPSTER_ALBUM::alb.131557069"
;;      {"artistName" "Jazzwerkstatt Bern",
;;       "id" "alb.131557069",
;;       "thumbnailHeight" 385,
;;       "thumbnailWidth" 385,
;;       "thumbnailUrl" "https://direct.rhapsody.com/imageserver/images/alb.131557069/385x385.jpeg",
;;       "apiProvider" "napster",
;;       "title" "Live 2012",
;;       "platforms" ["napster"],
;;       "type" "album"}},
;;     "linksByPlatform"
;;     {"google"
;;      {"url" "https://play.google.com/music/m/B4ulimbglqngmskfx6fsf2mlogi?signup_if_needed=1",
;;       "entityUniqueId" "GOOGLE_ALBUM::B4ulimbglqngmskfx6fsf2mlogi"},
;;      "appleMusic"
;;      {"url" "https://geo.music.apple.com/ru/album/_/722399370?mt=1&app=music&at=1000lHKX",
;;       "nativeAppUriMobile" "music://itunes.apple.com/ru/album/_/722399370?mt=1&app=music&at=1000lHKX",
;;       "nativeAppUriDesktop" "itmss://itunes.apple.com/ru/album/_/722399370?mt=1&app=music&at=1000lHKX",
;;       "entityUniqueId" "ITUNES_ALBUM::722399370"},
;;      "tidal" {"url" "https://listen.tidal.com/album/22921361", "entityUniqueId" "TIDAL_ALBUM::22921361"},
;;      "amazonStore"
;;      {"url" "https://amazon.com/dp/B00FN5I3X8?tag=songlink0d-20", "entityUniqueId" "AMAZON_ALBUM::B00FN5I3X8"},
;;      "spotify"
;;      {"url" "https://open.spotify.com/album/6c8cFNdrK64laCDjFe9Q0h",
;;       "nativeAppUriDesktop" "spotify:album:6c8cFNdrK64laCDjFe9Q0h",
;;       "entityUniqueId" "SPOTIFY_ALBUM::6c8cFNdrK64laCDjFe9Q0h"},
;;      "amazonMusic"
;;      {"url" "https://music.amazon.com/albums/B00FN5I3X8?do=play", "entityUniqueId" "AMAZON_ALBUM::B00FN5I3X8"},
;;      "googleStore"
;;      {"url" "https://play.google.com/store/music/album?id=B4ulimbglqngmskfx6fsf2mlogi",
;;       "entityUniqueId" "GOOGLE_ALBUM::B4ulimbglqngmskfx6fsf2mlogi"},
;;      "itunes"
;;      {"url" "https://geo.music.apple.com/ru/album/_/722399370?mt=1&app=itunes&at=1000lHKX",
;;       "nativeAppUriMobile" "itmss://itunes.apple.com/ru/album/_/722399370?mt=1&app=itunes&at=1000lHKX",
;;       "nativeAppUriDesktop" "itmss://itunes.apple.com/ru/album/_/722399370?mt=1&app=itunes&at=1000lHKX",
;;       "entityUniqueId" "ITUNES_ALBUM::722399370"},
;;      "napster"
;;      {"url" "http://napster.com/artist/art.131557066/album/alb.131557069",
;;       "entityUniqueId" "NAPSTER_ALBUM::alb.131557069"}}})

;; (platforms-to-urls rapsody)
