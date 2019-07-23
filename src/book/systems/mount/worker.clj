(ns book.systems.mount.worker
  (:require
   [mount.core :as mount :refer [defstate]]
   [clojure.java.jdbc :as jdbc]
   [clj-http.client :as client]
   [clojure.tools.logging :as log]
   [book.systems.mount.db :refer [db]]
   [book.systems.mount.config :refer [config]]))


(defn get-ip-info
  [ip]
  (:body
   (client/post "https://iplocation.com"
                {:form-params {:ip ip}
                 :as :json})))


(def query
  "
SELECT * FROM requests
WHERE NOT is_processed
LIMIT 1
FOR UPDATE;
")


(defn task-fn
  []
  (jdbc/with-db-transaction [tx db]
    (let [requests (jdbc/query tx query)]
      (doseq [request requests]
        (let [{:keys [id ip]} request
              info (get-ip-info ip)
              fields {:is_processed true
                      :zip     (:postal_code info)
                      :country (:country_name info)
                      :city    (:city info)
                      :lat     (:lat info)
                      :lon     (:lng info)}]
          (jdbc/update! tx :requests
                        fields
                        ["id = ?" id]))))))


(defstate
  worker

  :start
  (let [sleep-time (-> config :worker :sleep)
        flag (atom true)
        task (future
               (while @flag
                 (try
                   (task-fn)
                   (catch Throwable e
                     (log/error e))
                   (finally
                     (Thread/sleep sleep-time)))))]
    {:flag flag
     :task task})

  :stop
  (let [{:keys [flag task]} worker]
    (reset! flag false)
    (while (not (realized? task))
      (log/info "Waiting for the task to complete")
      (Thread/sleep 300))))


#_
(comment
  "31.148.198.0"
  (get-ip-info "85.214.132.117")

  {:postal_code "12529"
   :ip "85.214.132.117"
   :continent_code "EU"
   :region_name "Land Berlin"
   :city "Berlin"
   :isp "Strato AG"
   :ip_header "IP address"
   :region "BE"
   :country_code "DE"
   :country_name "Germany"
   :metro_code nil
   :found 1
   :time_zone "Europe/Berlin"
   :lat 52.5167
   :company "Strato AG"
   :lng 13.4})
