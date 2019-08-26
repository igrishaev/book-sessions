(ns book.integrant
  (:require
   [integrant.core :as ig]
   [clojure.java.jdbc :as jdbc]
   [clj-http.client :as client]
   [clojure.tools.logging :as log]
   [ring.adapter.jetty :refer [run-jetty]]
   [hikari-cp.core :as cp]))

(defn tag-env
  [varname]
  (cond
    (symbol? varname)
    (System/getenv (name varname))
    (string? varname)
    (System/getenv varname)
    :else
    (throw (new Exception "wrong var type"))))


#_


(defn load-config
  [filename]
  (ig/read-string {:readers {'env tag-env}}
                  (slurp filename)))


#_
(def config
  (-> "config.edn" slurp ig/read-string))


#_
(def config
  {:project.server/server {:options {:port 8080 :join? false}
                           :handler (ig/ref :project.handlers/index)}
   :project.db/db {}
   :project.worker/worker {:options {:sleep 1000}
                           :db (ig/ref :project.db/db)}
   :project.handlers/index {:db (ig/ref :project.db/db)}})

#_
(ig/load-namespaces config)

#_
{::db {:auto-commit   true
       :adapter       "postgresql"
       :username      "book"
       :password      "book"
       :database-name "book"
       :server-name   "127.0.0.1"
       :port-number   5432}}

(def config
  {::server {:options {:port 8080 :join? false}
             :handler (ig/ref ::handler)}

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
             :db (ig/ref ::db)}

   ::handler {:db (ig/ref ::db)}})


(require '[clojure.spec.alpha :as s])


(def app (constantly {:status 200 :body "Hello"}))


(s/def :db/username string?)

(defmethod ig/pre-init-spec ::db
  [_]
  (s/keys :req-un [:db/username
                   :db/password
                   :db/database-name
                   :db/server-name
                   :db/port-number]))


(defmethod ig/init-key ::handler
  [_ {:keys [db]}]
  (fn [request]
    (let [query "select count(*) as total from requests"
          result (jdbc/query db query)
          total (-> result first :total)]
      {:status 200
       :body (format "You've got %s requests." total)})))


(defmethod ig/init-key ::server
  [_ {:keys [handler options]}]
  (run-jetty handler options))

(defmethod ig/halt-key! ::server
  [_ server]
  (.stop server))


(def db-defaults
  {:auto-commit        false
   :read-only          false
   :connection-timeout 30000
   :validation-timeout 5000
   :idle-timeout       600000
   :max-lifetime       1800000
   :minimum-idle       10
   :maximum-pool-size  10})


(defmethod ig/prep-key ::db
  [_ options]
  (merge db-defaults options))


(defmethod ig/init-key ::db
  [_ options]
  {:datasource (cp/make-datasource options)})


(defmethod ig/halt-key! ::db [_ db-spec]
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


(defmethod ig/init-key ::worker
  [_ {:keys [db options]}]
  (let [flag (atom true)
        task (make-task db flag options)]
    {:flag flag :task task}))


(defmethod ig/halt-key! ::worker
  [_ {:keys [flag task]}]
  (reset! flag false)
  (while (not (realized? task))
    (Thread/sleep 300)))



(defonce ^:private system nil)

(def alter-system (partial alter-var-root #'system))

(defn system-start [config]
  (alter-system (constantly (ig/init config))))

(defn system-stop []
  (alter-system ig/halt!))


#_
(cond-> sys-config
  (is-worker-supported?)
  (assoc ::worker {:options {:sleep 1000}
                   :db (ig/ref ::db)}))


#_
(let [components (-> config keys set)
      components (cond-> components
                   (not (is-worker-supported?))
                   (disj ::worker))]
  (ig/init config components))

#_
(ns project.system
  (:require
   project.db
   project.server
   project.worker
   project.utils.queue
   ;; etc
   ))

(derive ::db-master ::db)
(derive ::db-replica ::db)

(def config2
  {::server {:options {:port 8080 :join? false}
             :handler (ig/ref ::handler)}

   ::db-master {:auto-commit        true
                :read-only          false
                :connection-timeout 30000
                :validation-timeout 5000
                :idle-timeout       600000
                :max-lifetime       1800000
                :minimum-idle       10
                :maximum-pool-size  10
                :pool-name          "book-pool-master"
                :adapter            "postgresql"
                :username           "book"
                :password           "book"
                :database-name      "book"
                :server-name        "127.0.0.1"
                :port-number        5432}

   ::db-replica {:auto-commit        false
                 :read-only          true
                 :connection-timeout 30000
                 :validation-timeout 5000
                 :idle-timeout       600000
                 :max-lifetime       1800000
                 :minimum-idle       10
                 :maximum-pool-size  10
                 :pool-name          "book-pool-slave"
                 :adapter            "postgresql"
                 :username           "book"
                 :password           "book"
                 :database-name      "book"
                 :server-name        "127.0.0.1"
                 :port-number        5432}

   ::sync {:dbs (ig/refset ::db)}

   ::worker {:options {:sleep 1000}
             :db (ig/ref ::db-master)}

   ::handler {:db (ig/ref ::db-replica)}})


(defmethod ig/init-key ::sync
  [_ opt] opt)



#_
(def config
  {::server {:options {:port 8080 :join? false}
             :handler (ig/ref ::handler)}
   ::db-master { ;; other fields
                :read-only false}
   ::db-replica { ;; other fields
                 :read-only true}
   ::worker {:options {:sleep 1000}
             :db (ig/ref ::db-master)}
   ::handler {:db (ig/ref ::db-replica)}})
