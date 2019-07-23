(ns book.systems.mount.db
  (:require
   [mount.core :as mount :refer [defstate]]
   [hikari-cp.core
    :refer [make-datasource close-datasource]]
   [book.systems.mount.config :refer [config]]))


(defstate ^{:on-reload :noop}
  db

  :start
  (let [{pool-opt :pool} config
        store (make-datasource pool-opt)]
    {:datasource store})

  :stop
  (-> db :datasource close-datasource))


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
