(ns book.systems.mount.config
  (:require
   [mount.core :as mount :refer [defstate]]
   [clojure.edn :as edn]))


(defstate config
  :start
  (-> "system.config.edn" slurp edn/read-string))
