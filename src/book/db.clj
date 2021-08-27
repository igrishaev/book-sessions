(ns book.db
  (:require

   [clojure.data.csv :as csv]
   [clojure.java.io :as io]
   [clojure.spec.alpha :as s]
   [clojure.walk :as walk]

   [honey.sql :as sql]
   [honey.sql.helpers :as h]

   [flatland.ordered.map :refer [ordered-map]]
   [hugsql.core :as hugsql]

   [cheshire.core :as json]
   [hikari-cp.core :as cp]
   [mount.core :as mount :refer [defstate]]
   [clojure.java.jdbc :as jdbc]
   [com.stuartsierra.component :as component]
   [clojure.tools.logging :as log])

  (:import org.joda.time.DateTime
           com.github.vertical_blank.sqlformatter.SqlFormatter))


(def db {:dbtype "postgresql"
         :dbname "test"
         :host "127.0.0.1"
         :user "book"
         :password "book"})

;; [org.clojure/java.jdbc "0.7.8"]
;; [org.postgresql/postgresql "42.1.3"]


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


{:id 1 :fname "Ivan", ...}


(create-user db {:fname "Ioann" :lname "Smith" :email "test@test.com" :age 30})

{:id 76, :fname "Ioann", :lname "Smith", :email "test@test.com", :age 30}

{:id 76, :fname "Ioann", ...}


​​:command :query
:command :insert
:command :execute
:command :returning-execute


(jdbc/with-db-transaction [tx db]
  (create-user tx {:fname "User1" :lname "Lname1" :email "test1@test.com" :age 30})
  (create-user tx {:fname "User2" :lname "Lname2" :email "test2@test.com" :age 30}))


(jdbc/with-db-transaction [tx db]
  (create-user tx {:fname "User1" ...})
  (create-user tx {:fname "User2" ...}))


(get-user-by-id db {:id 1 :user-fields (user-fields)})

(get-user-by-id db {:id 1})

select * from users
where name = :name
and city = :city
and year_birth = :year-birth
limit 10


(hugsql/def-db-fns "sql/queries.sql")

(find-users db {:name "Ivan"})

Execution error (ExceptionInfo) at hugsql.core/validate-parameters! (core.clj:83).
Parameter Mismatch: :city parameter data not found.

(find-users db {:name "Ivan" :city "Chita"})


select * from users
where fname = $1
and city = $2
limit 10
parameters: $1 = 'Ivan', $2 = 'Chita'

(get-user-by-id db {:id 1})
(get-user-by-id db {:id 1 :user-fields (user-fields)})


{:id 1 :fname "Ivan" :lname "Petrov" :email "...", :age ...}

(find-users2 db {:with-photo? true})

select * from users u
join photos p on p.user_id = u.id



(get-user-by-id db {:id 1 :user-fields (user-fields {:root? true})})


(def query
  {:select [:*]
   :from [:users]
   :where [:= :id 1]})

