(ns book.systems.comp.db
  (:require
   [com.stuartsierra.component :as component]
   [clojure.java.jdbc :as jdbc]
   [hikari-cp.core :as cp]))


(defprotocol IDB

  (get-spec [this])

  (set-spec [this spec])

  (query [this sql-params])

  (update! [this table set-map where-clause]))


(defrecord DB
    [options
     pool
     spec]

  component/Lifecycle

  (start [this]
    (let [pool (cp/make-datasource options)]
      (assoc this
             :pool pool
             :spec {:datasource pool})))

  (stop [this]
    (cp/close-datasource pool)
    (assoc this
           :pool nil
           :spec nil))

  IDB

  (get-spec [this]
    spec)

  (set-spec [this spec]
    (assoc this :spec spec))

  (query [this sql-params]
    (jdbc/query spec sql-params))

  (update! [this table set-map where-clause]
    (jdbc/update! spec
                  table set-map where-clause)))


(defn make-db [pool-options]
  (map->DB {:options pool-options}))


(defmacro with-trx
  [binding & body]
  (let [[comp-trx comp-db & trx-opt] binding]
    `(let [db-spec# (get-spec ~comp-db)]
       (jdbc/with-db-transaction
         [t-conn# db-spec# ~@trx-opt]
         (let [~comp-trx (set-spec ~comp-db t-conn#)]
           ~@body)))))
