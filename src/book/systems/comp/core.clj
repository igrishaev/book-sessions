(ns book.systems.comp.core
  (:require
   [com.stuartsierra.component :as component]

   [book.systems.comp.server :refer [make-server]]
   [book.systems.comp.worker :refer [make-worker]]
   [book.systems.comp.db :refer [make-db]]))


(defn make-system
  [config]
  (let [{:keys [jetty pool worker]} config]
    (component/system-map
     :server (make-server jetty)
     :db     (make-db pool)
     :worker (component/using
              (make-worker worker)
              [:db]))))


#_
(defn make-system
  [config]
  (let [{:keys [jetty pool worker]} config]
    (component/system-map
     :server (make-server jetty)
     :db     (make-db pool)
     :worker (-> worker
                 make-worker
                 (component/using [:db])))))


(def _c
  {:jetty {:join? false
           :port  8088}

   :pool {:auto-commit        true
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

   :worker {:sleep 1000}})


(def s0 (make-system _c))

;; (def s1 (component/start s0))
;; (def s2 (component/stop s1))
