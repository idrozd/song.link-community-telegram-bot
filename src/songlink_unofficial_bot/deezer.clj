(ns songlink-unofficial-bot.deezer
  (:require [cheshire.core :as json]
            [clj-http.util :refer [url-encode]]))

(defn fetch [id type]
  (let [type (or type "track")
        url (str "https://api.deezer.com/" type "/" id)]
    (->> url
     slurp
     json/parse-string
     clojure.walk/keywordize-keys)))


(defn tracks [result]
  (case (:type result)
    "track" [result]
    "album" (get-in result [:tracks :data])))


(defn album [fetched]
  (or (:album fetched) fetched) )

(defn audio [track]
  {:type "audio"
   :id (str "song-preview-" (:id track ))
   :audio (:preview track)
   :title (:title track)
   ;; :thumb (get-in track [:album :cover_small])
   :performer (get-in track [:artist :name])})


(comment

  (fetch 3135556 "track")

  (audio s)

  (def s {:explicit_content_cover 0,
    :disk_number 1,
    :explicit_content_lyrics 0,
    :release_date "2001-03-07",
    :readable true,
    :title_version "",
    :rank 723865,
    :type "track",
    :duration 224,
    :artist
    {:radio true,
     :picture_xl
     "https://e-cdns-images.dzcdn.net/images/artist/f2bc007e9133c946ac3c3907ddc5d2ea/1000x1000-000000-80-0-0.jpg",
     :name "Daft Punk",
     :type "artist",
     :picture_small
     "https://e-cdns-images.dzcdn.net/images/artist/f2bc007e9133c946ac3c3907ddc5d2ea/56x56-000000-80-0-0.jpg",
     :picture_big
     "https://e-cdns-images.dzcdn.net/images/artist/f2bc007e9133c946ac3c3907ddc5d2ea/500x500-000000-80-0-0.jpg",
     :picture_medium
     "https://e-cdns-images.dzcdn.net/images/artist/f2bc007e9133c946ac3c3907ddc5d2ea/250x250-000000-80-0-0.jpg",
     :share
     "https://www.deezer.com/artist/27?utm_source=deezer&utm_content=artist-27&utm_term=0_1571496327&utm_medium=web",
     :link "https://www.deezer.com/artist/27",
     :id 27,
     :picture "https://api.deezer.com/artist/27/image",
     :tracklist "https://api.deezer.com/artist/27/top?limit=50"},
    :title "Harder, Better, Faster, Stronger",
    :explicit_lyrics false,
    :title_short "Harder, Better, Faster, Stronger",
    :preview "https://cdns-preview-d.dzcdn.net/stream/c-deda7fa9316d9e9e880d2c6207e92260-5.mp3",
    :share
    "https://www.deezer.com/track/3135556?utm_source=deezer&utm_content=track-3135556&utm_term=0_1571496327&utm_medium=web",
    :link "https://www.deezer.com/track/3135556",
    :id 3135556,
    :contributors
    [{:role "Main",
      :radio true,
      :picture_xl
      "https://e-cdns-images.dzcdn.net/images/artist/f2bc007e9133c946ac3c3907ddc5d2ea/1000x1000-000000-80-0-0.jpg",
      :name "Daft Punk",
      :type "artist",
      :picture_small
      "https://e-cdns-images.dzcdn.net/images/artist/f2bc007e9133c946ac3c3907ddc5d2ea/56x56-000000-80-0-0.jpg",
      :picture_big
      "https://e-cdns-images.dzcdn.net/images/artist/f2bc007e9133c946ac3c3907ddc5d2ea/500x500-000000-80-0-0.jpg",
      :picture_medium
      "https://e-cdns-images.dzcdn.net/images/artist/f2bc007e9133c946ac3c3907ddc5d2ea/250x250-000000-80-0-0.jpg",
      :share
      "https://www.deezer.com/artist/27?utm_source=deezer&utm_content=artist-27&utm_term=0_1571496327&utm_medium=web",
      :link "https://www.deezer.com/artist/27",
      :id 27,
      :picture "https://api.deezer.com/artist/27/image",
      :tracklist "https://api.deezer.com/artist/27/top?limit=50"}],
    :gain -12.4,
    :track_position 4,
    :album
    {:release_date "2001-03-07",
     :cover_small "https://e-cdns-images.dzcdn.net/images/cover/2e018122cb56986277102d2041a592c8/56x56-000000-80-0-0.jpg",
     :cover_xl
     "https://e-cdns-images.dzcdn.net/images/cover/2e018122cb56986277102d2041a592c8/1000x1000-000000-80-0-0.jpg",
     :cover "https://api.deezer.com/album/302127/image",
     :type "album",
     :title "Discovery",
     :link "https://www.deezer.com/album/302127",
     :id 302127,
     :cover_medium
     "https://e-cdns-images.dzcdn.net/images/cover/2e018122cb56986277102d2041a592c8/250x250-000000-80-0-0.jpg",
     :tracklist "https://api.deezer.com/album/302127/tracks",
     :cover_big
     "https://e-cdns-images.dzcdn.net/images/cover/2e018122cb56986277102d2041a592c8/500x500-000000-80-0-0.jpg"},
    :bpm 123.4,
    :isrc "GBDUW0000059"})

  (fetch 302127 "album")

  (tracks (fetch 302127 "album") )



  (:cover_big (album x))

  (def x {:explicit_content_cover 0,
    :fans 201962,
    :nb_tracks 14,
    :record_type "album",
    :explicit_content_lyrics 7,
    :release_date "2001-03-07",
    :cover_small "https://e-cdns-images.dzcdn.net/images/cover/2e018122cb56986277102d2041a592c8/56x56-000000-80-0-0.jpg",
    :cover_xl "https://e-cdns-images.dzcdn.net/images/cover/2e018122cb56986277102d2041a592c8/1000x1000-000000-80-0-0.jpg",
    :genres {:data [{:id 113, :name "Dance", :picture "https://api.deezer.com/genre/113/image", :type "genre"}]},
    :tracks
    {:data
     [{:explicit_content_cover 0,
       :explicit_content_lyrics 0,
       :readable true,
       :title_version "",
       :rank 850065,
       :type "track",
       :duration 320,
       :artist {:id 27, :name "Daft Punk", :tracklist "https://api.deezer.com/artist/27/top?limit=50", :type "artist"},
       :title "One More Time",
       :explicit_lyrics false,
       :title_short "One More Time",
       :preview "https://cdns-preview-e.dzcdn.net/stream/c-e77d23e0c8ed7567a507a6d1b6a9ca1b-7.mp3",
       :link "https://www.deezer.com/track/3135553",
       :id 3135553}
      {:explicit_content_cover 0,
       :explicit_content_lyrics 6,
       :readable true,
       :title_version "",
       :rank 724626,
       :type "track",
       :duration 212,
       :artist {:id 27, :name "Daft Punk", :tracklist "https://api.deezer.com/artist/27/top?limit=50", :type "artist"},
       :title "Aerodynamic",
       :explicit_lyrics false,
       :title_short "Aerodynamic",
       :preview "https://cdns-preview-b.dzcdn.net/stream/c-b2e0166bba75a78251d6dca9c9c3b41a-5.mp3",
       :link "https://www.deezer.com/track/3135554",
       :id 3135554}
      {:explicit_content_cover 0,
       :explicit_content_lyrics 0,
       :readable true,
       :title_version "",
       :rank 629802,
       :type "track",
       :duration 301,
       :artist {:id 27, :name "Daft Punk", :tracklist "https://api.deezer.com/artist/27/top?limit=50", :type "artist"},
       :title "Digital Love",
       :explicit_lyrics false,
       :title_short "Digital Love",
       :preview "https://cdns-preview-0.dzcdn.net/stream/c-01ef0c4982c94b86c7c0e6b2a70dde4b-5.mp3",
       :link "https://www.deezer.com/track/3135555",
       :id 3135555}
      {:explicit_content_cover 0,
       :explicit_content_lyrics 0,
       :readable true,
       :title_version "",
       :rank 723865,
       :type "track",
       :duration 224,
       :artist {:id 27, :name "Daft Punk", :tracklist "https://api.deezer.com/artist/27/top?limit=50", :type "artist"},
       :title "Harder, Better, Faster, Stronger",
       :explicit_lyrics false,
       :title_short "Harder, Better, Faster, Stronger",
       :preview "https://cdns-preview-d.dzcdn.net/stream/c-deda7fa9316d9e9e880d2c6207e92260-5.mp3",
       :link "https://www.deezer.com/track/3135556",
       :id 3135556}
      {:explicit_content_cover 0,
       :explicit_content_lyrics 0,
       :readable true,
       :title_version "",
       :rank 546416,
       :type "track",
       :duration 211,
       :artist {:id 27, :name "Daft Punk", :tracklist "https://api.deezer.com/artist/27/top?limit=50", :type "artist"},
       :title "Crescendolls",
       :explicit_lyrics false,
       :title_short "Crescendolls",
       :preview "https://cdns-preview-0.dzcdn.net/stream/c-02585dc790f2904c4e870cb3bcecfcf3-5.mp3",
       :link "https://www.deezer.com/track/3135557",
       :id 3135557}
      {:explicit_content_cover 0,
       :explicit_content_lyrics 6,
       :readable true,
       :title_version "",
       :rank 485419,
       :type "track",
       :duration 104,
       :artist {:id 27, :name "Daft Punk", :tracklist "https://api.deezer.com/artist/27/top?limit=50", :type "artist"},
       :title "Nightvision",
       :explicit_lyrics false,
       :title_short "Nightvision",
       :preview "https://cdns-preview-1.dzcdn.net/stream/c-155b4d90d3d16d951e3d67c297988edc-5.mp3",
       :link "https://www.deezer.com/track/3135558",
       :id 3135558}
      {:explicit_content_cover 0,
       :explicit_content_lyrics 0,
       :readable true,
       :title_version "",
       :rank 539062,
       :type "track",
       :duration 237,
       :artist {:id 27, :name "Daft Punk", :tracklist "https://api.deezer.com/artist/27/top?limit=50", :type "artist"},
       :title "Superheroes",
       :explicit_lyrics false,
       :title_short "Superheroes",
       :preview "https://cdns-preview-3.dzcdn.net/stream/c-3d8caae0a1c59f417f31bb747c43818b-5.mp3",
       :link "https://www.deezer.com/track/3135559",
       :id 3135559}
      {:explicit_content_cover 0,
       :explicit_content_lyrics 0,
       :readable true,
       :title_version "",
       :rank 507930,
       :type "track",
       :duration 201,
       :artist {:id 27, :name "Daft Punk", :tracklist "https://api.deezer.com/artist/27/top?limit=50", :type "artist"},
       :title "High Life",
       :explicit_lyrics false,
       :title_short "High Life",
       :preview "https://cdns-preview-8.dzcdn.net/stream/c-8052077a75a884e93bda2e2b63f74bbb-5.mp3",
       :link "https://www.deezer.com/track/3135560",
       :id 3135560}
      {:explicit_content_cover 0,
       :explicit_content_lyrics 6,
       :readable true,
       :title_version "",
       :rank 668511,
       :type "track",
       :duration 232,
       :artist {:id 27, :name "Daft Punk", :tracklist "https://api.deezer.com/artist/27/top?limit=50", :type "artist"},
       :title "Something About Us",
       :explicit_lyrics false,
       :title_short "Something About Us",
       :preview "https://cdns-preview-9.dzcdn.net/stream/c-905aef3b23f4fb19db300a03f254fd6a-4.mp3",
       :link "https://www.deezer.com/track/3135561",
       :id 3135561}
      {:explicit_content_cover 0,
       :explicit_content_lyrics 0,
       :readable true,
       :title_version "",
       :rank 588362,
       :type "track",
       :duration 227,
       :artist {:id 27, :name "Daft Punk", :tracklist "https://api.deezer.com/artist/27/top?limit=50", :type "artist"},
       :title "Voyager",
       :explicit_lyrics false,
       :title_short "Voyager",
       :preview "https://cdns-preview-9.dzcdn.net/stream/c-98625d3ad54e88765fdfb812de62e515-5.mp3",
       :link "https://www.deezer.com/track/3135562",
       :id 3135562}
      {:explicit_content_cover 0,
       :explicit_content_lyrics 0,
       :readable true,
       :title_version "",
       :rank 731571,
       :type "track",
       :duration 345,
       :artist {:id 27, :name "Daft Punk", :tracklist "https://api.deezer.com/artist/27/top?limit=50", :type "artist"},
       :title "Veridis Quo",
       :explicit_lyrics false,
       :title_short "Veridis Quo",
       :preview "https://cdns-preview-f.dzcdn.net/stream/c-f6fde4f6f42bde740e3d07b019fde318-4.mp3",
       :link "https://www.deezer.com/track/3135563",
       :id 3135563}
      {:explicit_content_cover 0,
       :explicit_content_lyrics 0,
       :readable true,
       :title_version "",
       :rank 471250,
       :type "track",
       :duration 206,
       :artist {:id 27, :name "Daft Punk", :tracklist "https://api.deezer.com/artist/27/top?limit=50", :type "artist"},
       :title "Short Circuit",
       :explicit_lyrics false,
       :title_short "Short Circuit",
       :preview "https://cdns-preview-6.dzcdn.net/stream/c-6ef3bfc9e8f226b582bade5842df4517-6.mp3",
       :link "https://www.deezer.com/track/3135564",
       :id 3135564}
      {:explicit_content_cover 0,
       :explicit_content_lyrics 0,
       :readable true,
       :title_version "",
       :rank 551762,
       :type "track",
       :duration 240,
       :artist {:id 27, :name "Daft Punk", :tracklist "https://api.deezer.com/artist/27/top?limit=50", :type "artist"},
       :title "Face to Face",
       :explicit_lyrics false,
       :title_short "Face to Face",
       :preview "https://cdns-preview-7.dzcdn.net/stream/c-7af918cb131b9d5b8f5c1e40e62da91b-6.mp3",
       :link "https://www.deezer.com/track/3135565",
       :id 3135565}
      {:explicit_content_cover 0,
       :explicit_content_lyrics 0,
       :readable true,
       :title_version "",
       :rank 510397,
       :type "track",
       :duration 600,
       :artist {:id 27, :name "Daft Punk", :tracklist "https://api.deezer.com/artist/27/top?limit=50", :type "artist"},
       :title "Too Long",
       :explicit_lyrics false,
       :title_short "Too Long",
       :preview "https://cdns-preview-d.dzcdn.net/stream/c-ddf495316e2afbe4327d9a6e17840a69-5.mp3",
       :link "https://www.deezer.com/track/3135566",
       :id 3135566}]},
    :cover "https://api.deezer.com/album/302127/image",
    :type "album",
    :duration 3660,
    :artist
    {:picture_xl
     "https://e-cdns-images.dzcdn.net/images/artist/f2bc007e9133c946ac3c3907ddc5d2ea/1000x1000-000000-80-0-0.jpg",
     :name "Daft Punk",
     :type "artist",
     :picture_small
     "https://e-cdns-images.dzcdn.net/images/artist/f2bc007e9133c946ac3c3907ddc5d2ea/56x56-000000-80-0-0.jpg",
     :picture_big
     "https://e-cdns-images.dzcdn.net/images/artist/f2bc007e9133c946ac3c3907ddc5d2ea/500x500-000000-80-0-0.jpg",
     :picture_medium
     "https://e-cdns-images.dzcdn.net/images/artist/f2bc007e9133c946ac3c3907ddc5d2ea/250x250-000000-80-0-0.jpg",
     :id 27,
     :picture "https://api.deezer.com/artist/27/image",
     :tracklist "https://api.deezer.com/artist/27/top?limit=50"},
    :title "Discovery",
    :explicit_lyrics false,
    :share
    "https://www.deezer.com/album/302127?utm_source=deezer&utm_content=album-302127&utm_term=0_1571496324&utm_medium=web",
    :link "https://www.deezer.com/album/302127",
    :label "Parlophone (France)",
    :id 302127,
    :upc "724384960650",
    :contributors
    [{:role "Main",
      :radio true,
      :picture_xl
      "https://e-cdns-images.dzcdn.net/images/artist/f2bc007e9133c946ac3c3907ddc5d2ea/1000x1000-000000-80-0-0.jpg",
      :name "Daft Punk",
      :type "artist",
      :picture_small
      "https://e-cdns-images.dzcdn.net/images/artist/f2bc007e9133c946ac3c3907ddc5d2ea/56x56-000000-80-0-0.jpg",
      :picture_big
      "https://e-cdns-images.dzcdn.net/images/artist/f2bc007e9133c946ac3c3907ddc5d2ea/500x500-000000-80-0-0.jpg",
      :picture_medium
      "https://e-cdns-images.dzcdn.net/images/artist/f2bc007e9133c946ac3c3907ddc5d2ea/250x250-000000-80-0-0.jpg",
      :share
      "https://www.deezer.com/artist/27?utm_source=deezer&utm_content=artist-27&utm_term=0_1571496324&utm_medium=web",
      :link "https://www.deezer.com/artist/27",
      :id 27,
      :picture "https://api.deezer.com/artist/27/image",
      :tracklist "https://api.deezer.com/artist/27/top?limit=50"}],
    :cover_medium
    "https://e-cdns-images.dzcdn.net/images/cover/2e018122cb56986277102d2041a592c8/250x250-000000-80-0-0.jpg",
    :tracklist "https://api.deezer.com/album/302127/tracks",
    :available true,
    :genre_id 113,
    :rating 0,
    :cover_big "https://e-cdns-images.dzcdn.net/images/cover/2e018122cb56986277102d2041a592c8/500x500-000000-80-0-0.jpg"})



  (def example {"title_version" "",
                "album"
                {"cover" "https://api.deezer.com/album/302127/image",
                 "cover_small"
                 "https://e-cdns-images.dzcdn.net/images/cover/2e018122cb56986277102d2041a592c8/56x56-000000-80-0-0.jpg",
                 "id" 302127,
                 "release_date" "2001-03-07",
                 "tracklist" "https://api.deezer.com/album/302127/tracks",
                 "title" "Discovery",
                 "link" "https://www.deezer.com/album/302127",
                 "type" "album",
                 "cover_big"
                 "https://e-cdns-images.dzcdn.net/images/cover/2e018122cb56986277102d2041a592c8/500x500-000000-80-0-0.jpg",
                 "cover_xl"
                 "https://e-cdns-images.dzcdn.net/images/cover/2e018122cb56986277102d2041a592c8/1000x1000-000000-80-0-0.jpg",
                 "cover_medium"
                 "https://e-cdns-images.dzcdn.net/images/cover/2e018122cb56986277102d2041a592c8/250x250-000000-80-0-0.jpg"},
                "readable" true,
                "track_position" 4,
                "explicit_content_lyrics" 0,
                "id" 3135556,
                "explicit_lyrics" false,
                "rank" 723865,
                "bpm" 123.4,
                "release_date" "2001-03-07",
                "title_short" "Harder, Better, Faster, Stronger",
                "isrc" "GBDUW0000059",
                "explicit_content_cover" 0,
                "duration" 224,
                "title" "Harder, Better, Faster, Stronger",
                "link" "https://www.deezer.com/track/3135556",
                "type" "track",
                "preview" "https://cdns-preview-d.dzcdn.net/stream/c-deda7fa9316d9e9e880d2c6207e92260-5.mp3",
                "disk_number" 1,
                "contributors" [{"role" "Main",
                  "picture_medium"
                  "https://e-cdns-images.dzcdn.net/images/artist/f2bc007e9133c946ac3c3907ddc5d2ea/250x250-000000-80-0-0.jpg",
                  "id" 27,
                  "name" "Daft Punk",
                  "tracklist" "https://api.deezer.com/artist/27/top?limit=50",
                  "radio" true,
                  "picture_xl"
                  "https://e-cdns-images.dzcdn.net/images/artist/f2bc007e9133c946ac3c3907ddc5d2ea/1000x1000-000000-80-0-0.jpg",
                  "link" "https://www.deezer.com/artist/27",
                  "picture_small"
                  "https://e-cdns-images.dzcdn.net/images/artist/f2bc007e9133c946ac3c3907ddc5d2ea/56x56-000000-80-0-0.jpg",
                  "type" "artist",
                  "picture" "https://api.deezer.com/artist/27/image",
                  "picture_big"
                  "https://e-cdns-images.dzcdn.net/images/artist/f2bc007e9133c946ac3c3907ddc5d2ea/500x500-000000-80-0-0.jpg",
                  "share"
                  "https://www.deezer.com/artist/27?utm_source=deezer&utm_content=artist-27&utm_term=0_1570953485&utm_medium=web"}],
                "artist"
                {"picture_medium"
                 "https://e-cdns-images.dzcdn.net/images/artist/f2bc007e9133c946ac3c3907ddc5d2ea/250x250-000000-80-0-0.jpg",
                 "id" 27,
                 "name" "Daft Punk",
                 "tracklist" "https://api.deezer.com/artist/27/top?limit=50",
                 "radio" true,
                 "picture_xl"
                 "https://e-cdns-images.dzcdn.net/images/artist/f2bc007e9133c946ac3c3907ddc5d2ea/1000x1000-000000-80-0-0.jpg",
                 "link" "https://www.deezer.com/artist/27",
                 "picture_small"
                 "https://e-cdns-images.dzcdn.net/images/artist/f2bc007e9133c946ac3c3907ddc5d2ea/56x56-000000-80-0-0.jpg",
                 "type" "artist",
                 "picture" "https://api.deezer.com/artist/27/image",
                 "picture_big"
                 "https://e-cdns-images.dzcdn.net/images/artist/f2bc007e9133c946ac3c3907ddc5d2ea/500x500-000000-80-0-0.jpg",
                 "share"
                 "https://www.deezer.com/artist/27?utm_source=deezer&utm_content=artist-27&utm_term=0_1570953485&utm_medium=web"},
                "gain" -12.4,
                "share"
                "https://www.deezer.com/track/3135556?utm_source=deezer&utm_content=track-3135556&utm_term=0_1570953485&utm_medium=web"})

  )


(comment

  (audio (track 3135556))

  )
