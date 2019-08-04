(ns book.systems.comp.server
  (:require
   [com.stuartsierra.component :as component]
   [ring.adapter.jetty :refer [run-jetty]]))

(def app (constantly {:status 200 :body "Hello"}))

(defrecord Server
    [options
     server]

  component/Lifecycle

  (start [this]
    (let [server (run-jetty app options)]
      (assoc this :server server)))

  (stop [this]
    (.stop server)
    (assoc this :server nil)))


;; (def c0 (map->Server {:options {:port 8080 :join? false}}))
;; (def c1 (component/start c0))
;; (def c2 (component/stop c1))

(defn make-server
  [jetty-options]
  (map->Server {:options jetty-options}))
