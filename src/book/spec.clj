(ns book.spec
  (:require
   [clojure.tools.logging :as log]
   [clojure.java.jdbc.spec :as jdbc]
   [clojure.repl :refer [doc]]
   [clojure.spec.test.alpha
    :refer [instrument]]
   [clojure.walk :as walk]
   [clojure.string :as str]
   [clojure.instant
    :refer [read-instant-date]]
   [spec-dict :refer [dict dict*]]
   [clojure.java.io :as io]
   [clojure.spec.alpha :as s]
   [spec-tools.json-schema :as json-schema]))

(s/def ::string string?)

(s/get-spec ::string)

(s/valid? ::string 1)

(s/valid? ::string "test")

(s/def ::ne-string
  (fn [val]
	(and (string? val)
     	 (not (empty? val)))))

(s/def ::ne-string
  (every-pred string? not-empty))

(s/valid? ::ne-string "test")

(s/valid? ::ne-string "")

(s/def ::ne-string
  (s/and ::string not-empty))

(s/def ::url
  (partial re-matches #"(?i)^http(s?)://.*"))

(s/valid? ::url "test")

(s/valid? ::url "http://test.com")

;; (s/valid? ::url nil)

(s/def ::url
  (s/and
   ::ne-string
   (partial re-matches #"(?i)^http(s?)://.*")))

(s/valid? ::url nil)

(s/def ::age
  (s/and int? #(<= 0 % 150)))

(s/valid? ::age nil)

(s/valid? ::age -1)

(s/valid? ::age 42)

(s/def ::url-list (s/coll-of ::url))

(s/valid? ::url-list
          ["http://test.com" "http://ya.ru"])

(s/valid? ::url-list
          ["http://test.com" "dunno.com"])

(s/def ::params
  (s/map-of keyword? string?))

(s/valid? ::params {:foo "test"})

(s/valid? ::params {"foo" "test"})

(s/def :page/address ::url)
(s/def :page/description ::ne-string)

(s/def ::page
  (s/keys :req-un [:page/address
                   :page/description]))

(def valid-page? (partial s/valid? ::page))

{:address "clojure.org"
 :description "Clojure Language"}

{:address "https://clojure.org/"
 :description ""}

{:address "https://clojure.org/"}

{:page/address "https://clojure.org/"
 :page/description "Clojure Language"}

(s/valid? ::page {:address "https://clojure.org/"
                  :description "Clojure Language"})

(s/def ::page-fq
  (s/keys :req [:page/address
                :page/description]))

(s/valid? ::page-fq
          {:page/address "https://clojure.org/"
           :page/description "Clojure Language"})

(s/def :page/status int?)

(s/def ::page-status
  (s/keys :req-un [:page/address
                   :page/description]
          :opt-un [:page/status]))

(s/valid? ::page-status
          {:address "https://clojure.org/"
           :description "Clojure Language"})

(s/valid? ::page-status
          {:address "https://clojure.org/"
           :description "Clojure Language"
           :status 200})

(s/valid? ::page-status
          {:address "https://clojure.org/"
           :description "Clojure Language"
           :status nil})

(read-instant-date "2019")

(s/def ::->date
  (s/and
   ::ne-string
   (s/conformer
	(fn [value]
  	  (try
    	(read-instant-date value)
    	(catch Exception e
      	  ::s/invalid))))))


(s/def ::->date
  (s/and ::ne-string (s/conformer read-instant-date)))


(s/conform ::->date "2019-12-31")

(s/conform ::->date "2019-12-31T23:59:59")

(s/def ::->bits
  (s/conformer
   (fn [value]
 	(case value
   	"32" 32
   	"64" 64
   	::s/invalid))))

(s/conform ::->bits "32")

(s/conform ::->bits "42")

(def bits-map {"32" 32 "64" 64})

(s/def ::->bits
  (s/conformer
   (fn [value]
 	 (get bits-map value ::s/invalid))))

(s/conform ::->bits "32")

(s/conform ::->bits "42")

(s/def ::->bool
  (s/and ::ne-string
     	 (s/conformer clojure.string/lower-case)
     	 (s/conformer
      	  (fn [value]
        	(case value
              ("true" "1" "on" "yes") true
  	          ("false" "0" "off" "no") false
              ::s/invalid)))))

(s/conform ::->bool "True")

(s/conform ::->bool "yes")

(s/conform ::->bool "off")

(s/def ::status #{"todo" "in_progres" "done"})

(s/valid? ::status "todo")

(contains? #{1 :a nil} nil)

(defn enum [& args]
  (let [arg-set (set args)]
	(fn [value]
  	  (contains? arg-set value))))

(s/def ::status
  (enum "todo" "in_progres" "done"))

(defmacro with-conformer
  [[bind] & body]
  `(s/conformer
	(fn [~bind]
  	  (try
    	~@body
    	(catch Exception e#
      	  ::s/invalid)))))

(s/def ::->int
  (s/and
   ::ne-string
   (with-conformer [val]
 	 (Integer/parseInt val))))

(def ->lower
  (s/and
   string?
   (s/conformer clojure.string/lower-case)))

(s/def ::->bool
  (s/and
   ->lower
   (with-conformer [val]
 	 (case val
       ("true"  "1" "on"  "yes") true
   	   ("false" "0" "off" "no" ) false))))

(s/def ::smart-port
  (s/or :string ::->int :num int?))

(s/conform ::smart-port 8080)

(s/conform ::smart-port "8080")

(s/def :conn/port ::smart-port)

(s/def ::conn
  (s/keys :req-un [:conn/port]))

(s/conform ::conn {:port "9090"})
{:port [:string 9090]}

(s/def :sample/username ::ne-string)

(s/def ::sample
  (s/keys :req-un [:sample/username]))

(s/explain ::sample {:username "some user"})

(s/explain-data ::sample {:username "some user"})

(s/explain ::sample {:username 42})

(s/explain ::sample {:username ""})

(s/explain-data ::sample {:username ""})

#:clojure.spec.alpha
{:problems
 '({:path [:username]
    :pred clojure.core/not-empty
    :val ""
    :via [::sample ::ne-string]
    :in [:username]})
 :spec ::sample
 :value {:username ""}}

(s/def ::email
  (s/and
   ::ne-string
   (partial re-matches #"(.+?)@(.+?)\.(.+?)")))

(s/def :sample/email ::email)

(s/def ::sample
  (s/keys :req-un [:sample/username
                   :sample/email]))



(def spec-errors
  {::ne-string "Строка не должна быть пустой"
   ::email "Введите правильный почтовый адрес"})

(defn get-message
  [problem]
  (let [{:keys [via]} problem
        spec (last via)]
    (get spec-errors spec)))

(get-message {:via [::sample ::ne-string]})

{:path [:email]
 :pred clojure.core/not-empty
 :val ""
 :via [::sample :sample/email ::ne-string]
 :in [:email]}

{:path [:email]
 :pred
 (clojure.core/partial
  clojure.core/re-matches
  #"(.+?)@(.+?)\.(.+?)")
 :val "test"
 :via [::sample :sample/email]
 :in [:email]}


(s/def :account/username ::ne-string)
#_(s/def :account/email ::email)
(s/def :account/email (s/spec ::email))

(s/def ::account
  (s/keys :req-un [:account/username
                   :account/email]))


(def spec-errors
  {::ne-string "Строка не должна быть пустой"
   :email "Введите правильный почтовый адрес"
   :account/email "Особое сообщение для адреса отправителя"})

(def default-message
  "Исправьте ошибки в поле")

(defn get-better-message
  [problem]
  (let [{:keys [via]} problem
        spec (last via)]
    (or
     (get spec-errors spec)
     (get spec-errors
          (-> spec name keyword))
     default-message)))


(s/def :user/status
  (s/and
   ->lower
   (with-conformer [val]
     (case val
       "active" :USER_ACTIVE
       "pending" :USER_PENDING))))

(s/def ::user
  (s/cat :id ::->int
         :email ::email
         :status :user/status))

(s/conform ::user ["1" "test@test.com" "active"])

{:id 1
 :email "test@test.com"
 :status :USER_ACTIVE}

(s/def ::blocked
  (s/and
   ->lower
   (s/conformer
    #(= % "blocked"))))

(s/def ::user
  (s/cat :blocked (s/? ::blocked)
         :id ::->int
         :email ::email
         :status :user/status))

(s/conform ::user ["1" "test@test.com" "active"])

(s/conform ::user ["Blocked" "1" "test@test.com" "active"])

(s/def ::users
  (s/coll-of ::user))

(def user-data
  [["1" "test@test.com" "active"]
   ["Blocked" "2" "joe@doe.com" "pending"]])

(s/conform ::users user-data)

{:database {:host "localhost"
            :port 5432}
 :server {:host "127.0.0.1"}}


(defn get-ini-lines
  [path]
  (with-open [src (io/reader path)]
    (doall (line-seq src))))


(defn comment?
  [line]
  (str/starts-with? line "#"))

(defn clear-ini-lines
  [lines]
  (->> lines
       (filter (complement str/blank?))
       (filter (complement comment?))))



#_
(s/def :ini/title
  (s/and
   #(str/starts-with? % "[")
   #(str/ends-with? % "]")
   (with-conformer [val]
     (subs val 1 (dec (count val))))))

(s/def :ini/title
  (with-conformer [line]
    (or (second (re-matches #"^\[(.+)\]$" line))
        ::s/invalid)))

#_
(s/def :ini/field
  (with-conformer [val]
    (let [[key val :as pair] (str/split val #"=" 2)]
      (if (and key val)
        pair
        ::s/invalid))))

(s/def :ini/field
  (with-conformer [line]
    (let [pair (str/split line #"=" 2)]
      (if (= (count pair) 2)
        pair
        ::s/invalid))))

(s/def :ini/section
  (s/cat :title :ini/title :fields (s/* :ini/field)))

(s/def ::->ini-config
  (s/and
   (s/conformer clear-ini-lines)
   (s/* :ini/section)))

(defn parse
  [path]
  (let [lines (get-ini-lines path)]
    (s/conform ::->ini-config lines)))

[{:title "database"
  :fields [["host" "localhost"]
           ["port" "5432"]
           ["user" "test"]]}
 {:title "server"
  :fields [["host" "127.0.0.1"]
           ["port" "8080"]]}]

(defn remap-ini-data
  [data-old]
  (reduce
   (fn [data-new entry]
     (let [{:keys [title fields]} entry]
       (assoc data-new title (into {} fields))))
   {}
   data-old))

{"database" {"host" "localhost" "port" "5432" "user" "test"}
 "server" {"host" "127.0.0.1" "port" "8080"}}

(s/def :db/host ::ne-string)
(s/def :db/port ::->int)
(s/def :db/user ::ne-string)

(s/def ::database
  (s/keys :req-un [:db/host
                   :db/port
                   :db/user]))

(s/def :server/host ::ne-string)
(s/def :server/port ::->int)

(s/def ::server
  (s/keys :req-un [:server/host
                   :server/port]))

(s/def ::ini-config
  (s/keys :req-un [::database
                   ::server]))

(s/def ::->ini-config
  (s/and
   (s/conformer clear-ini-lines)
   (s/* (s/cat :title :ini/title :fields (s/* :ini/field)))
   (s/conformer remap-ini-data)
   (s/conformer walk/keywordize-keys)
   ::ini-config))

{:database {:host "localhost"
            :port 5432
            :user "test"}
 :server {:host "127.0.0.1"
          :port 8080}}


(s/def :defn/body
  (s/cat :args :defn/args
         :prepost (s/? map?)
         :code :defn/code))

(s/def ::defn
  (s/cat :tag (partial = 'defn)
         :name symbol?
         :docstring string?
         :body (s/+ :defn/body)))

(s/def ::date-range-args
  (s/and
   (s/cat :start inst? :end inst?)
   (fn [{:keys [start end]}]
     (<= (compare start end) 0))))

(s/valid? ::date-range-args [#inst "2019" #inst "2020"])

(s/valid? ::date-range-args [#inst "2020" #inst "2019"])

(import 'java.util.Date)

(defn date-range-sec
  "Return the difference between two dates in seconds."
  [^Date date1 ^Date date2]
  (quot (- (.getTime date2)
           (.getTime date1))
        1000))

(date-range-sec
 #inst "2019-01-01" #inst "2019-01-02")

(s/fdef date-range-sec
  :args (s/cat :start inst? :end inst?)
  :ret int?)

(instrument `date-range-sec)

"Execution error - invalid arguments to date-range-sec"
"nil - failed: inst? at: [:start]"

(time
 (dotimes [n 10000]
   (date-range-sec #inst "2019" #inst "2020")))

(defn date-range-sec-orig
  "Return the difference between two dates in seconds."
  [^Date date1 ^Date date2]
  (quot (- (.getTime date2)
           (.getTime date1))
        1000))

(time
 (dotimes [n 10000]
   (date-range-sec-orig
    #inst "2019" #inst "2020")))

(def config
  {:db {:dbtype "mysql"
        :host "127.0.0.1"
        :port 3306
        :dbname "project"
        :user "user"
        :password "********"
        :useSSL true}})


(s/def ::db ::jdbc/db-spec)

(s/def ::config
  (s/keys :req-un [::db]))

(s/valid? ::config config)

"
(expound/expound string? 1)
nil
-- Spec failed --------------------

  1

should satisfy

  string?

-------------------------
Detected 1 error
"
#_
(s/def :defn/body
  (s/cat :args vector?
         :prepost (s/? map?)
         :code (s/* any?)))
#_
(s/def ::defn
  (s/cat :tag (partial = 'defn)
         :name symbol?
         :body :defn/body))


(s/def ::defn
  (s/cat :tag #{'defn}
         :name symbol?
         :body :defn/body))

#_
(s/def ::defn
  (s/cat :tag (partial = 'defn)
         :name symbol?
         :body (s/+ (s/spec :defn/body))))

#_
(s/def :defn/body*
  (s/alt :single :defn/body
         :multi (s/+ (s/spec :defn/body))))

#_
(s/def ::defn
  (s/cat :tag (partial = 'defn)
         :name symbol?
         :doc (s/? string?)
         :body :defn/body*))

;; (s/conform ::defn '(defn my-inc [x] (println 1)))
;; {:tag defn, :name my-inc, :body [:single {:args [x], :code [(println 1)]}]}

;; (s/conform ::defn '(defn my-inc ([x] (println 1)) ([x y] (println 2))))
;; {:tag defn, :name my-inc, :body [:multi [{:args [x], :code [(println 1)]} {:args [x y], :code [(println 2)]}]]}

#_
{:tag defn
 :name my-inc
 :doc "Increase a number"
 :body
 [:single
  {:args [x]
   :prepost {:pre [(int? x)] :post [(int? %)]}
   :code [(+ x 1)]}]}

#_
(let [{:keys [body]} result
      [tag body] body]
  (case tag
    :single
    (process-body body)
    :multi
    (doseq [body body]
      (process-body body))))

(def spec-h
  (-> (make-hierarchy)
      (derive :account/email ::email)
      (derive :account/email2 ::email)))

#_
(derive :account/email ::email)
#_
(derive :account/email2 ::email)

(defmulti problem->text
  (fn [{:keys [via]}]
    (last via)))

#_
(defmulti problem->text
  (fn [{:keys [via]}]
    (last via))
  :hierarchy #'spec-h)

(defmethod problem->text :default [_]
  default-message)

(defmethod problem->text ::ne-string [_]
  "Строка не должна быть пустой")

(defmethod problem->text ::email [_]
  "Введите правильный почтовый адрес")

#_
(defmethod problem->text :account/email [_]
  "Введите почту сотрудника")

(defmethod problem->text :account/email
  [{:keys [val]}]
  (format "Ошибка в адресе почты: %s" val))

#_
{:path [:username]
 :pred clojure.core/not-empty
 :val ""
 :via [::sample ::ne-string]
 :in [:username]}

#_
(def spec-errors
  {::ne-string "Строка не должна быть пустой"
   ::email "Введите правильный почтовый адрес"})

#_
{:path [:email]
 :pred
 (clojure.core/partial
  clojure.core/re-matches
  #"(.+?)@(.+?)\.(.+?)")
 :val "test"
 :via [::sample :sample/email]
 :in [:email]}


#_
{:path [:email],
 :pred (clojure.core/partial clojure.core/re-matches #"(.+?)@(.+?)\.(.+?)"),
 :val "ddddd",
 :via [:book.spec/sample :book.spec/email],
 :in [:email]}

#_
(s/def ::->int
  (s/and
   ::ne-string
   (s/conformer
    (fn [string]
      (Integer/parseInt string))
    (fn [integer]
      (str integer)))))

#_
(s/def ::->int
  (s/and
   ::ne-string
   (s/conformer
    (fn [string]
      (Integer/parseInt string))
    (fn [integer]
      integer))))


#_
(s/conformer
 (fn [string] (Integer/parseInt string))
 (fn [integer] integer))

#_
(defmacro with-conformer
  [[bind] & body]
  `(s/conformer
    (fn [~bind]
      (try
        ~@body
        (catch Exception e#
          ::s/invalid)))
    identity))

#_
(def db-spec
  {:dbtype "mysql" :host "127.0.0.1"
   :port 3306 :dbname "project" :user "user"})

#_
(s/conform ::jdbc/db-spec db-spec)

#_
[:friendly {:dbtype [:name "mysql"]
            :host   "127.0.0.1"
            :port   [:port 3306]
            :dbname "project"
            :user   "user"}]

#_
(def config
  {:db {:dbtype "mysql"
        :host "127.0.0.1"
        :port 3306
        :dbname "project"
        :user "user"
        :password "********"
        :useSSL true}})

{:db
 [:friendly
  {:dbtype   [:name "mysql"]
   :host     "127.0.0.1"
   :port     [:port 3306]
   :dbname   "project"
   :user     "user"
   :password "********"
   :useSSL   true}]}

;; (s/def :book/description string?)
;; (s/def ::book (s/keys :req-un [:book/description]))

;; (s/def :movie/description string?)
;; (s/def ::movie (s/keys :req-un [:movie/description]))

;; (s/explain-data ::book {:description nil})
;; #:clojure.spec.alpha{:problems ({:path [:description], :pred clojure.core/string?, :val nil, :via [:book.spec/book :book/description], :in [:description]}), :spec :book.spec/book, :value {:description nil}}

;; (s/explain-data ::movie {:description nil})
;; #:clojure.spec.alpha{:problems ({:path [:description], :pred clojure.core/string?, :val nil, :via [:book.spec/movie :movie/description], :in [:description]}), :spec :book.spec/movie, :value {:description nil}}


(s/def ::ne-string
  (s/and string? not-empty))


(s/def ::notification #{"daily"})

(s/def ::notification (partial = "daily"))


;; (s/def :book/description (s/and string? not-empty))
;; (s/def ::book (s/keys :req-un [:book/description]))

;; (s/def :movie/description (s/and string? not-empty))
;; (s/def ::movie (s/keys :req-un [:movie/description]))

(s/def :book/description ::ne-string)
(s/def ::book (s/keys :req-un [:book/description]))

(s/def :movie/description ::ne-string)
(s/def ::movie (s/keys :req-un [:movie/description]))

(s/def :book/description (s/spec ::ne-string))

(def ne-string (every-pred string? not-empty))
(s/def :book/description ne-string)



#_
(s/explain-data ::book {:description ""})
#_
(s/explain-data ::movie {:description ""})

(s/def ::string string?)

(s/def ::not-empty not-empty)

(s/def ::ne-string (s/and ::string ::not-empty))



(defn get-message
  [problem]
  (let [{:keys [via]} problem]
    (->> via
         reverse
         (keep spec-errors)
         first)))


(def spec-errors
  {::email "Введите правильный почтовый адрес"})

(get-message {:via [::email ::ne-string]})
"Введите правильный почтовый адрес"


(def spec-errors
  {:book/description "Введите описание книги"
   :movie/description "Введите описание фильма"})


(def spec-errors
  {:description "Неверное описание"
   :book/description "Введите описание книги"})


#_
(def spec-errors
  {::ne-string "Строка не должна быть пустой"
   :email "Введите правильный почтовый адрес"
   :account/email "Особое сообщение для адреса отправителя"})


(s/def :post/title ::ne-string)
(s/def :post/tags (s/coll-of ::ne-string))
(s/def ::post (s/keys :req-un [:post/title]
                      :opt-un [:post/tags]))

(s/explain-data ::post {:title "test"})

(defn last-kw [coll]
  (->> coll
       reverse
       (filter keyword?)
       first))

(defn get-common-message [problem]
  (let [{:keys [in val]} problem
        field (last-kw in)]
    (format "The field '%s' has got an incorrect value '%s'."
            (name field) val)))


(get-common-message {:val "" :in [:tags 1]})
"The field 'tags' has got an incorrect value ''."


(defn get-better-message [problem]
  (let [{:keys [via]} problem
        spec (peek via)]
    (or (get spec-errors spec)
        (do (log/warnf "Missing message for spec %s" spec)
            default-message))))


(get-better-message {:via [:unknown/spec]})
"Исправьте ошибки в поле"


;; (require '[clojure.spec.alpha :as s])

;; (s/check-asserts true)

;; (s/def ::ne-string (s/and string? not-empty))

;; (s/assert ::ne-string "test")

;; (s/assert ::ne-string "")

;; (s/check-asserts false)


(defn spec->keys
  [spec-keys]
  (let [args (s/form spec-keys)
        type->keys (apply hash-map (rest args))
        {:keys [req opt req-un opt-un]} type->keys
        ->unqualify (comp keyword name)]
    (concat req
            opt
            (map ->unqualify opt-un)
            (map ->unqualify req-un))))


(s/def ::->env
  (s/and
   string?
   (s/conformer
    (fn [varname]
      (or (System/getenv varname)
          ::s/invalid)))))


(s/def ::db-password
  (s/and ::->env
         (s/conformer str/trim)
         not-empty))


#_
(edn/read-string {:readers {'env tag-env}}
                 "{:db-password #env DB_PASS}")


{:phrases ["Welcome!"
           "See you soon!"
           {:Warning "wrong email address."}]}


;;  :count - specifies coll has exactly this count (default nil)
;;  :min-count, :max-count - coll has count (<= min-count count max-count) (defaults nil)

(s/def ::url-list (s/coll-of ::url :min-count 1))

#_
(s/valid? ::url-list [])
;; false

#_
(s/valid? ::url-list ["http://test.com"])
;; true

(s/def ::ts->date (s/conformer (fn [^long ts]
                                 (new java.util.Date (* ts 1000)))))

(s/conform ::ts->date 1609230152)
;; #inst "2020-12-29T08:22:32.000-00:00"


(s/def ::->int
  (s/and
    ::ne-string
    (with-conformer [val]
      (Integer/parseInt val))))

(def spec-errors
  {:ru {::ne-string "Строка не должна быть пустой"
        ::email "Введите правильный почтовый адрес"}
   :en {::ne-string "The field must not be blank"
        ::email "The email address is invalid"}})


(def spec-errors
  {:ru
   {::ne-string "Строка не должна быть пустой"
    ::email "Введите правильный почтовый адрес"}
   :en
   {::ne-string "The field must not be blank"
    ::email "The email address is invalid"}})


(def form
  {:name {:title "Your name"
          :widget :textfield
          :value "Ivan"
          :error nil}
   :email {:title "Email address"
           :widget :textfield
           :value "test@"
           :error "Email is incorrect"}})


(def values
  {:name "Ivan"
   :email "test@"})


(s/conform ::user [])

"
1,test@test.com,active
2,ivan@test.com,pending
3,user@test.com,active
"

(require '[spec-tools.json-schema :as json-schema])

(s/def :user/name ::ne-string)
(s/def :user/age pos-int?)

(s/def :user/user
  (s/keys :req-un [:user/name :user/age]))

#_
(json-schema/transform :user/user)

#_
{:type "object"
 :properties
 {"name" {:type "string"}
  "age" {:type "integer"
         :format "int64"
         :minimum 1}}
 :required ["name" "age"]
 :title "user/user"}


#_
(require '[spec-dict :refer [dict dict*]])


(s/def ::user-strict
  (dict* {:name string? :age pos-int?}))

(s/valid? ::user-strict {:name "test" :age 30})
;; true

(s/valid? ::user-strict {:name "test" :age 30 :extra 1})
;; false
