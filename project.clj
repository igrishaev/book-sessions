(defproject book "0.1.0-SNAPSHOT"

  :description "REPL sessions for my Clojure book"

  :url "http://grishaev.me/"

  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}

  :dependencies [[org.clojure/clojure "1.10.0"]

                 ;; for spec
                 [org.clojure/java.jdbc "0.7.8"]]

  :target-path "target/%s"

  :profiles {:uberjar {:aot :all}})
