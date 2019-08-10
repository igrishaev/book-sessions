(ns book.systems.comp.worker
  (:require
   [com.stuartsierra.component :as component]
   [book.systems.comp.db :as db]
   [clj-http.client :as client]
   [clojure.tools.logging :as log]))


(defn get-ip-info
  [ip]
  (:body
   (client/post "https://iplocation.com"
                {:form-params {:ip ip}
                 :as :json})))


(def requests-query
  "SELECT * FROM requests
   WHERE NOT is_processed
   LIMIT 1 FOR UPDATE;")


(defprotocol IWorker
  (make-task [this])
  (task-fn [this]))


(defrecord Worker
    [options
     flag
     task
     db]

    component/Lifecycle

    (start [this]
      (let [flag (atom true)
            this (assoc this :flag flag)
            task (make-task this)]
        (assoc this :task task)))

    (stop [this]
      (reset! flag false)
      (while (not (realized? task))
        (log/info "Waiting for the task to complete")
        (Thread/sleep 300))
      (assoc this
             :flag nil
             :task nil))

    IWorker

    (make-task [this]
      (let [{:keys [sleep]} options]
        (future
          (while @flag
            (try
              (task-fn this)
              (catch Throwable e
                (log/error e))
              (finally
                (Thread/sleep sleep)))))))

    (task-fn [this]
      (db/with-db-transaction [tx db]
        (let [requests (db/query tx requests-query)]
          (doseq [request requests]
            (let [{:keys [id ip]} request
                  info (get-ip-info ip)
                  fields {:is_processed true
                          :zip (:postal_code info)
                          :country (:country_name info)
                          :city (:city info)
                          :lat (:lat info)
                          :lon (:lng info)}]
              (db/update! tx :requests
                          fields
                          ["id = ?" id])))))))


(defn make-worker
  [options]
  (map->Worker {:options options}))
