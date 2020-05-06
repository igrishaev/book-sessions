(ns book.util-test
  (:require

   [clojure.spec.alpha :as s]
   #_
   [clojure.spec.gen.alpha :as gen]

   [ring.middleware.json :refer [wrap-json-response]]
   [ring.middleware.params :refer [wrap-params]]
   [ring.middleware.keyword-params :refer [wrap-keyword-params]]
   [ring.adapter.jetty :refer [run-jetty]]

   [ring.mock.request :as mock]

   [migratus.core :as migratus]

   [clojure.java.jdbc :as jdbc]

   [etaoin.api :as e]

   [book.util :refer [->fahr]]
   [book.views :refer [view-main-page app]]
   [clojure.test :refer [deftest testing is are use-fixtures]])

  (:import java.io.FileInputStream
           java.util.zip.GZIPInputStream
           org.postgresql.copy.CopyManager)

  )





;; (ns ^:integration
;;   book.integration-test
;;   (:require ...))

;; (deftest test-some-case
;;   ...)


(defn aaa [t]
  (println "AAA")
  (t))


#_
(use-fixtures :each aaa)

(deftest test-fahr
  (is (= (int (->fahr 20)) 99999))


  #_
  (is (= (int (->fahr 100)) 212)))





(deftest test-fahr
  (are [c f] (= c (int (->fahr f)))
    68 20
    212 100))




(defn api-create-user
  [params]
  {:status 200})


(def params-ok {:name "John Smith" :email "john@test.com"})

(def params-variations
  [[{:name nil} "Empty name"]
   [{:name (apply str (repeat 999 "A"))} "Name is too long"]
   [{:email "dunno"} "Wrong email"]
   [{:email nil} "No email"]
   [{:extra 42} "Extra field"]])

(deftest test-api-create-user-bad-params
  (testing "Sending bad parameters"
    (doseq [[params* description] params-variations]
      (testing description
        (let [params (merge params-ok params*)
              response (api-create-user params)
              {:keys [status]} response]
          (is (= 400 status)))))))

#_
(deftest test-fahr-nil
  (is (nil? (->fahr nil))))

(deftest test-fahr-nil2
  (is (nil? (->fahr nil))))

#_
(deftest test-fahr-nil
  (try
    (->fahr nil)
    (catch NullPointerException e
      (is true))))

#_
(deftest test-fahr-nil
  (is (thrown? NullPointerException
               (->fahr 1))))

(deftest test-fahr-nil
  (try
    (->fahr nil)
    (catch Throwable e
      (is true))))

(deftest test-fahr-nil
  (is (thrown-with-msg?
       IllegalArgumentException #"Fahrenheit temperature"
       (->fahr nil))))

(defn square-roots [a b c]
  (let [D (- (* b b) (* 4 a c))]
    (cond
      (pos? D) [(/ (+ (- b) (Math/sqrt D)) (* 2 a))
                (/ (- (- b) (Math/sqrt D)) (* 2 a))]
      (zero? D) [(/ (- b) (* 2 a))]
      (neg? D) nil)))

(deftest test-square-roots
  (testing "Two roots"
    (let [result (square-roots 1 -5 6)]
      (is (= [3 2] (mapv int result)))))
  (testing "One root"
    (let [result (square-roots 1 6 9)]
      (is (= [-3] (mapv int result)))))
  (testing "No roots"
    (is (nil? (square-roots 2 4 7)))))


#_
(deftest test-some-api
  (testing "API call"
    ...
    (testing "HTTP response"
      ...))
  (testing "DB checks"
    ...
    (testing "user fields"
      ...)
    (testing "password is hashed"
      ...)))


