(ns scratch
  (:require [clojure.walk :as walk]))

#_
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


(read-string "(foo bar)")

(require '[clojure.string :as str])
(str/split "1 2 3" #"\s")


(into {} (System/getenv))

*print-level*
*print-length*

;; (in-ns 'repl-test)
;; (clojure.core/refer-clojure)
;; (+ 1 2)

18:12=> ...
18:14=> ...

(with-local-vars [foo 1]
  (var-set foo 42)
  (eval `(let [~'foo ~(var-get foo)]
           ~'foo)))


(eval
 '(let [*r 6]
    (* 3 *r)))

(/ 1 0)
;; ... Stacktrace ...

(ex-message e)
"Divide by zero"

(try (/ 1 0) (catch Exception e (ex-message e)))

(defn add [a b]
  (+ a b))


(defn add [a b]
..(let [c (+ a b)]
....(* c 3)))


(defn make-stack []
  (let [-stack (atom nil)]
    (fn stack
      ([cmd]
       (case cmd
         :count (count @-stack)
         :peek (first @-stack)
         :pop (let [item (first @-stack)]
                (swap! -stack rest)
                item)))
      ([cmd arg]
       (case cmd
         :push
         (swap! -stack conj arg))))))


(def stack (make-stack))

(stack :get)
(stack :push 1)
(stack :push 2)
(stack :push 3)
(stack :get)
(stack :pop)
(stack :pop)
(stack :pop)


(defn balanced? [stack]
  (zero? (stack :count)))

(defn print-indent [stack]
  (dotimes [_ (* (stack :count) 2)]
    (print ".")))


(def brace-pairs
  {\( \)
   \[ \]
   \{ \}})

(def brace-oppos
  (into {} (for [[k v] brace-pairs]
             [v k])))


(defn consume-line [stack line]
  (doseq [char line]
    (cond
      (contains? brace-pairs char)
      (stack :push char)

      (contains? brace-oppos char)
      (let [char-oppos
            (get brace-oppos char)]
        (when-not (= char-oppos (stack :pop))
          (throw (ex-info "aaa")))))))

(defn multi-input []
  (let [stack (make-stack)]
    (print-indent stack)
    (flush)
    (loop [result ""]
      (let [line (read-line)
            result (str result " " line)]
        (consume-line stack line)
        (if (balanced? stack)
          result
          (recur result))))))


(def s (make-stack))

(s :push 1)
(s :push 2)
(s :push 3)


(consume-line s "aaaa")

(consume-line s "((aa")


(+ 1 2 3
..3 4 5)
18
(assoc-in {:foo 1
....:bar 2
....:baz 3
....}
..[:test :hello]
..3
..)
{:foo 1, :bar 2, :baz 3, :test {:hello 3}}


:pop (if (empty? @-stack)
       (throw (new Exception "Stack is empty!"))
       (let [item (first @-stack)]
         (swap! -stack rest)
         item))


(let [a 1 b 2] (my-repl/repl) (+ a b))

(let [a 1 b 2]
  (my-repl/repl)
  (+ a b))

(defn add [a b]
  (my-repl/repl)
  (+ a b))

(add 1 2)

(defn add [a b] (my-repl/repl) (+ a b))


(defn add [a b]
  (+ a b))

(+ 1 2)

(let [a 1 b 2]
  (println "inner form")
  (+ a b))


;; (ns test1)

;; (defn add [a b]
;;   (+ a b))

;; (ns test2)

;; (add 1 2)


{
 :user {:dependencies [[nrepl/nrepl "0.9.0"]]}
 }


(defn decode-bytes [data]
  (clojure.walk/postwalk
   (fn [x]
     (if (bytes? x)
       (new String ^bytes x "UTF-8")
       x))
   data))

(->
 ;; "d2:id4:12702:ns10:bogus.core7:session36:5cabb9bd-dcfe-4dc5-855d-978504d32d1ee"
 ;; "d2:id4:12707:session36:5cabb9bd-dcfe-4dc5-855d-978504d32d1e5:value1:6e"
 ;; "d18:changed-namespacesde2:id4:12709:repl-type3:clj7:session36:5cabb9bd-dcfe-4dc5-855d-978504d32d1e6:statusl5:stateee"
 ;; "d18:changed-namespacesde2:id4:13349:repl-type3:clj7:session36:5cabb9bd-dcfe-4dc5-855d-978504d32d1e6:statusl5:stateee"
 ;; "d2:id4:13347:session36:5cabb9bd-dcfe-4dc5-855d-978504d32d1e6:statusl4:doneee"
 "d9:gen-inputle2:id4:13507:resultsd15:bogus.core-testd20:test-tag-reader-whenld7:contextle5:indexi0e7:message0:2:ns15:bogus.core-test4:type4:pass3:var20:test-tag-reader-wheneeee7:session36:5cabb9bd-dcfe-4dc5-855d-978504d32d1e7:summaryd5:errori0e4:faili0e2:nsi1e4:passi1e4:testi1e3:vari1ee10:testing-ns15:bogus.core-teste"
 ;; "d6:columni1e4:file52:file:/Users/ivan/work/bogus/test/bogus/core_test.clj2:id4:13534:linei87e4:name20:test-tag-reader-when2:ns15:bogus.core-test7:session36:5cabb9bd-dcfe-4dc5-855d-978504d32d1e6:statusl4:doneee"
 (.getBytes "UTF-8")
 (java.io.ByteArrayInputStream.)
 (java.io.PushbackInputStream.)
 (nrepl.bencode/read-bencode)
 (decode-bytes))


(defn handler [{:as message
                :keys [transport op code]}]
  (let [value (eval ...)]
    (t/send transport {:value value})))


d2:id4:13342:ns10:bogus.core7:session36:5cabb9bd-dcfe-4dc5-855d-978504d32d1e5:value1:6e
d2:id4:13342:ns10:bogus.core7:session36:5cabb9bd-dcfe-4dc5-855d-978504d32d1e5:value2:10e
d2:id4:13347:session36:5cabb9bd-dcfe-4dc5-855d-978504d32d1e6:statusl4:doneee

d6:columni1e4:file52:file:/Users/ivan/work/bogus/test/bogus/core_test.clj2:id4:13534:linei87e4:name20:test-tag-reader-when2:ns15:bogus.core-test7:session36:5cabb9bd-dcfe-4dc5-855d-978504d32d1e6:statusl4:doneee

{:id 1334 :session "..." :value 6}
{:id 1334 :session "..." :value 10}
{:id 1334 :session "..." :status "done"}


d9:gen-inputle2:id4:13507:resultsd15:bogus.core-testd20:test-tag-reader-whenld7:contextle5:indexi0e7:message0:2:ns15:bogus.core-test4:type4:pass3:var20:test-tag-reader-wheneeee7:session36:5cabb9bd-dcfe-4dc5-855d-978504d32d1e7:summaryd5:errori0e4:faili0e2:nsi1e4:passi1e4:testi1e3:vari1ee10:testing-ns15:bogus.core-teste

{"gen-input" [],
 "id" "1350",
 "results"
 {"bogus.core-test"
  {"test-tag-reader-when"
   [{"context" [],
     "index" 0,
     "message" "",
     "ns" "bogus.core-test",
     "type" "pass",
     "var" "test-tag-reader-when"}]}},
 "session" "5cabb9bd-dcfe-4dc5-855d-978504d32d1e",
 "summary" {"error" 0, "fail" 0, "ns" 1, "pass" 1, "test" 1, "var" 1},
 "testing-ns" "bogus.core-test"}

(op test ns bogus.core-test tests (test-tag-reader-when) load? true)


(defn handle-classpath
  [handler {:as msg :keys [op transport]}]
  (if (= "classpath" op)
    (let [paths (get-classpath ...)]
      (t/send transport {... :classpath paths}))
    (handler msg)))


(require '[nrepl.core :as nrepl])

(def conn (nrepl/connect :port 54780))
(def client (nrepl/client conn 1000))

(nrepl/message client {:op "eval" :code "(+ 1 2 3)"})

({:id "1ac6cbc4-74d4-4b3a-bf3f-97dcf7ca07c2"
  :ns "my-repl"
  :session "fec9d5a3-3c22-4640-b089-c1cecc041068"
  :value "6"}
 {:id "1ac6cbc4-74d4-4b3a-bf3f-97dcf7ca07c2"
  :session "fec9d5a3-3c22-4640-b089-c1cecc041068"
  :status ["done"]})

(nrepl/message client {:op "eval" :code "(/ 0 0)"})

({:err "Execution error (ArithmeticException) at my-repl/eval5984 (form-init9833672407535844907.clj:1).\nDivide by zero\n"
  :id "a8444b3c-7b54-4e04-9b48-04b8bda170f4"
  :session "17d02cb6-45ff-464c-a01c-c87da89cdfa7"}
 {:ex "class java.lang.ArithmeticException"
  :id "a8444b3c-7b54-4e04-9b48-04b8bda170f4"
  :root-ex "class java.lang.ArithmeticException"
  :session "17d02cb6-45ff-464c-a01c-c87da89cdfa7"
  :status ["eval-error"]}
 {:id "a8444b3c-7b54-4e04-9b48-04b8bda170f4"
  :session "17d02cb6-45ff-464c-a01c-c87da89cdfa7" :status ["done"]})

(nrepl/message client {:op "lookup" :sym "+"})

({:id "e66092b8-c6aa-49a9-9cff-cc3557e421c6",
  :info {:protocol "",
         :added "1.2",
         :ns "clojure.core",
         :name "+",
         :file "jar:file:/Users/ivan/.m2/repository/org/clojure/clojure/1.10.0/clojure-1.10.0.jar!/clojure/core.clj",
         :arglists-str "([] [x] [x y] [x y & more])",
         :column 1,
         :line 984,
         :arglists "([] [x] [x y] [x y & more])",
         :doc "Returns the sum of nums. (+) returns 0. Does not auto-promote\n  longs, will throw on overflow. See also: +'"},
  :session "63a4ba3f-6a19-4597-919d-3136ebbfa1cb",
  :status ["done"]})

(nrepl/message client {:op "completions" :prefix "ex-"})


({:completions
  [{:candidate "ex-cause", :type "function"}
   {:candidate "ex-data", :type "function"}
   {:candidate "ex-info", :type "function"}
   {:candidate "ex-message", :type "function"}],
  :id "e6f3b77b-5689-484e-8743-76f9682caaa7",
  :session "f15bb73c-e3f9-49c8-8aa5-0ebc592d038d",
  :status ["done"]})


(nrepl/message client {:op "eval" :code "
(defn add [a b]
  (+ a b))
"})


(with-open [conn (nrepl/connect :port 59258)]
  (-> (nrepl/client conn 1000)          ; message receive timeout required
      (nrepl/message {:op "eval" :code "(+ 2 3)"})
      nrepl/response-values))


(nrepl/message client {:op "completions" :prefix "add"})


(nrepl/message client {:op "load-file"
                       :file (slurp "src/sample.clj")})


{:id "92a66b15-d5e4-4649-bcfb-98a05b9aee5b"
 :session "97ec6c4b-28ee-4402-87e3-43f3275a7430"
 :value "#'sample/multiply"}

{:id "92a66b15-d5e4-4649-bcfb-98a05b9aee5b"
 :session "97ec6c4b-28ee-4402-87e3-43f3275a7430"
 :status ["done"]}


(nrepl/message client
               {:op "close"
                :session "43fb86a8-0680-44ed-b3e9-dd85150fef35"})


(defn decode-bytes [data]
  (clojure.walk/postwalk
   (fn [x]
     (if (bytes? x)
       (new String ^bytes x "UTF-8")
       x))
   data))



(->
 ;; "d4:code9:(+ 1 2 3)2:id36:c88f2dcb-d502-4c90-9529-2dd843bc56d02:op4:evale"
 ;; "d2:id36:c88f2dcb-d502-4c90-9529-2dd843bc56d02:ns7:my-repl7:session36:1fdd9575-881e-4be1-80ca-dd8e64bdff185:value1:6e"
 (.getBytes "UTF-8")
 (java.io.ByteArrayInputStream.)
 (java.io.PushbackInputStream.)
 (nrepl.bencode/read-bencode)
 (decode-bytes))


(defn bendecode [^String payload]
  (-> payload
      (.getBytes "UTF-8")
      (java.io.ByteArrayInputStream.)
      (java.io.PushbackInputStream.)
      (nrepl.bencode/read-bencode)
      (decode-bytes)))

(bendecode "d4:code9:(+ 1 2 3)2:id36:c88f2dcb-d502-4c90-9529-2dd843bc56d02:op4:evale")
