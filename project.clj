(defproject songlink_unofficial_bot "0.1.0-SNAPSHOT"
  :description "Listen chat for links to musical services and respond with songlink.com link"
  :url "http://songlink-unofficial-bot.com/"

  ;; :min-lein-version "2.0.0"

  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}

  :dependencies [[org.clojure/clojure       "1.10.1"]
                 [environ                  "1.1.0"]
                 [morse                    "0.4.3"]
                 [compojure                "1.6.1"]
                 [ring/ring-core           "1.7.1"]
                 [ring/ring-jetty-adapter  "1.7.1"]
                 [ring-logger              "1.0.1"]
                 [ring/ring-json "0.4.0"]]

  :plugins [[lein-environ "1.1.0"]]
  :heroku {:app-name "songlink-unofficial-bot"}

  :main ^:skip-aot songlink-unofficial-bot.core
  :target-path "target/%s"
  :uberjar-name "songlink-unofficial-bot.jar"

  :profiles {:uberjar {:aot :all}})
