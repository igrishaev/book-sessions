(ns book.systems.mount.server
  (:require
   [book.systems.mount.config :refer [config]]
   [mount.core :as mount :refer [defstate]]
   [ring.adapter.jetty :refer [run-jetty]]))

(def app (constantly {:status 200 :body "Hello"}))

(defstate ^{:on-reload :noop} server
  :start (run-jetty app {:join? false :port 8080})
  :stop (.stop server))