(require '[honey.sql :as sql])

(sql/format query)
;; ["SELECT * FROM users WHERE id = ?" 1]

(jdbc/query db (sql/format query))

"SELECT * FROM users WHERE id = 1"


(def query
  {:insert-into :users
   :columns [:id :fname :email]
   :values [[99 "Ivan" "test@test.com"]]})

(sql/format query)

["INSERT INTO users (id, fname, email) VALUES (?, ?, ?)"
 99
 "Ivan"
 "test@test.com"]


(def query
  {:select [:*]
   :from [:users]
   :where [:= :fname :lname]})

(sql/format query)

["SELECT * FROM users WHERE fname = lname"]


(def query
  {:update :users
   :set {:bonus_points [:+ :bonus_points 100]}
   :where [[:= :id 99]]})

(sql/format query)

["UPDATE users SET bonus_points = bonus_points + ? WHERE (id = ?)" 100 99]


(require '[honey.sql.helpers :as h])

(def query
  (-> (h/update :users)
      (h/set {:bonus_points [:+ :bonus_points 100]})
      (h/where [[:= :id 99]])))

(sql/format query)
["UPDATE users SET bonus_points = bonus_points + ? WHERE (id = ?)" 100 99]


(def query
  {:select [:*]
   :from [:users]
   :where [:= :id 1]})



(defn map-query [db-spec map-sql & [map-params]]
  (jdbc/query db-spec (sql/format map-sql {:params map-params})))

(def query
  {:select [:*]
   :from [:users]
   :where [:= :id :?id]})

(map-query db query {:id 1})


(defn map-query [db-spec map-sql & [map-params]]
  (let [sql-vec (sql/format map-sql {:params map-params})]
    (log/infof "Query: %s" (first sql-vec))
    (jdbc/query db-spec sql-vec)))


(map-query db query {:id 1})
2021-08-06 10:29:25,702 INFO  book.db - Query: SELECT * FROM users WHERE id = ?


(import 'com.github.vertical_blank.sqlformatter.SqlFormatter)

(SqlFormatter/format "SELECT * FROM table1")


(defn map-query [db-spec map-sql & [map-params]]
  (let [sql-vec (sql/format map-sql {:params map-params})]
    (log/infof "Query:\n%s" (SqlFormatter/format (first sql-vec)))
    (jdbc/query db-spec sql-vec)))



(sql/register-clause!
 :create-index
 (fn [_ {idx-name :name
         :keys [unique?
                if-not-exists?
                on-table
                on-field
                using]}]

   [(clojure.string/join
     " "
     ["CREATE"
      (when unique? "UNIQUE")
      "INDEX"
      (when if-not-exists? "IF NOT EXISTS")
      (name idx-name)
      "ON"
      (name on-table)
      "(" (name on-field) ")"
      (when using "USING")
      (when using using)])])
 nil)


;; CREATE [ UNIQUE ] INDEX [ CONCURRENTLY ] [ [ IF NOT EXISTS ] имя ] ON [ ONLY ] имя_таблицы [ USING метод ]


(sql/format {:create-index {:if-not-exists? true
                            :name "idx_user_lname"
                            :on-table :users
                            :on-field :lname}})



:where [:betwixt :x 1 10]

["... WHERE x BETWIXT ? AND ?" 1 10]



{:where [:= [:json->> :attributes :color] "red"]}

["... WHERE attributes ->> 'color' = ?" "red"]








(map-query db {:select [:*]
               :from [:users]
               :where [:>= :created_at [:raw "now() - interval '1 day'"]]})


{:select [:*]
 :from [:users]
 :where [:>= :created_at [:raw "now() - interval '1 day'"]]}


(map-query
 db
 {:update :items
  :set {:attrs [:raw ["attrs || " [:param :new-attrs]]]}
  :returning [:id]}
 {:new-attrs {:color "red" :size "XL"}})


(def user-base
  {:select [:*]
   :from [:users]})

(map-query db user-base)


(map-query db (assoc user-base
                     :where [:= :fname "Ivan"]
                     :limit 9))


(let [q-fname "Ivan"
      q-email "test@test.com"
      q-age nil

      query
      (assoc user-base :where [:and [:= :fname q-fname]])]

  (cond-> query

    q-age
    (update :where conj [:= :age q-age])

    q-email
    (update :where conj [:= :email q-email])))


{:select [:*]
 :from [:users]
 :where [:and
         [:= :fname "Ivan"]
         [:= :email "test@test.com"]]}





(let [q-fname "Ivan"
      with-photo? true

      query
      (assoc user-base :where [:and [:= :fname q-fname]])]

  (cond-> query

    with-photo?
    (assoc :join [:photos [:= :users.id :photos.user_id]])))



(def rows
  [{:user/id 1 :user/name "Ivan" :order/id 5 :order/title "Foo" :order/user-id 1}
   {:user/id 1 :user/name "Ivan" :order/id 6 :order/title "Bar" :order/user-id 1}
   {:user/id 2 :user/name "Huan" :order/id 7 :order/title "Out" :order/user-id 2}])


(reduce
 (fn [result row]

   (let [user (select-keys row [:user/id :user/name])
         user-id (:user/id user)
         order (select-keys row [:order/id :order/title :order/user-id])]

     (-> result
         (update-in [:users user-id] merge user)
         (update-in [:users user-id :user/orders] conj order))))
 {}
 rows)



(def rows
  [{:user/id 1 :user/name "Ivan" :post/id 5 :post/title "Foo" :post/user-id 1 :comment/id 2 :comment/text "aaa" :comment/post-id 5}])


(reduce
 (fn [result row]

   (let [{user-id :user/id
          post-id :post/id
          comment-id :comment/id} row

         user (select-keys row [:user/id :user/name])
         post (select-keys row [:post/id :post/title :post/user-id])
         comment (select-keys row [:comment/id :comment/text :comment/post-id])]

     (-> result
         (update-in [:users user-id :user/posts post-id] merge post)
         (update-in [:users user-id :user/posts post-id :post/comments comment-id] merge comment)
         (update-in [:users user-id] merge user))))
 {}
 rows)


{:users
 {1
  {:user/posts
   {5
    {:post/id 5
     :post/title "Foo"
     :post/user-id 1
     :post/comments {2 {:comment/id 2
                        :comment/text "aaa"
                        :comment/post-id 5}}}}
   :user/id 1
   :user/name "Ivan"}}}


;; goods
[{:id 1 :title "iPhone 99x"}
 {:id 2 :title "Galaxy 33.plus"}
 {:id 3 :title "G. Orwell 1984"}]

;; categories
[{:id 10 :title "Gadgets"}
 {:id 20 :title "Books"}]


[{:id 10
  :title "Gadgets"
  :goods [{:id 1 :title "iPhone 99x"}
          {:id 2 :title "Galaxy 33.plus"}]}
 {:id 20
  :title "Books"
  :goods [{:id 3 :title "G. Orwell 1984"}]}]


(def cat-id 10)

(jdbc/get-by-id db :categories cat-id)
;; {:id 10 :title "Gadgets"}

(jdbc/find-by-keys db :goods {:category_id cat-id})

({:id 1 :title "iPhone 99x" :category_id 10}
 {:id 2 :title "Galaxy 33.plus" :category_id 10})


(let [cat-id 10

      category
      (jdbc/get-by-id ...)

      goods
      (jdbc/find-by-keys ...)]

  (assoc category :goods goods))

{:id 10 :title "Gadgets" :goods [...]}


(def author-id 1)

(jdbc/get-by-id db :authors author-id)
;; {:id 1 :name "Ivan Petrov"}

(jdbc/find-by-keys db :posts {:author_id author-id})

;; ({:id 10, :title "Introduction to Python", :author_id 1})


(let [author-id 1
      author (jdbc/get-by-id ...)
      posts (jdbc/find-by-keys ...)]
  (assoc author :posts posts))

;; {:id 1 :name "Ivan Petrov" :posts [...]}


;; authors
[{:id 1 :name "Ivan Petrov"}
 {:id 2 :name "Ivan Rublev"}]

;; posts
[{:id :title "Introduction to Python" :author-id 1}
 {:id :title "Thoughts on LISP" :author-id 2}]



[{:id 1
  :name "Ivan Petrov"
  :posts [{:id
           :title "Introduction to Python"
           :author-id 1}]}
 {:id 2
  :name "Ivan Rublev"
  :posts [{:id
           :title "Thoughts on LISP"
           :author-id 2}]}]


(jdbc/query db ["SELECT * FROM authors WHERE name LIKE '%' || ? || '%'" "Ivan"])

({:id 1 ...} {:id 2 ...})


(for [author authors]
  (let [{:keys [id]} author
        posts (jdbc/find-by-keys db :posts {:author_id id})]
    (assoc author :posts posts)))

...FROM posts WHERE author_id IN (?, ?, ...)

["SELECT * FROM posts WHERE author_id IN (?,?,?,?,?,?,?,?,?,?)"
 1, 2, 3, 4, 5, 6, 7, 8, 9, 10]

(jdbc/query db ["SELECT * FROM posts WHERE author_id IN (?,?,?,?,?,?,?,?,?,?)"
                1, 2, 3, 4, 5, 6, 7, 8, 9, 10])


(defn fetch-related
  [db-spec table fk-name fk-vals])

(fetch-related db :posts :author_id [1..10])


{1 {:id 1 :name "Ivan Petrov"}
 2 {:id 2 :name "Ivan Rublev"}}



(defn by-chunks [coll n]
  (partition n n [] coll))

(reduce
 (fn [result ids-chunk]
   (let [rows (fetch-related ... ids-chunk)]
     (into result rows)))
 []
 (by-chunks ids-all 100))


(def ids-all [1 2 3 4 5])


(let [ids-chunks (by-chunks ids-all 100)
      futs (doall
            (for [ids-chunk ids-chunks]
              (future
                (fetch-related ... ids-chunk))))]
  (reduce
   (fn [result fut]
     (into result @fut))
   []
   futs))


(def ^java.sql.Connection conn
  (jdbc/get-connection db))

(def array (.createArrayOf conn "INTEGER" (object-array [1 2 3 4 5])))

(jdbc/query
 (assoc db :connection conn)
 ["select * from authors where name in (?)" array])


(extend-protocol jdbc/ISQLParameter

  java.sql.Array

  (set-parameter [val stmt ix]
    (println val stmt ix)
    (.setArray stmt ix val)))


(def ^java.sql.Connection conn
  (jdbc/get-connection db))

(def array (.createArrayOf conn "INTEGER" (object-array [1 2 3 4 5])))

(def stmp
  (.prepareStatement conn "select * from authors where id = ANY(?)"))

(.setArray stmp 1 array)

(def rs (.executeQuery stmp))

(resultset-seq rs)




(def ^java.sql.Connection conn
  (jdbc/get-connection db))

(def array
  (.createArrayOf conn "INTEGER" (object-array [1 2 3 4 5])))

(extend-protocol jdbc/ISQLParameter
  java.sql.Array
  (set-parameter [val ^java.sql.PreparedStatement stmt ix]
    (.setArray stmt ix val)))

(jdbc/query db ["select * from authors where id = ANY(?)" array])

;; ({:id 1 :name "Ivan Petrov"} {:id 2 :name "Ivan Rublev"})


(defn make-db-array [db-spec db-type values]
  (let [conn (jdbc/get-connection db)]
    (.createArrayOf conn db-type (object-array values))))


(defn get-author [db-row]
  (select-keys db-row [:author/id :author/name]))

(defn get-post [db-row]
  (select-keys db-row [:post/id :post/title :post/author-id]))


(defn get-entity [entity db-row]
  (reduce-kv
   (fn [result k v]
     (if (= (namespace k) entity)
       (assoc result k v)
       result))
   {}
   db-row))


(def row
  {:author/id 1
   :author/name "Ivan Petrov"
   :post/id 10
   :post/title "Introduction to Python"
   :post/author-id 1})


(get-author row)

(get-post row)

(get-entity "author" row)
#:author{:id 1 :name "Ivan Petrov"}

(get-entity "post" row)
#:post{:id 10 :title "Introduction to Python" :author-id 1}

(defn row->entities [db-row]
  (reduce-kv
   (fn [result k v]
     (assoc-in result [(namespace k) k] v))
   {}
   db-row))

(row->entities row)

{"author" #:author{:id 1 :name "Ivan Petrov"}
 "post" #:post{:id 10 :title "Introduction to Python" :author-id 1}}


(let [{:strs [author
              post
              comment]} (row->entities row)]
  ...)





(jdbc/query db "SELECT
  a.id        as \"author/id\",
  a.name      as \"author/name\",
  p.id        as \"post/id\",
  p.title     as \"post/title\",
  p.author_id as \"post/author-id\"
FROM authors a
JOIN posts p ON p.author_id = a.id;
")


({:author/id 1
  :author/name "Ivan Petrov"
  :post/id 10
  :post/title "Introduction to Python"
  :post/author-id 1}
 {:author/id 1
  :author/name "Ivan Petrov"
  :post/id 20
  :post/title "Thoughts on LISP"
  :post/author-id 1}
 {:author/id 2
  :author/name "Ivan Rublev"
  :post/id 30
  :post/title "Is mining still profitable?"
  :post/author-id 2}
 {:author/id 2
  :author/name "Ivan Rublev"
  :post/id 40
  :post/title "Mining on Raspberry Pi"
  :post/author-id 2})


(get-author {:author/id 1
             :author/name "Ivan Petrov"
             :post/id 10
             :post/title "Introduction to Python"
             :post/author-id 1})

#:author{:id 1, :name "Ivan Petrov"}


(def db-result
  '
  ({:author/id 1
    :author/name "Ivan Petrov"
    :post/id 10
    :post/title "Introduction to Python"
    :post/author-id 1}
   {:author/id 1
    :author/name "Ivan Petrov"
    :post/id 20
    :post/title "Thoughts on LISP"
    :post/author-id 1}
   {:author/id 2
    :author/name "Ivan Rublev"
    :post/id 30
    :post/title "Is mining still profitable?"
    :post/author-id 2}
   {:author/id 2
    :author/name "Ivan Rublev"
    :post/id 40
    :post/title "Mining on Raspberry Pi"
    :post/author-id 2}))


(reduce
 (fn [result row]

   (let [{:strs [author post]}
         (row->entities row)

         {author-id :author/id}
         author

         {post-id :post/id}
         post]

     (-> result
         (update-in [:authors author-id] merge author)
         (update-in [:authors author-id :author/posts post-id] merge post))))
 {}
 db-result)


{:authors
 {1 #:author{:id 1
             :name "Ivan Petrov"
             :posts {10 #:post{:id 10 :title "Introduction to Python" :author-id 1}
                     20 #:post{:id 20 :title "Thoughts on LISP" :author-id 1}}}
  2 #:author{:id 2
             :name "Ivan Rublev"
             :posts {30 #:post{:id 30 :title "Is mining still profitable?" :author-id 2}
                     40 #:post{:id 40 :title "Mining on Raspberry Pi" :author-id 2}}}}}


(reduce
 (fn [result row]
   (let [author    (get-author row)
         post      (get-post row)
         author-id (:author/id author)
         post-id   (:post/id post)]
     (-> result
         (update-in [:authors author-id] merge author)
         (update-in [:authors author-id :author/posts post-id] merge post))))
 {}
 db-result)


{:authors
 {1 #:author{:id 1
             :name "Ivan Petrov"
             :posts {10 #:post{:id 10 :title "Introduction to Python" :author-id 1}
                     20 #:post{:id 20 :title "Thoughts on LISP" :author-id 1}}}
  2 #:author{:id 2
             :name "Ivan Rublev"
             :posts {30 #:post{:id 30 :title "Is mining still profitable?" :author-id 2}
                     40 #:post{:id 40 :title "Mining on Raspberry Pi" :author-id 2}}}}}


(jdbc/query db "SELECT
  a.id        as \"author/id\",
  a.name      as \"author/name\",
  p.id        as \"post/id\",
  p.title     as \"post/title\",
  p.author_id as \"post/author-id\",
  c.id        as \"comment/id\",
  c.text      as \"comment/text\",
  c.post_id   as \"comment/post-id\"
FROM authors a
JOIN posts p ON p.author_id = a.id
LEFT JOIN comments c ON c.post_id = p.id;
")


(def db-result
  '
  ({:author/id 1
    :author/name "Ivan Petrov"
    :post/id 10
    :post/title "Introduction to Python"
    :post/author-id 1
    :comment/id 100
    :comment/text "Thanks for sharing this!"
    :comment/post-id 10}
   {:author/id 1
    :author/name "Ivan Petrov"
    :post/id 10
    :post/title "Introduction to Python"
    :post/author-id 1
    :comment/id 200
    :comment/text "Nice reading it was useful."
    :comment/post-id 10}
   {:author/id 2
    :author/name "Ivan Rublev"
    :post/id 30
    :post/title "Is mining still profitable?"
    :post/author-id 2
    :comment/id 300
    :comment/text "TL;DR: you must learn lisp"
    :comment/post-id 30}
   {:author/id 1
    :author/name "Ivan Petrov"
    :post/id 20
    :post/title "Thoughts on LISP"
    :post/author-id 1
    :comment/id nil
    :comment/text nil
    :comment/post-id nil}
   {:author/id 2
    :author/name "Ivan Rublev"
    :post/id 40
    :post/title "Mining on Raspberry Pi"
    :post/author-id 2
    :comment/id nil
    :comment/text nil
    :comment/post-id nil}))



(reduce
 (fn [result row]

   (let [{:strs [author post comment]}
         (row->entities row)

         {author-id :author/id}   author
         {post-id :post/id}       post
         {comment-id :comment/id} comment]

     (cond-> result
       :then
       (update-in [:authors author-id] merge author)

       :then
       (update-in [:authors author-id :author/posts post-id] merge post)

       comment-id
       (update-in [:authors author-id :author/posts post-id :post/comments comment-id] merge comment))))
 {}
 db-result)



{:authors
 {1 #:author{:id 1
             :name "Ivan Petrov"
             :posts {10 #:post{:id 10
                               :title "Introduction to Python"
                               :author-id 1
                               :comments {100 #:comment{:id 100
                                                        :text "Thanks for sharing this!"
                                                        :post-id 10}
                                          200 #:comment{:id 200
                                                        :text "Nice reading it was useful."
                                                        :post-id 10}}}
                     20 #:post{:id 20 :title "Thoughts on LISP" :author-id 1}}}
  2 #:author{:id 2
             :name "Ivan Rublev"
             :posts {30 #:post{:id 30
                               :title "Is mining still profitable?"
                               :author-id 2
                               :comments {300 #:comment{:id 300
                                                        :text "TL;DR: you must learn lisp"
                                                        :post-id 30}}}
                     40 #:post{:id 40 :title "Mining on Raspberry Pi" :author-id 2}}}}}


(def enumerate
  (partial map-indexed vector))


(enumerate ["a" "b" "c"])


(defn row->entities [idx db-row]
  (reduce-kv
   (fn [result k v]
     (update result
             (namespace k)
             assoc
             k v
             :db/index idx))
   {}
   db-row))



(row->entities 3 {:post/id 1 :author/id 2})

{"post" {:post/id 1 :db/index 3}
 "author" {:author/id 2 :db/index 3}}


(reduce
 (fn [result [idx row]]

   (let [{:strs [author post comment]}
         (row->entities idx row)

         {author-id :author/id}   author
         {post-id :post/id}       post
         {comment-id :comment/id} comment]

     (cond-> result
       :then
       (update-in [:authors author-id] merge author)

       :then
       (update-in [:authors author-id :author/posts post-id] merge post)

       comment-id
       (update-in [:authors author-id :author/posts post-id :post/comments comment-id] merge comment))))
 {}
 (enumerate db-result))


(reduce
 (fn [result [idx row]]
   (let [{:strs [author post comment]}
         (row->entities idx row)]
     ...))
 {}
 (enumerate db-result))


(def result-grouped
  {:authors
   {1 {:author/id 1
       :db/index 3
       :author/name "Ivan Petrov"
       :author/posts {10 {:post/id 10
                          :db/index 1
                          :post/title "Introduction to Python"
                          :post/author-id 1
                          :post/comments {100 {:comment/id 100
                                               :db/index 0
                                               :comment/text "Thanks for sharing this!"
                                               :comment/post-id 10}
                                          200 {:comment/id 200
                                               :db/index 1
                                               :comment/text "Nice reading it was useful."
                                               :comment/post-id 10}}}
                      20 {:post/id 20
                          :db/index 3
                          :post/title "Thoughts on LISP"
                          :post/author-id 1}}}
    2 {:author/id 2
       :db/index 4
       :author/name "Ivan Rublev"
       :author/posts {30 {:post/id 30
                          :db/index 2
                          :post/title "Is mining still profitable?"
                          :post/author-id 2
                          :post/comments {300 {:comment/id 300
                                               :db/index 2
                                               :comment/text "TL;DR: you must learn lisp"
                                               :comment/post-id 30}}}
                      40 {:post/id 40
                          :db/index 4
                          :post/title "Mining on Raspberry Pi"
                          :post/author-id 2}}}}})


(require '[clojure.walk :as walk])

(def entry?
  (partial instance? clojure.lang.MapEntry))


(def nested-tags
  #{:authors :author/posts :post/comments})


(defn remap-entities
  [form]
  (if (entry? form)
    (let [[k v] form]
      (if (contains? nested-tags k)
        [k (->> v vals (sort-by :db/index) vec)]
        form))
    form))


(:authors
 (walk/prewalk remap-entities result-grouped))


[{:author/id 1
  :db/index 3
  :author/name "Ivan Petrov"
  :author/posts
  [{:post/id 10
    :db/index 1
    :post/title "Introduction to Python"
    :post/author-id 1
    :post/comments
    [{:comment/id 100
      :db/index 0
      :comment/text "Thanks for sharing this!"
      :comment/post-id 10}
     {:comment/id 200
      :db/index 1
      :comment/text "Nice reading it was useful."
      :comment/post-id 10}]}
   {:post/id 20
    :db/index 3
    :post/title "Thoughts on LISP"
    :post/author-id 1}]}
 {:author/id 2
  :db/index 4
  :author/name "Ivan Rublev"
  :author/posts
  [{:post/id 30
    :db/index 2
    :post/title "Is mining still profitable?"
    :post/author-id 2
    :post/comments
    [{:comment/id 300
      :db/index 2
      :comment/text "TL;DR: you must learn lisp"
      :comment/post-id 30}]}
   {:post/id 40
    :db/index 4
    :post/title "Mining on Raspberry Pi"
    :post/author-id 2}]}]


(def query
  "SELECT
  a.id                         as \"author/id\",
  a.name                       as \"author/name\",
  json_agg(row_to_json(posts)) as \"author/posts\"
FROM
  authors a,
  (SELECT
    p.id        as \"post/id\",
    p.title     as \"post/title\",
    p.author_id as \"post/author-id\",
    json_agg(row_to_json(c)) FILTER (WHERE c IS NOT NULL) as \"post/comments\"
  FROM posts p
  LEFT JOIN comments c ON c.post_id = p.id
  GROUP BY p.id
) AS posts
WHERE a.id = posts.\"post/author-id\"
GROUP BY a.id;
")


(jdbc/query db query)


[#:author{:id 1
          :name "Ivan Petrov"
          :posts [#:post{:id 10
                         :title "Introduction to Python"
                         :author-id 1
                         :comments [{:id 100 :text "Thanks for sharing this!" :post_id 10}
                                    {:id 200
                                     :text "Nice reading it was useful."
                                     :post_id 10}]}
                  #:post{:id 20
                         :title "Thoughts on LISP"
                         :author-id 1
                         :comments nil}]}
 #:author{:id 2
          :name "Ivan Rublev"
          :posts [#:post{:id 30
                         :title "Is mining still profitable?"
                         :author-id 2
                         :comments [{:id 300 :text "TL;DR: you must learn lisp" :post_id 30}]}
                  #:post{:id 40
                         :title "Mining on Raspberry Pi"
                         :author-id 2
                         :comments nil}]}]


{:authors
 {1 {:author/id 1
     :db/index 3
     :author/posts
     {10 {:post/id 10
          :db/index 1
          :post/comments
          {100 {:comment/id 100
                :db/index 0
                :comment/post-id 10}
           200 {:comment/id 200
                :db/index 1
                :comment/post-id 10}}}}}}}



(into {} (for [x (range 8)]
           [x x]))

{0 0, 1 1, 2 2, 3 3, 4 4, 5 5, 6 6, 7 7}

(into {} (for [x (range 9)]
           [x x]))

{0 0, 7 7, 1 1, 4 4, 6 6, 3 3, 2 2, 5 5, 8 8}

(-> v vals count)


(require '[flatland.ordered.map :refer [ordered-map]])



[org.flatland/ordered "1.5.9"]              ;; project
[flatland.ordered.map :refer [ordered-map]] ;; ns


(into (ordered-map)
      (for [x (range 16)]
        [x x]))

{0 0, 1 1, 2 2, ... 13 13, 14 14, 15 15}

(fnil assoc (ordered-map))

(def assoc*
  (fnil assoc (ordered-map)))

(:authors result-grouped)



[{:id 1
  :title "iPhone 99x"
  :attrs {:phone.display.diag 145
          :phone.wifi.support true}}
 {:id 2
  :title "Galaxy 33.plus"
  :attrs nil}
 {:id 3
  :title "G. Orwell 1984"
  :attrs {:book.genre "dystopia"
          :book.pages 215}}]



(def result
  [{:id 1
    :type "user"
    :entity
    {:id 1
     :fname "Ivan"
     :lname "Petrov"
     :email "test@test.com"
     :age 30
     :city nil
     :year_birth nil
     :created_at "2021-08-10T10:36:03.934029+03:00"}}
   {:id 3
    :type "user"
    :entity
    {:id 3
     :fname "Huan"
     :lname nil
     :email nil
     :age nil
     :city nil
     :year_birth nil
     :created_at "2021-08-10T10:36:03.934029+03:00"}}
   {:id 1
    :type "admin"
    :entity {:id 1
             :full_name "Petr Smirnov"
             :email "petr@test.com"}}
   {:id 2
    :type "admin"
    :entity {:id 2
             :full_name "Oleg Ivanov"
             :email "oleg@test.com"}}])




(doseq [{:keys [id type entity]} result]
  (case type
    "user" (process-user ...)
    "admin" (process-admin ...)))


1630047678-create-users-table.up.sql
1630047678-create-users-table.down.sql


(def migrations
  [{:id 1630047678
    :description "Create users table"
    :up "1630047678-create-users-table.up.sql"
    :down "1630047678-create-users-table.down.sql"}
   {:id 1630048005
    :description "Create profiles table"
    :up "1630048005-create-profiles-table.up.sql"
    :down "1630048005-create-profiles-table.down.sql"}])


(def current-db-id 1)


(->> migrations
     (filter (fn [{:keys [id]}]
               (> id current-db-id)))
     (sort-by :id)
     (map :up))

(->> migrations
     (filter (fn [{:keys [id]}]
               (and (>= id target_id)
                    (< id current-db-id))))
     (sort-by :id)
     (reverse)
     (map :down))


:plugins [... [migratus-lein "0.7.3"]]


mkdir -p resources/migrations
cd resources/migrations

touch 1630047678-create-users-table.up.sql
touch 1630047678-create-users-table.down.sql
touch 1630048005-create-profiles-table.up.sql
touch 1630048005-create-profiles-table.down.sql

:dependencies [... [migratus "1.3.5"]]
