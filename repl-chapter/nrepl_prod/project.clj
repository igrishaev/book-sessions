(defproject nrepl_prod "0.1.0"

  :dependencies
  [[org.clojure/clojure "1.10.1"]
   [nrepl/nrepl "0.9.0"]
   [cider/cider-nrepl "0.28.3"]
   [com.stuartsierra/component "0.4.0"]]

  :main
  ^:skip-aot nrepl-prod.core

  :target-path
  "target/%s"

  :profiles
  {:uberjar
   {:aot :all
    :jvm-opts ["-Dclojure.compiler.direct-linking=true"]}})