;; (require '[clojure.test :refer [test-vars]])
;; (require '[clojure.test :refer [run-tests]])
;; (require '[clojure.test :refer [run-all-tests]])

(require '[clojure.java.jdbc :as jdbc])

(def db {:dbtype "postgres" :dbname "book"
         ;; other JDBC fields go here...
         })

(def user-data {})
(def profile-data {})

(defn fix-db-data [t]
  ;; purge
  (jdbc/execute! db "truncate users cascade;")
  (jdbc/execute! db "truncate orders cascade;")
  ;; write
  (jdbc/insert! db :users user-data)
  (jdbc/insert! db :profile profile-data)
  ;; execute
  (t))

(import 'java.io.File
        'org.apache.commons.io.FileUtils)

(defn fix-clear-files [t]
  (t)
  (FileUtils/cleanDirectory (new File "/tmp/tests/reports")))

(defn fix-db-server [t])

,;; (use-fixtures :once fix-db-server fix-clear-files)
;; (use-fixtures :each fix-db-data)

#_
(defonce ^:dynamic ^File *file* nil)



(defn fix-multi [t]
  (t) (t) (t))

;; (use-fixtures :each fix-multi)
;; (use-fixtures :once)

;; (defn fix-mute [t])
;; (use-fixtures :each fix-mute)


;; (defn fix-this-is-fine [t]
;;   (is true))

;; (use-fixtures :each fix-this-is-fine)


(deftest test-1984
  (is (= (* 2 2) 5)))


(deftest ^:special test-special-case
  (is true))


(deftest ^:special ^:backend ^:no-db
  test-special-case
  (is true))


;; :aliases {:test
;;           {:extra-paths ["test"]
;;            :extra-deps {com.cognitect/test-runner
;;                         {:git/url "https://github.com/cognitect-labs/test-runner.git"
;;                          :sha "209b64504cb3bd3b99ecfec7937b358a879f55c1"}}
;;            :main-opts ["-m" "cognitect.test-runner"]}}


#_
(defn fix-pg-only [t]
  (when (= (:dbtype *db*) "postgresql")
    (t)))

#_
(defn fix-pg-only [t]
  (let [{:keys [dbtype]} *db*]
    (if (= dbtype "postgresql")
      (t)
      (is false (str "Unsupported DB " dbtype)))))

#_
(defn fix-mac-only [t]
  (when (= (System/getProperty "os.name") "Mac OS X")
    (t)))

#_
(defn fix-mac-only [t]
  (if (= (System/getProperty "os.name") "Mac OS X2")
    (testing "AAA"

      (t))
    (testing "Wrong OS"
      (is false))
    ))

;; (use-fixtures :each fix-mac-only)



(defn fix-mac-only [t]
  (let [os (System/getProperty "os.name")]
    (if (= os "Mac OS X2")
      (testing (format "OS: %s" os)
        (t))
      (testing (format "Unsupported OS: %s" os)
        (is false)))))


#_
(use-fixtures :each fix-mac-only)



(deftest ^{:pg/version 11}
  test-some-db-test
  (is true))


#_
(defn with-fix-tmp-file [t]
  (let [^File tmp-file (TmpFile/createFile "....")]
    (binding [*file* tmp-file]
      (t))
    (.delete tmp-file)))

#_
(use-fixture :each with-fix-tmp-file)

#_
(import 'some.path.PngImage)

#_
(deftest test-plot-chart-png
  (let [dataset [[...] [...] [...]]
        filepath (.getAbsPath *file*)]
    (plot-chart dataset filepath)
    (let [png (new PngImage *file*)
          width (.getWidth png)
          height (.getHeight png)
          size (.getFileSize png)])
    (is (pos? size))
    (is (= [640 480] [width height]))))

#_
(defonce ^:dynamic ^Session *db* nil)

#_
(defn with-fix-db [t]
  (let [cluster ...
        session ...]
    (binding [*db* session]
      (t))
    (.close session)
    (.close cluster)))

#_
(use-fixtures :once with-fix-db)


#_
(defn fix-db-prepare-data [t]
  (alia/execute! *db* "truncate project.users;")
  (alia/execute! *db* "truncate project.orders;")
  (alia/execute! *db* "insert into project.users (name, email) values (%s, %s)" user-data)
  (t))

#_
(use-fixture :each fix-db-prepare-data)


(defn fix-factory [type number]
  (fn [t]
    (println (format "%s %s starts" type number))
    (t)
    (println (format "%s %s ends" type number))))


;; :test-selectors {:default (complement :integration)
;;                  :integration :integration
;;                  :all identity}


;; :test-selectors {:special :special
;;                  :backend :backend}



#_
(use-fixtures :once
  (fix-factory :once 1)
  (fix-factory :once 2))

#_
(use-fixtures :each
  (fix-factory :each 3)
  (fix-factory :each 4))


(def db-pg {:dbtype "postgresql" :host "..."})
(def db-mysql {:dbtype "mysql" :host "..."})

(defonce ^:dynamic *db* nil)

(defn fix-multi-db-backend [t]
  (doseq [db [db-pg db-mysql]]
    (binding [*db* db]
      (testing (format "Testing with DB: %s" (:dbtype *db*))
        (t)))))

#_
(defn test-get-user-by-id
  (let [user (orm/get-user *db* 1)]
    (is (= user {:name "Ivan"}))))


#_
(defmacro deftest
  [name & body]
  (when *load-tests*
    `(def ~(vary-meta name assoc :test `(fn [] ~@body))
       (fn [] (test-var (var ~name))))))

#_
"
lein test book.util-test

Ran 1 tests containing 2 assertions.
0 failures, 0 errors.
"

#_
"

$ lein test

lein test :only book.util-test/test-fahr-nil

ERROR in (test-fahr-nil) (Numbers.java:3849)
expected: (nil? (->fahr nil))
  actual: java.lang.NullPointerException: null
 at clojure.lang.Numbers.multiply (Numbers.java:3849)
    book.util$__GT_fahr.invokeStatic (util.clj:5)
    book.util$__GT_fahr.invoke (util.clj:4)
    book.util_test$fn__370.invokeStatic (util_test.clj:10)
    book.util_test/fn (util_test.clj:9)
    ... <truncated>

Ran 2 tests containing 3 assertions.
0 failures, 1 errors.
Tests failed.

"


;; :once 1 starts
;;  :once 2 starts
;;   :each 3 starts   +
;;    :each 4 starts  |
;;    :each 4 ends    |
;;   :each 3 ends     +
;;   :each 3 starts   +
;;    :each 4 starts  |
;;    :each 4 ends    |
;;   :each 3 ends     +
;;   :each 3 starts   +
;;    :each 4 starts  |
;;    :each 4 ends    |
;;   :each 3 ends     +
;;   :each 3 starts   +
;;    :each 4 starts  |
;;    :each 4 ends    |
;;   :each 3 ends     +
;;  :once 2 ends
;; :once 1 ends



#_
(with-redefs
  [book.views/get-sites-by-location (constantly {:foo :bar})
   book.views/get-events-by-location (constantly {:aaa :bbb})]
  (view-main-page {}))



(defmacro with-mock
  [path result & body]
  `(with-redefs
     [~path (fn [& ~'_] ~result)]
     ~@body))


#_
(deftest test-sites-ok-events-err
  (with-mock book.views/get-sites-by-location []
    (with-mock book.views/get-events-by-location
      (throw (new java.net.UnknownHostException "DNS error"))
      (let [response (view-main-page {})]
        (is (:body response) {})))))

#_
(with-mock book.views/get-sites-by-location (throw (new Exception "AAAA"))
  (view-main-page {})
  )


#_
(with-mock book.views/get-sites-by-location {:foo :bar}
  (with-mock book.views/get-events-by-location {:aaa :bbb}
    (view-main-page {})))

#_
(with-mock book.views/get-sites-by-location
  (throw (new Exception "AAA"))
  (with-mock book.views/get-events-by-location {:aaa :bbb}
    (view-main-page {})))


#_
(deftest test-main-page
  (let [request {:params {:lat 55.751244
                          :lon 37.618423}}
        result (view-main-page request)]
    (is (= (:body result)
           {:events [...] :sites [...]}))))


#_
(deftest test-main-page
  (let [sites [{:name "Cafe1"} {:name "Cafe2"}]
        events [{:name "Event1"} {:name "Event2"}]]
    (with-redefs
      [book.views/get-sites-by-location (constantly sites)
       book.views/get-events-by-location (constantly events)]
      (let [request {:params {:lat 55.751244
                              :lon 37.618423}}
            result (view-main-page request)]
        (is (= (:body result)
               {:sites sites :events events}))))))


;; (defn fix-mock-main-view [t]
;;   (with-redefs [...]
;;     (t)))

;; (use-fixtures :each fix-mock-main-view)


#_
(def data-events
  (-> "data/events.json"
      clojure.java.io/resource
      slurp
      (cheshire.core/parse-string true)))



#_
(defn sites-handler*
  [request]
  (let [{:keys [uri]} request]

    (case uri
      "/search/v1/"

      #_
      {:status 200
       :body (-> "data/events.json"
                 clojure.java.io/resource
                 clojure.java.io/file)}

      {:status 200
       :body [{:name "Cafe1" :address "..."}
              {:name "Cafe2" :address "..."}]}

      ;; else
      {:status 404
       :body "page not found"})))


#_
(defn sites-handler* [{:keys [uri]}]
  (case uri
    "/search/v1/"
    {:status 200
     :body [{:name "Cafe1" :address "..."}
            {:name "Cafe2" :address "..."}]}
    {:status 404
     :body "page not found"}))

(defn sites-handler* [request]
  (let [{:keys [uri params]} request
        {:keys [lat lon]} params]
    (case uri
      "/search/v1/"
      (case [lat lon]
        ["0" "0"]   {:status 200 :body []}
        ["66" "66"] {:status 403 :body {:error "ACCESS_ERROR"}}
        ["42" "42"] (do (Thread/sleep (* 1000 35))
                        {:status 200 :body []})
        {:status 200
         :body [{:name "Cafe1" :address "..."}
                {:name "Cafe2" :address "..."}]})
      {:status 404 :body "page not found"})))


(def sites-handler
  (-> sites-handler*
      wrap-keyword-params
      wrap-params
      wrap-json-response))


#_
(defn fix-fake-sites-server [t]
  (let [opt {:port 8808 :join? false}
        server (run-jetty sites-handler opt)]
    (t)
    (.stop server)))

(defmulti multi-handler :foo)

(defmethod multi-handler :page-save
  [request]
  (let [{:keys [params route-params]} request
        {order-id :id} route-params
        fields (select-keys params [:title :description :price])]
    (jdbc/update! *db* :orders params ["id = ?" order-id])
    {:status 302
     :headers {:Location (format "/content/order/%s/view" order-id)}}))


(def render-order-page)

(def get-order-by-id)

(def response-404)

(defmethod multi-handler :page-view
  [request]
  (if-let [order (-> request :route-params :id get-order-by-id)]
    {:status 200
     :headers {:content-type "text/html"}
     :body (render-order-page {:order order})}
    response-404))

(defonce ^:dynamic *server* nil)

#_
(defn fix-fake-sites-server [t]
  (let [opt {:port 8808 :join? false}]
    (binding [*server* (run-jetty sites-handler opt)]
      (t)
      (.stop *server*))))

#_
(use-fixtures :once fix-fake-sites-server)


#_
(deftest test-main-page
  (let [request {:params {:lat 55.751244
                          :lon 37.618423}}
        result (view-main-page request)]
    (is (= (:body result) {}))))


#_
(deftest test-the-website-is-down
  (.stop *server*)
  (let [request {:params {:lat 1 :lon 2}}
        result (view-main-page request)]
    (is (= (:body result) {})))
  (.start *server*))


#_
(ns user
  (:require [migratus.core :as migratus]))

;; (def config {...})
;; (migratus/migrate config)


;; (ns user
;;   (:require [migratus.core :as migratus]))
;; (def config {...})
;; (migratus/migrate config)



(def ^:dynamic *db*
  {:dbtype "postgresql"
   :dbname "test"
   :host "127.0.0.1"
   :user "ivan"
   :password "ivan"
   :assumeMinServerVersion "10"})


#_
(def db-data
  [[:users {:name "Ivan" :email "ivan@test.com"}]
   [:users {:name "Juan" :email "Juan@test.com"}]
   [:groups {:name "Dog fans" :topics 6}]
   [:groups {:name "Cat fans" :topics 7}]])


#_
(defn load-data []
  (doseq [[table row] db-data]
    (jdbc/insert! *db* table row)))


#_
(defn with-db-data [t]
  (load-data) (t))


(def db-data
  [[:users [{:name "Ivan" :email "ivan@test.com"}
            {:name "Juan" :email "Juan@test.com"}]]
   [:groups [{:name "Dog fans" :topics 6}
             {:name "Cat fans" :topics 7}]]])


(defn load-data []
  (doseq [[table rows] db-data]
    (jdbc/insert-multi! *db* table rows)))


#_
(jdbc/execute! *db* "COPY users(name,email) FROM '/Users/ivan/work/book/env/test/resources/data/users.csv' DELIMITER ',' CSV HEADER")


#_
(jdbc/execute! *db*
  "
  COPY users(name,email)
  FROM '/Users/ivan/work/book/env/test/resources/data/users.csv'
  DELIMITER ',' CSV HEADER
  ")

(defn load-data-gz []
  (let [conn (jdbc/get-connection *db*)
        copy (CopyManager. conn)
        stream (-> "data/users.csv.gz"
                   clojure.java.io/resource
                   clojure.java.io/file
                   FileInputStream.
                   GZIPInputStream.)]
    (.copyIn copy "COPY users(name, email)
                   FROM STDIN (FORMAT CSV, HEADER true)"
             stream)
    (.close conn)))




#_(jdbc/insert! *db* :groups {:id 3 :name "Clojure fans"})
#_(jdbc/insert! *db* :users {:group_id 3 :name "Ivan"})


#_
(defn foo []
  (let [config {:store :database
                :migration-dir "migrations"
                :db *db*}]


    (migratus/init config)
    (migratus/pending-list config)
    ))


(def id-user-admin 1)

(def db-data
  [[:users [{:id id-user-admin :name "Ivan"}]]
   [:profiles [{:id 1 :user_id id-user-admin :avatar "..."}]
    :posts [{:id 1 :user_id id-user-admin :title "New book"}
            {:id 2 :user_id id-user-admin :title "Some post"}]]])


#_
(let [tables (set (map first db-data))
      query "TRUNCATE %s CASCADE"]
  (doseq [table tables]
    (jdbc/execute! *db* (format query (name table)))))


(defn delete-data []
  (let [tables (set (map first db-data))
        tables-comma (clojure.string/join "," (map name tables))
        query (format "TRUNCATE %s CASCADE" tables-comma)]
    (jdbc/execute! *db* query)))


#_
(let [value 100
      tables (set (map first db-data))
      query "ALTER SEQUENCE %s_id_seq RESTART WITH %s"]
  (doseq [table tables]
    (jdbc/execute! *db* (format query (name table) value))))


;; user
^{:entity :users/admin} {:name "Ivan" :email "ivan@test.com"}
;; profile
{:user_id :users/admin :avatar "/images/ivan.png"}
;; posts
{:user_id :users/admin :title "New book"}
{:user_id :users/admin :title "Some post"}


;; (jdbc/insert! *db* :users {:name "Ivan"})
;; ({:id 42 :name "Ivan}) ;; for PostgreSQL
;; ({:generated_key 42})  ;; for MariaDB




"alter sequence users_id_seq restart with 100"

"
ERROR:  duplicate key value violates unique constraint 'groups_pkey'
DETAIL:  Key (id)=(3) already exists.
"


"
BEGIN;
INSERT INTO users ...
INSERT INTO profiles ...
UPDATE users SET ...
ROLLBACK;
"


(defmacro with-db-rollback
  [[t-conn & bindings] & body]
  `(jdbc/with-db-transaction [~t-conn ~@bindings]
     (jdbc/db-set-rollback-only! ~t-conn)
     ~@body))


#_
(with-db-rollback
  [tx *db*]
  (println "Inserting the data...")
  (jdbc/insert! tx :users {:name "Ivan"})
  (let [...]
    (do-something)))

#_
(defn with-db-data [t]
  (with-db-rollback [tx *db*]
    ()

    )


  (load-data) (t))


#_
(deftest test-logic-with-rollback
  (with-db-rollback [tx *db*]
    (load-data tx)
    (let [user (get-user-by-name tx "Ivan")]
      (is (= "Ivan" (:name user))))))

#_
(deftest test-logic-with-rollback
  (with-db-rollback [tx *db*]
    (binding [*db* tx]
      (load-data)
      (let [user (get-user-by-name "Ivan")]
        (is (= "Ivan" (:name user)))))))


(deftest test-app-index
  (let [request {:request-method :get :uri "/"}
        response (app request)
        {:keys [status body]} response]
    (is (= 200 status))))

(deftest test-app-page-not-found
  (let [request {:request-method :get :uri "/missing"}
        response (app request)
        {:keys [status body]} response]
    (is (= 404 status))))


#_
(do

  (mock/request :get "/test")

  (mock/request :get "/movies" {:search "batman" :page 1})

  (mock/request :post "/users" {:name "Ivan" :email "test@test.com"})

  (-> (mock/request :post "/users")
      (mock/json-body {:name "Ivan" :email "test@test.com"}))

  (sites-handler (mock/request :get "/search/v1/" {:lat 11 :lon 22}))


  (let [request (mock/request :get "/search/v1/" {:lat 11 :lon 22})
        response (sites-handler request)
        body (-> response :body (cheshire.core/parse-string true))]
    (is (= {:some :data} body))))


#_
{:sites [{:name "Site1" :date-updated (new Date) :id 42}]}


#_
(let [body* (update body :sites
                    (fn [sites]
                      (map (fn [site]
                             (dissoc site :id :date-updated))
                           sites)))]
  (is (= {} body*)))


;; {:sites ({:name "Site1"} {:name "Site2"})}



;; (defn fix-system [t]
;;   (system/start!)
;;   (t)
;;   (system/stop!))


;; (defn fix-db-data [t]
;;   (let [{:keys [db]} system/system]
;;     (prepare-test-data db)
;;     (t)
;;     (clear-test-data db)))

;; (use-fixtures :once fix-system fix-db-data)


;; (def state-field ::started?)


;; (defn stop! []
;;   (let [sys (-> system
;;                 component/stop-system
;;                 (with-meta {state-field false}))]
;;     (alter-var-root #'system (constantly sys))))


;; (defn start! []
;;   (let [sys (-> system
;;                 component/start-system
;;                 (with-meta {state-field true}))]
;;     (alter-var-root #'system (constantly sys))))


;; (defn started? []
;;   (some-> system meta (get state-field)))


;; (defn fixture-system [t]
;;   (let [started-manually? (system/started?)]
;;     (when-not started-manually?
;;       (start!))
;;     (t)
;;     (when-not started-manually?
;;       (stop!))))


#_
(deftest test-ui-login-ok
  (e/with-chrome {} driver
    (e/go driver "http://127.0.0.1:8080/login")
    (e/wait-visible driver {:fn/has-text "Login"})
    (e/fill-human driver {:tag :input :name :email} "test@test.com")
    (e/fill-human driver {:tag :input :name :password} "J3QQ4-H7H2V")
    (e/click driver {:tag :button :fn/text "Login"})
    (e/wait-visible driver {:fn/has-text "Welcome"})
    (is (e/visible? driver {:tag :a :fn/text "My profile"}))
    (is (e/visible? driver {:tag :button :fn/text "Logout"}))))


    ;; (e/click driver {:tag :button :fn/text "Logout"})
    ;; (e/wait-visible driver {:fn/has-text "Bye!"})
    ;; (e/wait-visible driver {:fn/has-text "Login"})


(defonce ^:dynamic *driver* nil)

(defn fix-chrome [t]
  (e/with-chrome {} driver
    (binding [*driver* driver]
      (t))))

(defn fix-multi-driver [t]
  (doseq [driver-type [:firefox :chrome]]
    (e/with-driver driver-type {} driver
      (binding [*driver* driver]
        (testing (format "Browser %s" (name driver-type))
          (t))))))


#_
(e/with-chrome {} driver
  (doto driver
    (e/go "http://127.0.0.1:8080/login")
    (e/wait-visible {:fn/has-text "Login"})
    (e/fill-human {:tag :input :name :email} "test@test.com")
    (e/fill-human {:tag :input :name :password} "J3QQ4-H7H2V")
    (e/click {:tag :button :fn/text "Login"}))

  (is (e/visible? driver {:tag :a :fn/text "My profile"}))
  (is (e/visible? driver {:tag :button :fn/text "Logout"})))


#_
(defn fix-login-logout [t]
  (doto *driver*
    (e/go "http://127.0.0.1:8080/login")
    (e/fill {:tag :input :name :email} "test@test.com")
    (e/click {:tag :button :fn/text "Login"}))
  (t)
  (doto *driver*
    (e/click {:tag :button :fn/text "Logout"})
    (e/wait-has-text "Login")))


#_

(with-mock mock
  {:target :project.path/get-geo-point
   :return {:lat 14.23 :lng 52.52}}
  (get-geo-point "cafe" "200m"))


#_

{:called? true
 :call-count 3
 :call-args '(1 2 3)
 :call-args-list '[(1) (1 2) (1 2 3)] ;; args history
 :return 42              ;; the last result
 :return-list [42 42 42] ;; the entire result history
 }

#_


(let [{:keys [called? call-count call-args]} @mock]
  (is called?)
  (is (= 1 call-count))
  (is (= '("cafe" "200m") call-args)))


;; (defn adder [x y] (+ x y))
;; (def spy-adder (spy/spy adder))
;; (testing "calling the function"
;;   (is (= 3 (spy-adder 1 2))))
;; (testing "calls to the spy can be accessed via spy/calls"
;;   (is (= [[1 2]] (spy/calls spy-adder))))


#_
(facts "about `split`"
 (str/split "a/b/c" #"/") => ["a" "b" "c"]
 (str/split "" #"irrelvant") => [""])


;; :plugins [[test2junit "1.1.2"]]
;; :test2junit-output-dir "target/test2junit"

#_
(require '[clojure.test.check.generators :as gen])

#_
(defrecord User [user-name user-id active?])

#_
(def user-gen
  (gen/fmap (partial apply ->User)
            (gen/tuple (gen/not-empty gen/string-alphanumeric)
                       gen/nat
                       gen/boolean)))

;; (last (gen/sample user-gen))
;; => #user.User{:user-name "kWodcsE2"
;;               :user-id 1
;;               :active? true}




(s/def :user/id int?)
(s/def :user/name string?)
(s/def :user/active? boolean?)
(s/def ::user (s/keys :req-un [:user/id :user/name :user/active?]))

#_
(gen/generate (s/gen ::user))

(deftest test-aaa
  (is (= 5 (+ 2 2)))
  )




"

;; wrong                ;; correct
(is (= status 200))     (is (= 200 status))

Fail in test-...        Fail in test-...
expected: 404           expected: 200
  actual: 200             actual: 404
    diff: - 404             diff: - 200
          + 200                   + 404
"







(import 'javax.imageio.ImageIO)

#_
(deftest test-plot-chart-png
  (let [dataset [[...] [...] [...]]
        filepath (.getAbsPath *file*)]
    (plot-chart dataset filepath)
    (let [png (ImageIO/read *file*)
          width (.getWidth png)
          height (.getHeight png)])
    (is (= [640 480] [width height]))))

#_
(defn restart-db-ids []
  (let [value 100
        tables (set (map first db-data))
        query "ALTER SEQUENCE %s_id_seq RESTART WITH %s"]
    (doseq [table tables]
      (jdbc/execute! *db* (format query (name table) value)))))

#_
(defn get-key-from-insert [result]
  (let [[row] result
        {:keys [id generated_key]} row]
    (or id generated_key)))

#_
(defn fix-sites [body]
  (update body :sites
          (fn [sites]
            (for [site sites]
              (dissoc site :id :date-updated)))))


#_
{:sites [{:name "Site1" :date-updated "2019-11-12" :id 42}
         {:name "Site2" :date-updated "2019-11-10" :id 99}]}


#_
(require '[spy.core :as spy]
         '[spy.assert :as assert])


(require '[spy.assert :as assert])


#_(do

    (require '[spy.core :as spy])

    (def spy+ (spy/spy +))

    (map spy+ [1 2 3] [4 5 6])

    (spy/calls spy+)
    [(1 4) (2 5) (3 6)]

    (spy/responses spy+)
    [5 7 9])


#_(do

    (deftest test-config-error-die-fn-called
      (let [spy-die-fn (spy/spy)]
        (load-config {:program-name :test
                      :spec ::broken-config
                      :die-fn spy-die-fn})
        (assert/called? spy-die-fn))))
