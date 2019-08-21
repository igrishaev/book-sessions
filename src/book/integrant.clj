(ns book.integrant
  (:require
   [integrant.core :as i]
   [clojure.java.jdbc :as jdbc]
   [clj-http.client :as client]
   [clojure.tools.logging :as log]
   [ring.adapter.jetty :refer [run-jetty]]
   [hikari-cp.core :as cp]))


(def config
  {::server {:port 8080 :join? false}
   ::db {:auto-commit        true
         :read-only          false
         :connection-timeout 30000
         :validation-timeout 5000
         :idle-timeout       600000
         :max-lifetime       1800000
         :minimum-idle       10
         :maximum-pool-size  10
         :pool-name          "book-pool"
         :adapter            "postgresql"
         :username           "book"
         :password           "book"
         :database-name      "book"
         :server-name        "127.0.0.1"
         :port-number        5432}

   ::worker {:options {:sleep 1000}
             :db (i/ref ::db)}})


(def app (constantly {:status 200 :body "Hello"}))


(defmethod i/init-key ::server
  [_ options]
  (run-jetty app options))

(defmethod i/halt-key! ::server
  [_ server]
  (.stop server))


(defmethod i/init-key ::db
  [_ options]
  {:datasource (cp/make-datasource options)})


(defmethod i/halt-key! ::db [_ db-spec]
  (-> db-spec :datasource cp/close-datasource))



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
  (jdbc/with-db-transaction [tx db]
    (let [requests (jdbc/query tx requests-query)]
      (doseq [request requests]
        (let [{:keys [id ip]} request
              info (get-ip-info ip)
              fields {:is_processed true
                      :zip (:postal_code info)
                      :country (:country_name info)
                      :city (:city info)
                      :lat (:lat info)
                      :lon (:lng info)}]
          (jdbc/update! tx :requests
                        fields
                        ["id = ?" id]))))))


(defn make-task
  [db flag opt]
  (let [{:keys [sleep]} opt]
    (future
      (while @flag
        (try
          (task-fn db)
          (catch Throwable e
            (log/error e))
          (finally
            (Thread/sleep sleep)))))))


(defmethod i/init-key ::worker
  [_ {:keys [db options]}]
  (let [flag (atom true)
        task (make-task db flag options)]
    {:flag flag :task task}))


(defmethod i/halt-key! ::worker
  [_ {:keys [flag task]}]
  (reset! flag false)
  (while (not (realized? task))
    (Thread/sleep 300)))



(defonce ^:private system nil)

(def alter-system (partial alter-var-root #'system))

(defn system-start []
  (alter-system (constantly (i/init config))))

(defn system-stop []
  (alter-system i/halt!))
