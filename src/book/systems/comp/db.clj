(ns book.systems.comp.db
  (:require
   [com.stuartsierra.component :as component]
   [clojure.java.jdbc :as jdbc]
   [hikari-cp.core :as cp]))


(defprotocol IDB

  (query [this sql-params])

  (update! [this table set-map where-clause]))


(defrecord DB
    [options
     db-spec]

  component/Lifecycle

  (start [this]
    (let [pool (cp/make-datasource options)]
      (assoc this :db-spec {:datasource pool})))

  (stop [this]
    (-> db-spec :datasource cp/close-datasource)
    (assoc this :db-spec nil))

  IDB

  (query [this sql-params]
    (jdbc/query db-spec sql-params))

  (update! [this table set-map where-clause]
    (jdbc/update! db-spec table set-map where-clause)))


(defn make-db [options]
  (map->DB {:options options}))


(defmacro with-db-transaction
  [[comp-trx comp-db & trx-opt] & body]
  `(let [{db-spec# :db-spec} ~comp-db]
     (jdbc/with-db-transaction
       [t-conn# db-spec# ~@trx-opt]
       (let [~comp-trx (assoc ~comp-db :db-spec t-conn#)]
         ~@body))))



#_
(defrecord DB
    [options db-spec]
  component/Lifecycle
  (start [this]
    (let [pool (cp/make-datasource options)]
      (assoc this :db-spec {:datasource pool})))
  (stop [this]
    (-> db-spec :datasource cp/close-datasource)
    (assoc this :db-spec nil)))



#_
(defn ___ []
  (let [db-started (-> {} make-db component/start)
        {:keys [db-spec]} db-started
        users (jdbc/query db-spec "select * from users")]
    (do users)))

#_
(let [{:keys [db-spec]} db-started
      users (jdbc/query db-spec "select * from users")]
  (process-users users))


#_
(;; ........

  (stop [this]
    (-> db-spec :datasource cp/close-datasource)
      (assoc this :db-spec nil))

  IDB

  (query [this sql-params]
    (jdbc/query db-spec sql-params))

  (update! [this table set-map where-clause]
    (jdbc/update! db-spec table set-map where-clause)))


#_
(def options
  {:auto-commit        true
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
   :port-number        5432
   :register-mbeans    false})


;; (def db-created (make-db options))
;; (def db-started (component/start db-created))
;; (query db-started "select * from requests")
;; (update! db-started :requests {:is_processed false} ["id = ?" 42])
;; (def db-stopped (component/stop db-started))


#_
(with-db-transaction
  [db-tx db-started]
  (let [requests (query db-tx "select * from requests limit 1 for update")]
    (when-let [request (first requests)]
      (update! db-tx :requests {:is_processed false} ["id = ?" (:id request)]))))


"
BEGIN
select * from requests limit 1 for update
UPDATE requests SET is_processed = $1 WHERE id = $2
DETAIL:  parameters: $1 = 'f', $2 = '3'
COMMIT
"
