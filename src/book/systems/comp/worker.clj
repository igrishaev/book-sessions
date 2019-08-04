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


(defn task-fn
  [db]
  (db/with-trx [tx db]
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
                      ["id = ?" id]))))))


(defn make-task
  [db flag options]
  (let [{:keys [sleep]} options]
    (future
      (while @flag
        (try
          (task-fn db)
          (catch Throwable e
            (log/error e))
          (finally
            (Thread/sleep sleep)))))))


(defrecord Worker
    [options
     flag
     task
     db]

    component/Lifecycle

    (start [this]
      (let [flag (atom true)
            task (make-task db flag options)]
        (assoc this
               :flag flag
               :task task)))

    (stop [this]
      (reset! flag false)
      (while (not (realized? task))
        (log/info "Waiting for the task to complete")
        (Thread/sleep 300))
      (assoc this
             :flag nil
             :task nil)))


(defn make-worker
  [options]
  (map->Worker {:options options}))
