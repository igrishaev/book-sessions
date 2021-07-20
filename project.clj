(defproject book "0.1.0-SNAPSHOT"

  :description "REPL sessions for my Clojure book"

  :url "http://grishaev.me/"

  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}

  :plugins [[test2junit "1.1.2"]]
  :test2junit-output-dir "target/test2junit"

  ;; :test2junit-output-dir
  ;; (or (System/getenv "CI_ARTEFACTS")
  ;;     "/some/default/path")

  :test-selectors
  {:db-experimental
   (fn [test-meta]
     (some-> test-meta :pg/version (>= 11)))}

  :dependencies [[org.clojure/clojure "1.10.0"]

                 ;; for web
                 [bidi "2.1.5"]
                 [compojure "1.6.1"]
                 [metosin/ring-http-response "0.9.1"]

                 [ring/ring-json "0.5.0"]
                 [ring/ring-core "1.7.1"]
                 [ring/ring-jetty-adapter "1.7.1"]

                 ;; for spec chapter
                 [spec-dict "0.2.1"]
                 [metosin/spec-tools "0.10.4"]
                 [org.clojure/java.jdbc "0.7.8"]
                 [migratus "1.2.7"]

                 ;; for exceptions chapter
                 [slingshot "0.12.2"]
                 [clj-http "3.9.1"]
                 [cheshire "5.8.1"]

                 ;; logging
                 [org.clojure/tools.logging "0.4.1"]
                 [ch.qos.logback/logback-classic "1.2.3"]

                 ;; sentry
                 [exoscale/raven "0.4.15"]
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
                 [integrant "0.7.0"]

                 ;; tests
                 [tortue/spy "2.0.0"]
                 [ring/ring-mock "0.4.0"]
                 [org.clojure/test.check "0.10.0"]
                 [etaoin "0.3.6"]

                 ;; db/jdbc
                 [org.clojure/data.csv "1.0.0"]
                 [org.xerial/sqlite-jdbc "3.36.0"]]

  :target-path "target/%s"

  ;; :uberjar {:aot :all}

  :profiles {;; :uberjar {:resource-paths ["env/test/resources"]}

             :test {:resource-paths ["env/test/resources"]}
             :dev  {:resource-paths ["env/test/resources"]}})
