(ns book.config
  (:require
   [clojure.spec.alpha :as s]))

#_
(def server
  (jetty/run-jetty app {:port 8080}))

(def mysql-db
  {:dbtype   "mysql"
   :dbname   "book"
   :user     "ivan"
   :password "****"})


(defn fake-exit
  [_ template & args]
  (let [message (apply format template args)]
    (throw (new Exception ^String message))))


;;-------
(defn exit

  ([code template & args]
   (exit code (apply format template args)))

  ([code message]
   (let [out (if (zero? code) *out* *err*)]
     (binding [*out* out]
       (println message)))
   (System/exit code)))


;;-------
(import 'java.io.File)

(defn get-config-path
  []
  (if-let [filepath (System/getenv "CONFIG_PATH")]
    (if (-> filepath File. .exists)
      filepath
      (exit 1 "Config file does not exist"))
    (exit 1 "File path is not set")))


;;-------
(require '[cheshire.core :as json])

(defn read-config-file
  [filepath]
  (try
    (-> filepath slurp (json/parse-string true))
    (catch Exception e
      (exit 1 "Malformed config file: %s" (ex-message e)))))


;;-------
(require '[clojure.spec.alpha :as s])
(require '[expound.alpha :as expound])

(defn coerce-config
  [config]
  (try
    (let [result (s/conform ::config config)]
      (if (= result ::s/invalid)
        (let [report (expound/expound-str ::config config)]
          (exit 1 "Invalid config values: %s %s" \newline report))
        result))
    (catch Exception e
      (exit 1 "Wrong config values: %s" (ex-message e)))))


(s/def ::server_port
  (s/and int? #(< 0x400 % 0xffff)))

(require '[clojure.java.jdbc.spec :as jdbc])

(s/def ::db ::jdbc/db-spec)

(require '[clojure.instant :as inst])

(def ->date (s/conformer inst/read-instant-date))

(s/def ::date-range
  (fn [[date1 date2]]
    (neg? (compare date1 date2))))

(s/def ::event
  (s/and
   (s/tuple ->date ->date)
   ::date-range))

(s/def ::ne-string (s/and string? not-empty))

(s/def :db/dbtype   #{"mysql"})
(s/def :db/dbname   ::ne-string)
(s/def :db/user     ::ne-string)
(s/def :db/password ::ne-string)

(s/def ::db
  (s/keys :req-un [:db/dbtype
                   :db/dbname
                   :db/user
                   :db/password]))

(s/def ::config
  (s/keys :req-un [::server_port ::db ::event]))

;;-------
(def CONFIG nil)

(defn set-config!
  [config]
  (alter-var-root (var CONFIG) (constantly config)))


;;-------
(defn load-config!
  []
  (-> #_(get-config-path)
      "config.json"
      (read-config-file)
      (coerce-config)
      (set-config!)))

;;-------

(defn load-config-repl!
  []
  (with-redefs
    [exit (fn [_ ^String msg]
            (throw (new Exception msg)))]
    (load-config!)))


#_
(load-config!)

#_
(require '[project.config :refer [CONFIG]])

#_
(def server
  (jetty/run-jetty app {:server_port CONFIG}))

#_
(jdbc/query (:db CONFIG) "select * from users")




(def ->keywords (partial map keyword))

(require '[clojure.string :as str])

(defn remap-key
  [^String key]
  (-> key
      str/lower-case
      (str/replace #"_" "-")
      keyword))

(defn remap-env
  [env]
  (reduce
   (fn [acc [k v]]
     (let [key (remap-key k)]
       (assoc acc key v)))
   {}
   env))

(defn remap-key-nest
  [^String key]
  (-> key
      str/lower-case
      (str/replace #"_" "-")
      (str/split #"--")
      ->keywords))

(defn remap-env-nest
  [env]
  (reduce
   (fn [acc [k v]]
     (let [key-path (remap-key-nest k)]
       (assoc-in acc key-path v)))
   {}
   env))


(def env (-> (System/getenv)
             remap-env))

(defn deep-merge
  [& maps]
  (if (every? map? maps)
    (apply merge-with deep-merge maps)
    (last maps)))

"
LANG=en_US.UTF-8
PWD=/Users/ivan
SHELL=/bin/zsh
TERM_PROGRAM=iTerm.app
COMMAND_MODE=unix2003
"

;; so-so
{:db_name "book"
 :db_user "ivan"
 :db_pass "****"}

;; better
{:db {:name "book"
      :user "ivan"
      :pass "****"}}

"DB_NAME=book"
;; {:db_name "book"}

"DB__NAME=book"
;; {:db {:name "book"}}


(System/getenv "HOME")
"/Users/ivan"

(System/getenv)
{"JAVA_ARCH" "x86_64", "LANG" "en_US.UTF-8"} ;; truncated


{:home "/Users/ivan"
 :lang "en_US.UTF-8"
 :term "xterm-256color"
 :java-arch "x86_64"
 :term-program "iTerm.app"
 :shell "/bin/zsh"}


(s/def ::->int
  (s/conformer
   (fn [value]
     (cond
       (int? value) value
       (string? value)
       (try (Integer/parseInt value)
            (catch Exception e
              ::s/invalid))
       :else ::s/invalid))))

(s/def ::db-port
  (s/and ::->int #(< 0x400 % 0xffff)))


'
(clojure.spec.alpha/keys
 :req-un [:book.config/server_port
          :book.config/db
          :book.config/event])


(defn spec->keys
  [spec-keys]
  (let [form (s/form spec-keys)
        params (apply hash-map (rest form))
        {:keys [req opt req-un opt-un]} params
        ->unqualify (comp keyword name)]
    (concat
     req
     opt
     (map ->unqualify opt-un)
     (map ->unqualify req-un))))

(spec->keys ::config)
;; (:server_port :db :event)


(let [cfg-keys (spec->keys ::config)]
  (-> (System/getenv)
      remap-env
      (select-keys cfg-keys)))

#_
(def env (-> (System/getenv)
             remap-env))


(defn read-env-vars
  []
  (let [cfg-keys (spec->keys ::config)]
    (-> (System/getenv)
        remap-env
        (select-keys cfg-keys))))


(defn load-config!
  []
  (-> (read-env-vars)
      (coerce-config)
      (set-config!)))


(-> (System/getenv)
    remap-env-nest
    (select-keys [:db :http]))

{:db {:user "ivan", :pass "****", :name "book"},
 :http {:port "8080", :host "api.random.com"}}

(s/def ::->env
  (s/conformer
   (fn [varname]
     (or (System/getenv varname)
         ::s/invalid))))

(s/def ::db-password
  (s/and ::->env
         string?
         (s/conformer str/trim)
         not-empty))


;; tags

(defmulti !tag
  (fn [args]
    (when (vector? args)
      (first args))))

(defmethod !tag "!env"
  [[_ varname]]
  (System/getenv varname))

(defn tag-env
  [varname]
  (cond
    (symbol? varname)
    (System/getenv (name varname))
    (string? varname)
    (System/getenv varname)
    :else
    (throw (new Exception "wrong var type"))))

(require '[clojure.edn :as edn])

(edn/read-string
 {:readers {'env tag-env}}
 "{:db-password #env DB_PASS}")


(def read-config
  (partial edn/read-string
           {:readers {'env tag-env}}))


(require '[yummy.config :as yummy])

#_
(yummy/load-config {:path "config.yaml"})

{:server_port 8080
 :db {:dbtype "mysql"
      :dbname "book"
      :user "ivan"
      :password "*(&fd}A53z#$!"}}


#_
(-> "/path/to/config.edn"
    slurp
    read-config)


{:phrases
 ["Welcome aboard!"
  "See you soon!"
  {:Warning "wrong email address."}]}

{:task-state #{:pending :in-progress :done}
 :account-ids [1001 1002 1003]
 :server {:host "127.0.0.1" :port 8080}
 :date-range [#inst "2019-07-01" #inst "2019-07-31"]
 :cassandra-id #uuid "26577362-902e-49e3-83fb-9106be7f60e1"}

#_
(-> (get-huge-dataset)
    pr-str
    (as-> dump
        (spit "test.edn" dump)))


{:users [{:id 1
          :name "Ivan"}
         #_
         {:id 2
          :name "Juan"}
         {:id 3
          :name "Ioann"}]}

;;
;; cprop
;;

(require '[cprop.core :refer [load-config]])

(require '[cprop.source :refer [from-env
                                from-props-file
                                from-file
                                from-resource]])


#_
(load-config)

#_
(from-props-file "config.properties")

{:db {:type "mysql"
      :host "127.0.0.1"
      :pool {:connections 8}}}

#_
(load-config
 :resource "private/config.edn"
 :file "/path/custom/config.edn")

#_
(load-config
 :resource "path/within/classpath/to.edn"
 :file "/path/to/some.edn"
 :merge [{:datomic {:url "foo.bar"}}
         (from-file "/path/to/another.edn")
         (from-resource "path/within/classpath/to-another.edn")
         (from-props-file "/path/to/some.properties")
         (from-system-props)
         (from-env)])

;;
;; aero
;;

;; (require '[aero.core :refer (read-config)])
;; (read-config "aero.test.edn")
;; (read-config "aero.test.edn" {:profile :test})


;;
;; yummy
;;

#_
(require '[yummy.config :refer [load-config]])

;; (load-config {:path "config.yaml"})

;; (load-config {:program-name :book})

#_
(load-config {:program-name :book
              :spec ::config})


#_
(load-config
 {:program-name :book
  :spec ::config
  :die-fn (fn [e msg]
            (binding [*out* *err*]
              (println msg)
              (println (ex-message e))))})

#_
(load-config
 {:program-name :book
  :spec ::config
  :die-fn (fn [e msg]
            (log/errorf e "Config error")
            (System/exit 1))})

#_
(require '[aero.core :refer [read-config]]
         '[clojure.java.io :as io])
