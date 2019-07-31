(ns book.systems.mount.server
  (:require
   [mount.core :as mount :refer [defstate]]
   [ring.adapter.jetty :refer [run-jetty]]))

(def app (constantly {:status 200 :body "Hello"}))

(defstate server
  :start (run-jetty app {:join? false :port 8080})
  :stop (.stop server))
