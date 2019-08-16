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
     :worker (make-worker worker))))

#_
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
(component/system-map
 :server (make-server jetty)
 :db     (make-db pool)
 :worker (make-worker worker))

#_
(component/system-map
 :server  (make-server jetty)
 :storage (make-db pool)
 :worker  (component/using
           (make-worker worker)
           {:db :storage}))

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
  {:features {:worker true}

   :jetty {:join? false
           :port 8088}

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


#_
(defn make-system
  [config]
  (let [{:keys [features jetty pool worker]} config

        components
        (cond-> [:server (make-server jetty)
                 :db     (make-db pool)]

          (:worker features)
          (conj :worker (make-worker worker)))]

    (apply component/system-map components)))


;; (def config _c)
;; (def sys-init (make-system config))
;; (def sys-started (component/start sys-init))
;; (def sys-stopped (component/stop sys-started))

#_
(def s1 (component/start s0))

#_
(def s2 (component/stop s1))


(defn my-system-start
  [config]

  (let [{db-opt :pool
         worker-opt :worker} config

        db (-> db-opt
               make-db
               component/start)

        worker (-> worker-opt
                   make-worker
                   (assoc :db db) ;; note
                   component/start)]

    {:db db
     :worker worker}))


(defn my-system-stop
  [system]
  (-> system
      (update :worker component/stop)
      (update :db component/stop)))


;; (def _sys (my-system-start {:pool {} :worker {}}))
;; (my-system-stop _sys)


(defonce ^:private system nil)

(def alter-system (partial alter-var-root #'system))

(defn system-init [config]
  (alter-system (constantly (make-system config))))

(defn system-start []
  (alter-system component/start))

(defn system-stop []
  (alter-system component/stop))


#_
(with-handler :term
  (log/info "caught SIGTERM, quitting")
  (system-stop)
  (log/info "all components shut down")
  (exit))
#_
(with-handler :int
  (log/info "caught SIGINT, quitting")
  (system-stop)
  (log/info "all components shut down")
  (exit))
#_
(with-handler :hup
  (log/info "caught SIGHUP, reloading")
  (system-stop)
  (system-start)
  (log/info "system reloaded"))


#_
(component/system-map
 ;; ...
 :worker (component/using
          (make-worker worker) [:db]))


{:features {:worker true}
 :jetty {:join? false :port 8088}
 ;; etc
}
