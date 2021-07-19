(ns book.config
  (:require

   [clojure.data.csv :as csv]
   [clojure.java.io :as io]

   [clojure.java.jdbc :as jdbc]
   [com.stuartsierra.component :as component]
   [clojure.tools.logging :as log]))


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
