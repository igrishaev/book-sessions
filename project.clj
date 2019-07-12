(defproject book "0.1.0-SNAPSHOT"

  :description "REPL sessions for my Clojure book"

  :url "http://grishaev.me/"

  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}

  :dependencies [[org.clojure/clojure "1.10.0"]

                 ;; for web

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

                 ;; for spec and config
                 [expound "0.7.2"]

                 [aero "1.1.3"]
                 [exoscale/yummy "0.2.6"]
                 [cprop "0.1.14"]]

  :target-path "target/%s"

  :profiles {:uberjar {:aot :all}})
