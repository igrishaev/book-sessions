(ns book.util-test
  (:require [book.util :refer [->fahr]]
            [clojure.test :refer [deftest testing is are use-fixtures]]))


(defn aaa [t]
  (println "AAA")
  (t))


(use-fixtures :each aaa)

#_
(deftest test-fahr
  (is (= (int (->fahr 20)) 68))
  (is (= (int (->fahr 100)) 212)))

(deftest test-fahr
  (are [f c] (= (int (->fahr f)) c)
    20 68
    100 212))



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
  (is (thrown-with-msg?
       IllegalArgumentException #"Fahrenheit temperature"
       (->fahr nil))))



(defn square-roots [a b c]
  (let [D (- (* b b) (* 4 a c))]
    (cond
      (pos? D) [(/ (+ (- b) (Math/sqrt D)) (* 2 a))
                (/ (- (- b) (Math/sqrt D)) (* 2 a))]
      (zero? D) (/ (- b) (* 2 a))
      (neg? D) nil)))


(deftest test-square-roots
  (testing "Two roots"
    (let [result (square-roots 1 -5 6)]
      (is (= (mapv int result) [3 2]))))
  (testing "One root"
    (is (= (square-roots 1 6 9) -3)))
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

(use-fixtures :once fix-db-server fix-clear-files)
(use-fixtures :each fix-db-data)

(defonce ^:dynamic ^File *file* nil)

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


(use-fixtures :once
  (fix-factory :once 1)
  (fix-factory :once 2))

(use-fixtures :each
  (fix-factory :each 3)
  (fix-factory :each 4))


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


:once 1 starts
 :once 2 starts
  :each 3 starts   +
   :each 4 starts  |
   :each 4 ends    |
  :each 3 ends     +
  :each 3 starts   +
   :each 4 starts  |
   :each 4 ends    |
  :each 3 ends     +
  :each 3 starts   +
   :each 4 starts  |
   :each 4 ends    |
  :each 3 ends     +
  :each 3 starts   +
   :each 4 starts  |
   :each 4 ends    |
  :each 3 ends     +
 :once 2 ends
:once 1 ends
