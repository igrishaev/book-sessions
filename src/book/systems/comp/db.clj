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
    (let [pool (cp/make-datasource options)
          spec {:datasource pool}]
      (assoc this :db-spec spec)))

  (stop [this]
    (-> db-spec :datasource cp/close-datasource)
    (assoc this :db-spec nil))

  IDB

  (query [this sql-params]
    (jdbc/query db-spec sql-params))

  (update! [this table set-map where-clause]
    (jdbc/update! db-spec table set-map where-clause)))


(defn make-db [pool-options]
  (map->DB {:options pool-options}))


(defmacro with-db-transaction
  [[comp-trx comp-db & trx-opt] & body]
  `(let [{db-spec# :db-spec} ~comp-db]
     (jdbc/with-db-transaction
       [t-conn# db-spec# ~@trx-opt]
       (let [~comp-trx (assoc ~comp-db :db-spec t-conn#)]
         ~@body))))
