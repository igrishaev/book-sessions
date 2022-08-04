(defproject repl-chapter "0.1.0-SNAPSHOT"

  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}

  :plugins []

  :main my-repl

  :dependencies [[org.clojure/clojure "1.10.0"]
                 [clj-http "3.9.1"]
                 [cheshire "5.8.1"]



                 ]

  :repl-options {;; :port 9911
                 ;; :host "0.0.0.0"
                 ;; :init-ns my-repl
                 :prompt (fn [the-ns]
                           (format "[%s] >> " the-ns))
                 ;; :transport nrepl.transport/edn
                 }

  :target-path "target/%s"

  :profiles
  {:docker
   {:local-repo ".docker/m2"
    :repl-options {:port ~(some-> "NREPL_PORT" (System/getenv) (Integer/parseInt))
                   :host "0.0.0.0"}
    :plugins []}


   :uberjar {:aot :all}
   :dev {:dependencies [[nrepl/nrepl "0.9.0"]]}

   })
