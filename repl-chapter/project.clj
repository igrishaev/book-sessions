(defproject repl-chapter "0.1.0-SNAPSHOT"

  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}

  :plugins [
            ]

  :main my-repl

  :dependencies [[org.clojure/clojure "1.10.0"]

                 [clj-http "3.9.1"]
                 [cheshire "5.8.1"]

                 ]

  :target-path "target/%s"

  :profiles {:uberjar {:aot :all}})
