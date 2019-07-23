(ns book.systems.mount.server
  (:require
   [mount.core :as mount :refer [defstate]]
   [ring.adapter.jetty :refer [run-jetty]])
  (:import
   org.eclipse.jetty.server.Server))


(defn app
  [request]
  {:status 200
   :headers {"content-type" "text/plain"}
   :body "Hello!"})


(defstate server
  :start (run-jetty app {:join? false
                         :port 8080})
  :stop (.stop ^Server server))
