(defproject book "0.1.0-SNAPSHOT"

  :description "REPL sessions for my Clojure book"

  :url "http://grishaev.me/"

  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}

  :test-selectors
  {:db-experimental
   (fn [test-meta]
     (some-> test-meta :pg/version (>= 11)))}

  :dependencies [[org.clojure/clojure "1.10.0"]

                 ;; for web
                 [compojure "1.6.1"]

                 [ring/ring-json "0.5.0"]
                 [ring/ring-core "1.7.1"]
                 [ring/ring-jetty-adapter "1.7.1"]

                 ;; for spec chapter
                 [org.clojure/java.jdbc "0.7.8"]

                 ;; for exceptions chapter
                 [slingshot "0.12.2"]
                 [clj-http "3.9.1"]
                 [cheshire "5.8.1"]

                 [org.clojure/tools.logging "0.4.1"]
                 [ch.qos.logback/logback-classic "1.2.3"]

                 [io.sentry/sentry-clj "0.7.2"]

                 ;; config
                 [expound "0.7.2"]
                 [aero "1.1.3"]
                 [exoscale/yummy "0.2.6"]
                 [cprop "0.1.14"]

                 ;; systems
                 [org.postgresql/postgresql "42.1.3"]
                 [hikari-cp "2.8.0"]

                 [mount "0.1.16"]
                 [com.stuartsierra/component "0.4.0"]
                 [integrant "0.7.0"]]

  :target-path "target/%s"

  ;; :uberjar {:aot :all}

  :profiles {:test {:resource-paths ["env/test/resources"]}
             :dev  {:resource-paths ["env/test/resources"]}})
