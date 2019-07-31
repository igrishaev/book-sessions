(ns book.systems.mount.config
  (:require
   [mount.core :as mount :refer [defstate]]
   [clojure.spec.alpha :as s]
   [clojure.edn :as edn]))

(s/def ::config map?)

(defstate config
  :start
  (-> "system.config.edn"
      slurp
      edn/read-string
      (as-> config
          (s/conform ::config config))))
