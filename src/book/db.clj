(ns book.db
  (:require

   [clojure.data.csv :as csv]
   [clojure.java.io :as io]
   [clojure.spec.alpha :as s]

   [hugsql.core :as hugsql]

   [cheshire.core :as json]
   [hikari-cp.core :as cp]
   [mount.core :as mount :refer [defstate]]
   [clojure.java.jdbc :as jdbc]
   [com.stuartsierra.component :as component]
   [clojure.tools.logging :as log])

  (:import org.joda.time.DateTime))


;; [org.clojure/java.jdbc "0.7.8"]
;; [org.postgresql/postgresql "42.1.3"]


(def db {:dbtype "postgresql"
         :dbname "test"
         :host "127.0.0.1"
         :user "book"
         :password "book"})


(jdbc/query db "select 1 as value")

(jdbc/query db "select * from users")

(jdbc/query db ["select * from users where id = ?" 1])

(jdbc/get-by-id db :users 1)

(jdbc/get-by-id db :users 1 "id")


(jdbc/find-by-keys db :users {:fname "John" :age 25})


(defn find-first [db table filters]
  (first (jdbc/find-by-keys db table filters)))


(find-first db :users {:fname "John" :age 25})


(jdbc/insert! db :users {:fname "Ivan"
                         :lname "Petrov"
                         :email "ivan@test.com"
                         :age 87})

(jdbc/insert! db :users
              [:fname :lname :email :age]
              ["Andy" "Stone" "andy@test.com" 33])


(jdbc/execute! db "insert into users (id) values (default)")


#_
({:value 1})

#_
({:id 1, :fname "John", :lname "Smith", :email "test@test.com", :age 25})


#_
(let [get-pk
      (some-fn :id :generated_key)

      fields
      {:fname "Ivan"
       :lname "Petrov"
       :email "ivan@test.com"
       :age 87}

      db-result
      (jdbc/insert! db :users fields)]

  (-> db-result first get-pk))


(jdbc/update! db :users {:age 50} ["id = ?" 7])

(jdbc/update! db :users {:age 50} ["true"])


(jdbc/update! db :users {:email "test@test.com"} ["id = ? OR id = ?" 1 3])

(jdbc/update! db :users {:is_active true} ["true"])

(let [[num-updated]
      (jdbc/update! db :users {:is_active true} ["true"])]
  (log/infof "%s records updated" num-updated))


(jdbc/delete! db :users ["age >= ?" 50])



(jdbc/execute! db "create index users_fname on users(fname);")

(jdbc/execute! db "drop index users_fname;")

(jdbc/execute! db "truncate users cascade;")

(jdbc/execute! db "ALTER TABLE users DISABLE TRIGGER on_users_update")



(jdbc/query db "select * from users where id = 13")

(jdbc/query db ["select * from users where id = ?" 13])



(def user-id 13)


(def user-id "13 OR TRUE")

(jdbc/query db (format "select * from users where id = %s" user-id))


(def fname "Robert'; DROP TABLE users;--")
(println (str "select * from users where fname = '" fname "'"))



(def fname "Robert")
(def sql (str "select * from users where fname = '" fname "'"))
(jdbc/query db sql)

"select * from users where fname = 'Robert'; DROP TABLE users;--'"


"http://site.com/users?user-id=13+OR+TRUE"

;; $user_id = 13;
;; $query = sprintf("SELECT * FROM users WHERE id = %s", $user_id);
;; $result = mysql_query($query);


(let [fname (-> request :params "first_name")]
  ...)



(def fname "Robert' UNION select * from users--")
(def sql (str "select * from users where fname = '" fname "'"))
(jdbc/query db sql)



(def movie "Д'Артаньян и три мушкетёра")

(def sql (format "insert into movies (name) values ('%s')" movie))

