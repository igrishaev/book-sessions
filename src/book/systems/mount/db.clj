(ns book.systems.mount.db
  (:require
   [mount.core :as mount :refer [defstate]]
   [hikari-cp.core :as cp]
   [book.systems.mount.config :refer [config]]))


#_
(ns book.systems.mount.db
  (:require
   [book.systems.mount.config :refer [config]]))


#_
(defstate ^{:on-reload :noop}
  db

  :start
  (let [{pool-opt :pool} config
        store (cp/make-datasource pool-opt)]
    {:datasource store})

  :stop
  (-> db :datasource cp/close-datasource))


(defstate db
  :start
  (let [{pool-opt :pool} config
        store (cp/make-datasource pool-opt)]
    {:datasource store})
  :stop
  (-> db :datasource cp/close-datasource))


;; (jdbc/get-by-id db :users 42)
;; (jdbc/insert! db :users {:name "Ivan" :email "ivan@test.com"})



;; (mount/start)
;; (require '[clojure.java.jdbc :as jdbc])
;; (jdbc/query db "select 42 as answer")
;; ({:answer 42})

#_
(def datasource-options
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
