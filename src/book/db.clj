(ns book.config
  (:require
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