;; insert into movies (name) values ('Д'Артаньян и три мушкетёра')

(jdbc/query db ["select * from users where id = ?" "1"])

(jdbc/query db ["select * from users where fname = ?" "Д'Артаньян"])

;; select * from users where fname = $1
;; parameters: $1 = 'Д''Артаньян'

(jdbc/query db ["select * from ?" "users"])

(jdbc/query db ["select ?, ?, ? from users" "id" "fname" "email"])




;; 3 + 2 * 2


;; [+ 3 [* 2 2]]


;; select * from users where id = ?

{:type :select
 :tables [:users]
 :fields [:id :fname :lname :email :age]
 :where [[:= :id ?]]
 :params [:integer]}



(def conn
  (jdbc/get-connection db))

(def prep-stmt
  (jdbc/prepare-statement conn "select * from users where id = ?"))

(jdbc/query db [prep-stmt 1])


(def stmt-insert
  (jdbc/prepare-statement conn "insert into users (fname, lname, age, email) values (?, ?, ?, ?)"))

(jdbc/execute! db [stmt-insert "John" "Smith" 20 "john@test.com"])


#_
(jdbc/query db ["select * from users"])


(defprotocol IUserManager
  (get-by-id [this id]))


(defrecord UserManager
    [db
     conn
     stmt-get-by-id]

    component/Lifecycle

    (start [this]
      (let [conn (jdbc/get-connection db)

            stmt-get-by-id
            (jdbc/prepare-statement conn "select * from users where id = ?")]

        (assoc this
               :conn conn
               :stmt-get-by-id stmt-get-by-id)))

    (stop [this]
      (.close conn)
      (.close stmt-get-by-id))

    IUserManager

    (get-by-id [this id]
      (first (jdbc/query conn [stmt-get-by-id id]))))


(def user-mgr
  (-> {:db db}
      map->UserManager
      component/start))



(jdbc/query db "select * from users")
({:id 1
  :fname "Ivan"
  :lname "Petrov"
  :email "test@test.com"
  :age 42}
 {:id 2
  :fname "John"
  :lname "Smith"
  :email "john@test.com"
  :age 20})



(def rs (jdbc/db-query-with-resultset db "select * from users"
                                      (fn [rs]
                                        (while (.next rs)
                                          (println (.getInt rs "id"))
                                          (println (.getString rs "fname"))))))


(println (.getInt rs "id"))
(println (.getString rs "fname"))

(while (.next rs)
  (println (.getInt rs "id"))
  (println (.getString rs "fname")))


(let [...
      records
      (fn thisfn []
        (when (.next rs)
          (cons (zipmap keys (row-values)) (lazy-seq (thisfn)))))]
  (records))


(jdbc/query db "select * from users" {:as-arrays? true})

[[1 "Ivan" "Petrov" "test@test.com"]
 [2 "Ivan" "Petrov" "ivan@test.com"]
 [3 "John" "Smith" "john@test.com"]]

[[:id :fname :lname :email :age]
 [1 "Ivan" "Petrov" "test@test.com" 42]
 [2 "Ivan" "Petrov" "ivan@test.com" 87]
 [3 "John" "Smith" "john@test.com" 20]]




(with-open [reader (io/reader "in-file.csv")]
  (doall
   (csv/read-csv reader)))

(with-open [writer (io/writer "out-file.csv")]
  (csv/write-csv writer
                 [["abc" "def"]
                  ["ghi" "jkl"]]))


;; [clojure.data.csv :as csv]
;; [clojure.java.io :as io]


(with-open [writer (io/writer "users.csv")]
  (->> (jdbc/query db "select * from users" {:as-arrays? true})
       (csv/write-csv writer)))


(jdbc/query db "select * from users"
            {:as-arrays? true :keywordize? false})

(jdbc/query db "select * from users" {:keywordize? false})

({"id" 1
  "fname" "Ivan"
  "lname" "Petrov"
  "email" "test@test.com"
  "age" 42}
 {"id" 3
  "fname" "John"
  "lname" "Smith"
  "email" "john@test.com"
  "age" 20})


(first (jdbc/query db "select * from users" {:qualifier "user"}))

{:user/id 2
 :user/fname "Ivan"
 :user/lname "Petrov"
 :user/email "ivan@test.com"
 :user/age 87}

;; create table users (id serial primary key, fname text, lname text, email text, age integer);
;; create table profiles (id serial primary key, user_id integer not null references users (id), avatar text);



select
u.id as user_id,
u.fname as user_fname,
u.lname as user_lname,
p.avatar as profile_avatar
from users u
join profiles p on p.user_id = u.id;



select
u.id as "user/id",
u.fname as "user/fname",
u.lname as "user/lname",
p.avatar as "profile/avatar"
from users u
join profiles p on p.user_id = u.id

(jdbc/query db "
select
u.id as \"user/id\",
u.fname as \"user/fname\",
...
")


({:user/id 1
  :user/fname "Ivan"
  :user/lname "Petrov"
  :profile/avatar "kitten.jpg"})


select
u.id as "user/id",
u.fname as "user/fname",
u.lname as "user/lname",
p.avatar as "profile/avatar"
from users u
join profiles p on p.user_id = u.id;

 user/id | user/fname | user/lname | profile/avatar
---------+------------+------------+----------------
       1 | Ivan       | Petrov     | kitten.jpg




(jdbc/query db "select * from users"
            {:as-arrays? true
             :keywordize? false
             :result-set-fn
             (fn [rows]
               (with-open [writer (io/writer "users.csv")]
                 (csv/write-csv writer rows)))})


(jdbc/db-query-with-resultset
 db "select * from users"
 (fn [rs]
   (while (.next rs)
     (let [id (.getInt rs "id")
           fname (.getString rs "fname")
           lname (.getString rs "lname")]
       (println id fname lname)))))




(def db {:auto-commit? true
         :dbtype "postgresql"
         :dbname "test"
         :host "127.0.0.1"
         :user "book"
         :password "book"})


(def db {:auto-commit? true
         :dbtype "postgresql"
         ...})


(jdbc/insert! db :users {:fname "Ivan"})
(jdbc/insert! db :users {:fname "Huan"})


BEGIN
INSERT INTO users ( fname ) VALUES ( $1 )
parameters: $1 = 'Ivan'
COMMIT

BEGIN
INSERT INTO users ( fname ) VALUES ( $1 )
parameters: $1 = 'Huan'
COMMIT

(def db {:auto-commit? false
         :dbtype "postgresql"
         :dbname "test"
         :host "127.0.0.1"
         :user "book"
         :password "book"})

(def conn (jdbc/get-connection db))

(def db {:auto-commit? true
         :dbtype "postgresql"
         :dbname "test"
         :host "127.0.0.1"
         :user "book"
         :password "book"
         :connection conn})


(jdbc/insert! db :users {:fname "Ivan"} {:transaction? false})
(jdbc/insert! db :users {:fname "Huan"} {:transaction? false})

(jdbc/execute! db "commit" {:transaction? false})


BEGIN;

INSERT INTO users (fname, email) VALUES ('Ivan', 'ivan@test.com') RETURNING id;

id
----
 59
(1 row)

INSERT INTO profiles (user_id, avatar) VALUES (59, 'facepalm.jpg');

COMMIT;


(jdbc/insert! db :users {:fname "Ivan"})
(jdbc/insert! db :users {:fname "Huan"})


BEGIN
INSERT INTO users ( fname ) VALUES ( $1 )
parameters: $1 = 'Ivan'
COMMIT

BEGIN
INSERT INTO users ( fname ) VALUES ( $1 )
parameters: $1 = 'Huan'
COMMIT


(jdbc/insert! db :users {:fname "Ivan"} {:transaction? false})
(jdbc/insert! db :users {:fname "Huan"} {:transaction? false})


INSERT INTO users ( fname ) VALUES ( $1 )
parameters: $1 = 'Ivan'

INSERT INTO users ( fname ) VALUES ( $1 )
parameters: $1 = 'Huan'


(jdbc/with-db-transaction [tx db]
  (jdbc/insert! tx :users {:fname "Ivan"})
  (jdbc/insert! tx :users {:fname "Huan"}))

BEGIN
INSERT INTO users ( fname ) VALUES ( $1 )
parameters: $1 = 'Ivan'

INSERT INTO users ( fname ) VALUES ( $1 )
parameters: $1 = 'Huan'
COMMIT


(jdbc/with-db-transaction [tx db]
  (jdbc/insert! db :users {:fname "Ivan"})
  (jdbc/insert! db :users {:fname "Huan"}))


book_postgres | 2021-07-17 09:07:45.298 UTC [113] LOG:  execute <unnamed>: BEGIN
book_postgres | 2021-07-17 09:07:45.298 UTC [113] LOG:  execute <unnamed>: INSERT INTO users ( fname ) VALUES ( $1 )
book_postgres | 2021-07-17 09:07:45.302 UTC [113] LOG:  execute S_1: COMMIT

book_postgres | 2021-07-17 09:07:45.330 UTC [114] LOG:  execute <unnamed>: BEGIN
book_postgres | 2021-07-17 09:07:45.330 UTC [114] LOG:  execute <unnamed>: INSERT INTO users ( fname ) VALUES ( $1 )
book_postgres | 2021-07-17 09:07:45.336 UTC [114] LOG:  execute S_1: COMMIT


(jdbc/with-db-transaction [tx db]

  (let [[{user-id :id}]
        (jdbc/insert! tx :users {:fname "Ivan"})

        [{profile-id :id}]
        (jdbc/insert! tx :profiles {:user_id user-id :avatar "cat.jpg"})]

    {:user/id user-id
     :profile/id profile-id}))


(jdbc/with-db-transaction [tx db {:isolation :serializable}]

  (let [[{user-id :id}]
        (jdbc/insert! tx :users {:fname "Ivan"})

        [{profile-id :id}]
        (jdbc/insert! tx :profiles {:user_id user-id :avatar "cat.jpg"})]

    {:user/id user-id
     :profile/id profile-id}))


(jdbc/with-db-transaction
  [tx db {:isolation :serializable}]
  ...)


SHOW TRANSACTION ISOLATION LEVEL
SET SESSION CHARACTERISTICS AS TRANSACTION ISOLATION LEVEL SERIALIZABLE
BEGIN
...
COMMIT
SET SESSION CHARACTERISTICS AS TRANSACTION ISOLATION LEVEL READ COMMITTED


(def db {:dbtype "postgresql"
         :dbname "test"
         ...})


"jdbc:postgresql://127.0.0.1:5432/test"

(jdbc/with-db-connection [conn db]
  (jdbc/query conn "select 1"))

(jdbc/with-db-connection [conn db]
  conn)

{:dbtype "postgresql"
 :dbname "test"
 ...
 :connection #object[org.postgresql.jdbc.PgConnection 0x46b37678 "org.postgresql.jdbc.PgConnection@46b37678"]}


(time (dotimes [_ 1000]
        (jdbc/query db "select 1")))
;; "Elapsed time: 19097.466607 msecs"

(time
 (jdbc/with-db-connection [conn db]
   (dotimes [_ 1000]
     (jdbc/query conn "select 1"))))
;; "Elapsed time: 1680.252291 msecs"


(def db {:dbtype "postgresql"
         :dbname "test"
         :host "127.0.0.1"
         :user "book"
         :password "book"})


(require '[mount.core :as mount :refer [defstate]])

(defstate DB
  :start
  (assoc db :connection
         (jdbc/get-connection db))

  :stop
  (let [{:keys [connection]} DB]
    (when connection
      (.close connection))
    db))


(mount/start #'DB)


(require '[hikari-cp.core :as cp])

(def pool-config ;; truncated
  {:minimum-idle       10
   :maximum-pool-size  10
   :adapter            "postgresql"
   :username           "book"
   :server-name        "127.0.0.1"
   :port-number        5432})

(defstate DB
  :start
  (let [pool (cp/make-datasource pool-config)]
    {:datasource pool})
  :stop
  (-> DB :datasource cp/close-datasource))


(def db
  (with-meta
    {:dbtype "postgresql"
     :dbname "test"
     :host "127.0.0.1"
     :user "book"
     :password "book"}

    {'com.stuartsierra.component/start
     (fn [this]
       (assoc this :connection
              (jdbc/get-connection this)))

     'com.stuartsierra.component/stop
     (fn [{:as this :keys [connection]}]
       (when connection
         (.close connection))
       (dissoc this :connection))}))

(def db-started (component/start db))

(jdbc/query db-started "select 1")

(component/stop db-started)


;; [org.xerial/sqlite-jdbc "3.36.0"]

(def db
  {:classname   "org.sqlite.JDBC"
   :subprotocol "sqlite"
   :subname     ":memory:"})

(def db
  {:classname   "org.sqlite.JDBC"
   :subprotocol "sqlite"
   :subname     "database.sqlite"})


(def db
  {:classname "org.sqlite.JDBC"
   :subprotocol "sqlite"
   :subname "/Users/ivan/Library/Application Support/Google/Chrome/Default/History"})



(jdbc/execute! db "create table users (id integer)")

(jdbc/query db "select * from users")

(jdbc/query db "SELECT name FROM sqlite_master WHERE type = 'table'")

(jdbc/query db "SELECT name FROM sqlite_master")




;; https://www.howtogeek.com/255653/how-to-find-your-chrome-profile-folder-on-windows-mac-and-linux/


(jdbc/query db "SELECT name FROM sqlite_master WHERE type = 'table'")

({:name "meta"}
 {:name "urls"}
 {:name "sqlite_sequence"}
 {:name "visits"}
 {:name "visit_source"}
 {:name "keyword_search_terms"}
 {:name "downloads"}
 {:name "downloads_url_chains"}
 {:name "downloads_slices"}
 {:name "segments"}
 {:name "segment_usage"}
 {:name "typed_url_sync_metadata"}
 {:name "content_annotations"})


(def db
  {:classname   "org.sqlite.JDBC"
   :subprotocol "sqlite"
   :subname     ":memory:"})

(def db*
  (assoc db :connection
         (jdbc/get-connection db)))

(jdbc/execute! db* "create table users (id integer)")
(jdbc/insert! db* :users {:id 1})
(jdbc/query db* "select * from users")


(def db
  {:classname "org.sqlite.JDBC"
   :subprotocol "sqlite"
   :subname "/Users/ivan/Library/Application Support/Google/Chrome/Default/History"})


(jdbc/query db* "SELECT name FROM sqlite_master WHERE type = 'table'")

(jdbc/query db* "SELECT name FROM sqlite_master")



(jdbc/execute! db "CREATE TABLE payments (id INTEGER, sum INTEGER, meta TEXT)")


(require '[cheshire.core :as json])


(defn meta->str [meta-info]
  (json/generate-string meta-info))

(defn str->meta [db-string]
  (json/parse-string db-string keyword))

(jdbc/insert!
 db :payments
 {:id 1
  :sum 99
  :meta (meta->str {:year "2021"
                    :from "test@test.com"
                    :BIK "332233"
                    :alerts 0})})

(let [result
      (jdbc/query db "select * from payments")]
  (for [mapping result]
    (update mapping :meta str->meta)))

({:id 1
  :sum 99
  :meta {:year "2021" :from "test@test.com" :BIK "332233" :alerts 0}})


(-> (jdbc/query db "SELECT current_timestamp AS now")
    first
    :now
    type)

java.sql.Timestamp


#_
(extend-protocol jdbc/IResultSetReadColumn
  java.sql.Timestamp
  (result-set-read-column [v _2 _3]
    (tc/from-sql-time v))
  java.sql.Date
  (result-set-read-column [v _2 _3]
    (tc/from-sql-date v))
  java.sql.Time
  (result-set-read-column [v _2 _3]
    (org.joda.time.DateTime. v)))


(extend-protocol jdbc/IResultSetReadColumn
  java.sql.Timestamp
  (result-set-read-column [val _rsmeta _idx]
    (new DateTime (.getTime val))))

(first (jdbc/query db "SELECT current_timestamp AS now"))


(first (jdbc/query db "SELECT current_timestamp AS now"))

{:now #object[org.joda.time.DateTime 0x49a269a4 "2021-07-22T10:23:51.136+03:00"]}


(extend-protocol jdbc/ISQLValue
  DateTime
  (sql-value [val]
    (new java.sql.Timestamp (.getMillis val))))


;; (ns project.core
;;   (:require
;;    project.time-joda ;; extends JDBC protocols

;;    [cheshire.core :as json]
;;    ...))


{:ignored-unused-namespaces [project.time-joda
                             ...]}


{"foo": 1, "bar": 2}

{"bar": 2, "foo": 1}


(jdbc/query db "SELECT * from items")


(-> (jdbc/query db "SELECT * from items")
    first
    :attrs
    type)

;; org.postgresql.util.PGobject


(def attrs
  (-> (jdbc/query db "SELECT * from items")
      first
      :attrs))


(defmulti pg->clojure (fn [pg-obj]
                        (.getValue pg-obj)))


(extend-protocol jdbc/IResultSetReadColumn
  org.postgresql.util.PGobject
  (result-set-read-column [pg-obj _rsmeta _idx]

    (let [pg-val (.getValue pg-obj)
          pg-type (.getType pg-obj)]

      (case pg-type
        ("json" "jsonb")
        (json/parse-string pg-val keyword)

        "inet"
        (java.net.InetAddress/getByName pg-val)

        ;; else
        pg-obj))))


(supers (type {}))

#{clojure.lang.IMeta java.lang.Runnable java.io.Serializable
  clojure.lang.Associative clojure.lang.IPersistentMap clojure.lang.Counted
  clojure.lang.IMapIterable java.util.Map clojure.lang.MapEquivalence
  clojure.lang.IEditableCollection clojure.lang.ILookup
  java.util.concurrent.Callable java.lang.Iterable clojure.lang.APersistentMap
  clojure.lang.IObj java.lang.Object clojure.lang.IFn clojure.lang.Seqable
  clojure.lang.AFn clojure.lang.IKVReduce clojure.lang.IPersistentCollection
  clojure.lang.IHashEq}


#{clojure.lang.IMeta
  java.lang.Runnable
  java.io.Serializable
  ...
  clojure.lang.IPersistentCollection}



(extend-protocol jdbc/ISQLValue
  clojure.lang.IPersistentCollection
  (sql-value [val]
    (doto (new org.postgresql.util.PGobject)
      (.setType "jsonb")
      (.setValue (json/generate-string val)))))


(jdbc/insert! db :items
              {:id 5
               :title "The Catcher in the Rye"
               :attrs {:author "J. D. Salinger"
                       :genre "novel"
                       :year 1951}})

(jdbc/insert! db :items
              {:id 6
               :title "The Catcher in the Rye"
               :attrs ["book" "novel"]})

(jdbc/execute! db ["INSERT INTO items VALUES (?, ?, ?)" 6 "The Catcher in the Rye" {:year 1951 :genre "novel" :author "J. D. Salinger"}])

(jdbc/execute! db ["INSERT INTO items VALUES (?, ?, ?)" 6 "The Catcher in the Rye" ["book" "novel"]])


(jdbc/update! db :items
              {:tags ["book" "novel"]}
              ["id = ?" 5])


(jdbc/get-by-id db :items 5)


(supers (type []))
(supers (type ()))
(supers (type (repeat 1)))


(require '[clojure.spec.alpha :as s])

(s/def ::item-fields
  (s/keys :req-un [::id ::title]
          :opt-un [::attrs ::tags]))

(s/def ::attrs
  (s/map-of keyword? any?))

(s/def ::tags
  (s/coll-of string?))


(defn insert-item [db fields]
  {:pre [(s/assert ::item-fields fields)]}
  (jdbc/insert! db :items fields))


(s/check-asserts true)

(insert-item db {:id 5
                 :title "The Catcher in the Rye"
                 :attrs {:author "J. D. Salinger"
                         :genre "novel"
                         :year 1951}
                 :tags ["book" "novel" nil]})


(first (jdbc/query db "select '(1, 2)'::point"))

{:point #object[org.postgresql.geometric.PGpoint 0x28ebf39e "(1.0,2.0)"]}


(defrecord Point [x y])


(import 'org.postgresql.geometric.PGpoint)

(extend-protocol jdbc/IResultSetReadColumn
  PGpoint
  (result-set-read-column [pg-point _rsmeta _idx]
    (new Point (.-x pg-point) (.-y pg-point))))


(extend-protocol jdbc/ISQLValue
  Point
  (sql-value [{:keys [x y]}]
    (new PGpoint x y)))


(jdbc/query db ["select ? as point" (new Point 1 2)])
(jdbc/query db ["select ? as point" (map->Point {:x 1 :y 2})])
(jdbc/query db ["select ? as point" #book.db.Point{:x 1 :y 2}])


(let [events (get-events http-client)
      sql "select * from users u
join profiles p on p.user_id = u.id
where u.is_active
and p.created_at > ..."
      result (jdbc/query db sql)]
  ...)


"where ... and not u.is_deleted"


(require '[hugsql.core :as hugsql])


(hugsql/def-db-fns "sql/queries.sql")

(list-users db)

(get-user-by-id db {:id 1})
