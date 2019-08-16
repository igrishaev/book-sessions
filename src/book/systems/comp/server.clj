(ns book.systems.comp.server
  (:require
   [com.stuartsierra.component :as component]
   [book.systems.comp.db :as db]
   [ring.adapter.jetty :refer [run-jetty]]))

(def app (constantly {:status 200 :body "Hello"}))

#_
(defn app
  [request]
  {:status 200
   :body (with-out-str
           (clojure.pprint/pprint request))})


#_
(defn app
  [request]
  (let [{:keys [db]} request
        data (db/query db "select * from requests")]
    {:status 200
     :body (with-out-str
             (clojure.pprint/pprint data))}))


#_
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


(defn make-handler [app db]
  (fn [request]
    (app (assoc request :db db))))


(defrecord Server
    [options
     server
     db]

    component/Lifecycle

    (start [this]
      (let [handler (make-handler app db)
            server (run-jetty handler options)]
        (assoc this :server server)))

    (stop [this]
      (.stop server)
      (assoc this :server nil)))



#_
(defn make-server
  [options]
  (map->Server {:options options}))


(defn make-server
  [options]
  (-> (map->Server {:options options})
      (component/using [:db])))

;; (def c0 (map->Server {:options {:port 8080 :join? false}}))
;; (def c1 (component/start c0))
;; (def c2 (component/stop c1))

;; (def s-created (map->Server {:options {:port 8080 :join? false}}))
;; (def s-started (component/start s-created))
;; (def s-stopped (component/stop s-started))

#_
(def s-created
  (map->Server
   {:options {:port 8080 :join? false}}))

#_(def s-started (component/start s-created))

#_(def s-stopped (component/stop s-started))

#_
(def s-created (make-server {:port 8080 :join? false}))

#_
(defrecord Server
    [options
     server]

    component/Lifecycle

    (start [this]
      (if server
        this
        (let [server (run-jetty app options)]
          (assoc this :server server))))

    (stop [this]
      (when server
        (.stop server))
      (assoc this :server nil)))


#_
(defrecord BadServer
  [options server]
  component/Lifecycle
  (start [this]
    {:server (run-jetty app options)})
  (stop [this]
    (.stop server)
    nil))


;; (def bs-created (map->BadServer {:options {:port 8080 :join? false}}))
;; (def bs-started (component/start bs-created))

"
2019-08-07 10:21:47,531 INFO  org.eclipse.jetty.server.Server - jetty-9.4.12.v20180830; built: 2018-08-30T13:59:14.071Z; git: 27208684755d94a92186989f695db2d7b21ebc51; jvm 1.8.0_102-b14
2019-08-07 10:21:47,538 INFO  o.e.jetty.server.AbstractConnector - Started ServerConnector@405ff5ed{HTTP/1.1,[http/1.1]}{0.0.0.0:8080}
2019-08-07 10:21:47,542 INFO  org.eclipse.jetty.server.Server - Started @61386534ms
"

#_
(def bs-stopped (component/stop bs-started))


#_
NullPointerException
