
(clojure.inspector/inspect-tree )




(defn add [a b]
  (+ a b))

(require '[clojure.inspector :as insp])
(insp/inspect-tree (System/getenv))


{}
  JAVA_MAIN_CLASS_68934=clojure.main
  LC_TERMINAL=iTerm2
  COLORTERM=truecolor
  LOGNAME=ivan
  TERM_PROGRAM_VERSION=3.3.12
  PWD=/Users/ivan/work/book-sessions
  SHELL=/bin/zsh

(spit "dump.edn" (pr-str {"key1" 1 :key2 "2"}))

(read-string (slurp "dump.edn"))

(load-file "sample.clj")

(slurp "sample.clj")


(require '[clj-http.client :as client])
(require 'cheshire.core)

(def ip "178.210.54.129")

(def request
  {:url "https://iplocation.com"
   :method :post
   :form-params {:ip ip}
   :as :json})

(def response
  (client/request request))

(def data
  (:body response))


(defproject repl-chapter "0.1.0-SNAPSHOT"
  :dependencies [[org.clojure/clojure "1.10.0"]
                 [clj-http "3.9.1"]
                 [cheshire "5.8.1"]])

(require '[clojure.inspector :as insp])
(insp/inspect-tree data)


{...}
  - [:postal_code "394002"]
  - [:ip "178.210.54.129"]
  - [:continent_code "EU"]
  - [:region_name "Voronezh Oblast"]
  - [:city "Voronezh"]
  - [:isp "Jsc Kvant-telekom"]
  - [:ip_header "Your IP address"]
  - [:region "VOR"]
  - [:country_code "RU"]
  - [:country_name "Russia"]
  - [:metro_code nil]
  - [:found 1]
  - [:time_zone "Europe/Moscow"]
  - [:lat 51.672]
  - [:company "Jsc Kvant-telekom"]
  - [:lng 39.1843]


(select-keys data [:city :country_code])


(defn get-ip-info [ip]
  (let [request
        {:url "https://iplocation.com"
         :method :post
         :form-params {:ip ip}
         :as :json}]
    (-> request
        (client/request)
        :body
        (select-keys [:city :country_code]))))

(get-ip-info "81.17.28.231")


(require '[clojure.java.io :as io])


(cheshire.core/generate-stream data (io/writer "ip-data.json") {:pretty true})


(def request
  {:url "https://iplocation.com"
   :method :post
   :form-params {:ip "hello"}
   :as :json})

(def response
  (client/request request))

(client/request
 {:url "https://iplocation.com"
  :method :post
  :form-params {:ip "hello"}
  :as :json})


(def response
 (client/request
  {:url "https://iplocation.com"
   :method :post
   :form-params {:ip "hello"}
   :as :json
   :throw-exceptions? false}))



;; https://api.agify.io/?name=ivan

;; https://api.agify.io/?username=ivan


(client/request
 {:url "https://api.agify.io"
  :method :get
  :query-params {:username "ivan"}
  :as :json
  :coerce :always
  :throw-exceptions? false})


(:body
 (client/request
  {:url "https://api.agify.io"
   :method :get
   :query-params {:username "ivan"}
   :as :json
   :coerce :always
   :throw-exceptions? false}))


;; https://v2.jokeapi.dev/joke/Programming?type=twopart

;; https://v2.jokeapi.dev/joke/Dunno

;; https://v2.jokeapi.dev/joke/Programming?contains=java

{
    "error": false,
    "category": "Programming",
    "type": "single",
    "joke": "Java is like Alzheimer's, it starts off slow, but eventually, your memory is gone.",
    "flags": {
        "nsfw": false,
        "religious": false,
        "political": false,
        "racist": false,
        "sexist": false,
        "explicit": false
    },
    "id": 27,
    "safe": false,
    "lang": "en"
 }


;; https://v2.jokeapi.dev/joke/Programming?contains=clojure

{
    "error": true,
    "internalError": false,
    "code": 106,
    "message": "No matching joke found",
    "causedBy": [
        "No jokes were found that match your provided filter(s)."
    ],
    "additionalInfo": "Error while finalizing joke filtering: No jokes were found that match your provided filter(s).",
    "timestamp": 1651044225373
 }


(def request
  {:url "https://v2.jokeapi.dev/joke/Programming"
   :method :get
   :as :json})

(def response
  (client/request request))

(def data
  (:body response))

(def joke
  (let [{:keys [setup
                delivery]} data]
    (format "%s %s" setup delivery)))

;; ?contains=clojure


(def request
  {:url "https://v2.jokeapi.dev/joke/Programming"
   :method :get
   :query-params {:contains "javascript"}
   :as :json})

(def response
  (client/request request))


(defn get-joke [lang]
  (let [request
        {:url "https://v2.jokeapi.dev/joke/Programming"
         :method :get
         :query-params {:contains lang}
         :as :json}

        response
        (client/request request)

        {:keys [body]}
        response

        {:keys [setup delivery]}
        body]

    (format "%s %s" setup delivery)))

(get-joke "python")

(get-joke "lisp")


(def data
  (-> {:url "https://v2.jokeapi.dev/joke/Programming"
       :method :get
       :query-params {:contains "clojure"}
       :as :json}
      (client/request)
      (:body)))


(-> {:url "https://v2.jokeapi.dev/joke/Programming"
     :method :get
     :query-params {:contains "python"
                    :type :single}
     :as :json}
    (client/request)
    (:body))

(cheshire.core/generate-stream data (io/writer "joke-err.json") {:pretty true})
