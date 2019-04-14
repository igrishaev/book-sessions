(ns book.spec)

(require '[clojure.spec.alpha :as s])

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

(require '[clojure.instant
       	   :refer [read-instant-date]])

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
  [bind & body]
  `(s/conformer
	(fn [~bind]
  	  (try
    	~@body
    	(catch Exception e#
      	  ::s/invalid)))))

(s/def ::->int
  (s/and
   ::ne-string
   (with-conformer val
 	 (Integer/parseInt val))))

(def ->lower
  (s/and
   string?
   (s/conformer clojure.string/lower-case)))

(s/def ::->bool
  (s/and
   ->lower
   (with-conformer val
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
   (with-conformer val
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

(require '[clojure.java.io :as io])

(defn get-ini-lines
  [path]
  (with-open [src (io/reader path)]
    (doall (line-seq src))))

(require '[clojure.string :as str])

(defn comment?
  [line]
  (str/starts-with? line "#"))

(defn clear-ini-lines
  [lines]
  (->> lines
       (filter (complement str/blank?))
       (filter (complement comment?))))

(s/def :ini/title
  (s/and
   #(str/starts-with? % "[")
   #(str/ends-with? % "]")
   (with-conformer val
     (subs val 1 (dec (count val))))))

(s/def :ini/field
  (with-conformer val
    (let [[key val :as pair] (str/split val #"=" 2)]
      (if (and key val)
        pair
        ::s/invalid))))

(s/def ::->ini-config
  (s/and
   (s/conformer clear-ini-lines)
   (s/* (s/cat :title :ini/title :fields (s/* :ini/field)))))

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

(require '[clojure.walk :as walk])

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
  "Return a difference between two dates in seconds."
  [^Date date1 ^Date date2]
  (quot (- (.getTime date2)
           (.getTime date1))
        1000))

(date-range-sec
 #inst "2019-01-01" #inst "2019-01-02")

(s/fdef date-range-sec
  :args (s/cat :start inst? :end inst?)
  :ret int?)

(require '[clojure.spec.test.alpha
           :refer [instrument]])

(instrument `date-range-sec)

"Execution error - invalid arguments to date-range-sec"
"nil - failed: inst? at: [:start]"

(time
 (dotimes [n 10000]
   (date-range-sec #inst "2019" #inst "2020")))

(defn date-range-sec-orig
  "Return a difference between two dates in seconds."
  [^Date date1 ^Date date2]
  (quot (- (.getTime date2)
           (.getTime date1))
        1000))

(time
 (dotimes [n 10000]
   (date-range-sec-orig
    #inst "2019" #inst "2020")))

(require '[clojure.repl :refer [doc]])

(def config
  {:db {:dbtype "mysql"
        :host "127.0.0.1"
        :port 3306
        :dbname "project"
        :user "user"
        :password "********"
        :useSSL true}})

(require '[clojure.java.jdbc.spec :as jdbc])

(s/def ::db ::jdbc/db-spec)

(s/def ::config
  (s/keys :req-un [::db]))

(s/valid? ::config config)
