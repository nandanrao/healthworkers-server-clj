(defproject healthworkers-server "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [org.clojure/tools.logging "0.3.1"]
                 [ch.qos.logback/logback-classic "1.2.3"]
                 [com.novemberain/monger "3.1.0"]
                 [com.taoensso/carmine "2.18.1"]
                 [ring/ring-defaults "0.2.1"]
                 [clj-time "0.14.2"]
                 [clojure.java-time "0.3.0"]
                 [environ "1.1.0"]
                 [cheshire "5.8.0"]
                 [ring-cors "0.1.11"]
                 [ring/ring-json "0.4.0"]
                 [compojure "1.6.0"]]
  :plugins [[lein-ring "0.9.7"]]
  :ring {:handler healthworkers-server.core/app}
  :profiles
  {:dev {:dependencies [[javax.servlet/servlet-api "2.5"]
                        [ring/ring-mock "0.3.2"]]}}

  ;; :main ^:skip-aot healthworkers-server.core
  ;; :target-path "target/%s"
  ;; :profiles {:uberjar {:aot :all}}
  )
