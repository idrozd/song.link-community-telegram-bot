(ns songlink-unofficial-bot.core-test
  (:require [clojure.test :refer :all]
    [songlink-unofficial-bot.core :refer :all]))

(deftest responses-test
  (testing "works"
    (is (responses "https://music.apple.com/us/album/screen-shot/836834698?i=836834718&ign-mpt=uo%3D4"))))
