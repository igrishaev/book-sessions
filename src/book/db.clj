(ns book.config
  (:require
   [clojure.java.jdbc :as jdbc]))


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
