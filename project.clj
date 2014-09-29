(defproject background-on "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.6.0"]
                 [org.clojure/data.json "0.2.5"]
                 [clj-http "1.0.0"]
                 [ring/ring-core "1.3.1"]
                 [ring/ring-jetty-adapter "1.3.1"]
                 [compojure "1.1.9"]
                 [cheshire "5.3.1"]]
  :plugins [[lein-ring "0.8.5"]]
  :min-lein-version "2.0.0"
  :main ^:skip-aot background-on.web
  :uberjar-name "background-on.jar"
  :ring {:handler background-on.web/app
         :init    background-on.web/init
         :destroy background-on.web/destroy}
  :profiles {:uberjar {:aot :all}})
