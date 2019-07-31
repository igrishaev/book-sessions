(ns book.systems.mount.server-better
  (:require
   [mount.core :as mount :refer [defstate]]
   [ring.adapter.jetty :refer [run-jetty]]
   [book.systems.mount.config :refer [config]])
  (:import
   org.eclipse.jetty.server.Server))


;; config added
;; noop added


(defn app
  [request]
  {:status 200
   :headers {"content-type" "text/plain"}
   :body "Hello!"})


#_
(defstate
  ^{:on-reload :noop} ;; !!!
  server
  :start
  (let [{jetty-opt :jetty} config]
    (run-jetty app jetty-opt))
  :stop (.stop ^Server server))


(defstate
  server
  :start
  (let [{jetty-opt :jetty} config]
    (run-jetty app jetty-opt))
  :stop (.stop ^Server server))
