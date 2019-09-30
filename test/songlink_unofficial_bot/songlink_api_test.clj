(ns songlink-unofficial-bot.songlink-api-test
  (:require [clojure.test :as t :refer [deftest is testing]]
            [environ.core :refer [env]]
            [songlink-unofficial-bot.songlink-api :as sl]))

(deftest fetch-links-test
  (testing "it works"
    (is (sl/fetch-links "https://music.apple.com/us/album/screen-shot/836834698?i=836834718&ign-mpt=uo%3D4")))
  (testing "with token it still works"
    (is (sl/fetch-links "https://music.apple.com/us/album/screen-shot/836834698?i=836834718&ign-mpt=uo%3D4"
                            (env :songlink-token)))))
