(ns songlink-unofficial-bot.apple-music
  (:require [cheshire.core :as json]
            [ring.util.codec :refer [form-encode] ]))


(defn search-url
  "to search for albums, use {:term \"Queen\" :entity \"album\"}
  https://developer.apple.com/library/archive/documentation/AudioVideo/Conceptual/iTuneSearchAPI/Searching.html"
  [prm]
  (let [prm (merge {:country "RU" :entity "song" :media "music" :limit 20 :lang "en"} prm)
        url (str "https://itunes.apple.com/search?" (form-encode prm))]
    url))


(defn search [prm]
  (let [ url (search-url prm)
         result (slurp url)]
    (-> result
        json/parse-string
        clojure.walk/keywordize-keys
        :results)))


(defn song-result->inline-article [result]
  {:type "article"
   :id (str "song-" (:trackId result))

   :thumb_url (:artworkUrl30 result)
   :title (:trackName result)
   :description (str "From " (:collectionName result) " by " (:artistName result))

   :hide_url false
   :url (str "https://song.link/" (:trackViewUrl result))
   :message_text (str "https://song.link/" (:trackViewUrl result))
   })


(defn song-result->audio
  "https://core.telegram.org/bots/api#inlinequeryresultaudio"
  [result]
  {:type "audio"
   :id (str "song-preview-" (:trackId result))
   :audio_url (:previewUrl result)
   :title (:trackName result)
   :caption (:trackName result)
   :performer (:artistName result)
   })



(comment
  ;; cider workbench

  (->> {:term "barry - mandy" :entity "musicTrack"}
       search
       (take 2)
       ;; (map #(select-keys % [:trackName :artistName :collectionName :primaryGenreName])))
       ;; (map #(select-keys % [:trackName :collectionName :artistName ]))
       ;; (map vals)
       )

  (def apple-res-example

    {:collectionArtistId 80204262,
     :artistId 16233,
     :collectionId 795614222,
     :trackName "Mandy",
     :artworkUrl60
     "https://is4-ssl.mzstatic.com/image/thumb/Music6/v4/c6/e6/ec/c6e6ecca-e3b5-6036-65c6-25e430db8802/source/60x60bb.jpg",
     :collectionArtistName "Various Artists",
     :trackPrice 22.0,
     :discNumber 2,
     :collectionPrice 299.0,
     :isStreamable true,
     :artistName "Barry Manilow",
     :primaryGenreName "Pop",
     :trackTimeMillis 204880,
     :collectionViewUrl "https://music.apple.com/ru/album/mandy-remastered/795614222?i=795614326&l=en&uo=4",
     :releaseDate "2014-01-24T12:00:00Z",
     :trackId 795614326,
     :previewUrl
     "https://audio-ssl.itunes.apple.com/itunes-assets/Music/v4/c3/22/2c/c3222c38-96cd-fc9f-161d-f078b3e2b91e/mzaf_8754900959084083747.plus.aac.p.m4a",
     :trackCount 21,
     :artworkUrl30
     "https://is4-ssl.mzstatic.com/image/thumb/Music6/v4/c6/e6/ec/c6e6ecca-e3b5-6036-65c6-25e430db8802/source/30x30bb.jpg",
     :currency "RUB",
     :discCount 3,
     :trackNumber 20,
     :trackViewUrl "https://music.apple.com/ru/album/mandy-remastered/795614222?i=795614326&l=en&uo=4",
     :wrapperType "track",
     :kind "song",
     :collectionExplicitness "notExplicit",
     :artistViewUrl "https://music.apple.com/ru/artist/barry-manilow/16233?l=en&uo=4",
     :collectionName "Love Songs",
     :trackExplicitness "notExplicit",
     :trackCensoredName "Mandy (Remastered)",
     :artworkUrl100
     "https://is4-ssl.mzstatic.com/image/thumb/Music6/v4/c6/e6/ec/c6e6ecca-e3b5-6036-65c6-25e430db8802/source/100x100bb.jpg",
     :collectionCensoredName "Love Songs",
     :country "RUS"})

  )
