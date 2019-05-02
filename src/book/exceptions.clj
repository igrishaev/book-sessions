(ns book.exceptions)

"

              +------+
              |Object|
              +---+--+
                  |
                  v
             +----+----+
          +--+Throwable+--+
          |  +---------+  |
          v               v
       +--+--+       +----+----+
       |Error|       |Exception+-----+
       +-----+       +---------+     |
                                     |
                            +--------v-------+
                            |RuntimeException|
                            +----------------+

java.lang.Object
  java.lang.Throwable
    java.lang.Exception
      java.io.IOException
        java.io.FileNotFoundException


"

"
Execution error (ArithmeticException) at user/eval5848 (form-init4879053674803415204.clj:1).
Divide by zero

Execution error (ArithmeticException) at user/eval5848...
Divide by zero
"

#_
(/ 1 0)

#_
(try
  (/ 1 0)
  (catch ArithmeticException e
    (println "Weird arithmetics")))

#_
(try
  (/ 1 0)
  (catch ArithmeticException e
    (println (ex-message e))))

#_
(try
  (+ 1 nil)
  (catch ArithmeticException e
    (println "Weird arithmetics")))

"
Execution error (NullPointerException) at user/eval6159
"

#_
(try
  (+ 1 nil)
  (catch ArithmeticException e
    (println "Weird arithmetics"))
  (catch NullPointerException e
    (println "You've got a null value")))

#_
(try
  (/ 1 0)
  (+ 1 nil)
  (catch Throwable e
    (println "I catch everything")))


#_
(let [e (new Exception "something is wrong")]
  (throw e))

(defn add [a b]
  (when-not (and a b)
    (let [message (format "Value error, a: %s, b: %s" a b)]
      (throw (new Exception message))))
  (+ a b))

"
Execution error at book.exceptions/add (exceptions.clj:86).
Value error, a: 1, b: null
"

(str "Value error, a:" 1 ", b: " nil)


#_
(throw (ex-info
        "Cannot get the data from remote server."
        {:user 9825632
         :http-method "POST"
         :http-url "http://some.host/api"}))




(defn authorize-user
  [user-id]
  (throw (ex-info
          "Cannot get the data from remote server."
          {:user user-id
           :http-method "POST"
           :http-url "http://some.host/api"})))

(try
  (authorize-user 42)
  (catch Exception e
    (let [data (ex-data e)
          {:keys [http-method http-url]} data]
      (format "HTTP error: %s %s" http-method http-url))))

"HTTP error: POST http://some.host/api"


(assoc nil :test 42)
(update nil :test (fnil inc 0))
(into nil [1 2 3])
(merge nil {:test 42})

(let [{:keys [a b c]} nil]
  [a b c])

(let [[a b c] nil]
  [a b c])

(require '[clojure.spec.alpha :as s])

(s/def ::data (s/coll-of int?))

#_
(when-let [explain (s/explain-data ::data [1 2 3])]
  (throw (ex-info "Validation failed" {:explain explain})))

(require '[clj-http.client :as client])

(defn auth-user
  [user-id]
  (let [url "http://auth.company.com"
        params {:form-params {:user-id user-id}
                :throw-exceptions? false
                :coerce :always
                :as :json
                :content-type :json}
        response (client/post url params)
        {:keys [status body]} response]

    (if (= status 200)
      body
      (throw (ex-info "Authentication error"
                      {:user-id user-id
                       :url url
                       :http-status status
                       :http-body body})))))


(defn divide [a b]
  (try
    (/ a b)
    (catch ArithmeticException e
      (throw (ex-info
              "Calculation error"
              {:a a :b b}
              e)))))

(try
  (divide 1 0)
  (catch Exception e
    (-> e ex-message println)
    (-> e ex-cause ex-message println)))

"
Calculation error
Divide by zero
"

#_
(defn get-user
  [user-id]
  (try
    ;; fetch a user
    (catch Exception e
      (throw (ex-info ... e)))))

#_
(defn get-user-details
  [user-id]
  (try
    (let [user (get-user user-id)
          history (get-history user-id)]
      (merge user history))
    (catch Exception e
      (throw
       (ex-info "Cannot get user's details"
                {:user-id user-is}
                e)))))

(require '[clojure.tools.logging :as log])

(def e
  (ex-info
   "Get user info error"
   {:user-id 42}
   (ex-info "Auth error"
            {:api-key "........."}
            (ex-info "HTTP error"
                     {:method "POST"
                      :url "http://api.site.com"}))))

#_
(println e)

"
#error {
 :cause HTTP error
 :data {:method POST, :url http://api.site.com}
 :via
 [{:type clojure.lang.ExceptionInfo
   :message Get user info error
   :data {:user-id 42}
   :at [clojure.lang.AFn applyToHelper AFn.java 160]}
  {:type clojure.lang.ExceptionInfo
   :message Auth error
   :data {:api-key .........}
   :at [clojure.lang.AFn applyToHelper AFn.java 160]}
  {:type clojure.lang.ExceptionInfo
   :message HTTP error
   :data {:method POST, :url http://api.site.com}
   :at [clojure.lang.AFn applyToHelper AFn.java 156]}]
 :trace
 [[clojure.lang.AFn applyToHelper AFn.java 156]
  [clojure.lang.AFn applyTo AFn.java 144]
  [clojure.lang.Compiler$InvokeExpr eval Compiler.java 3702]
  [clojure.lang.Compiler$InvokeExpr eval Compiler.java 3701]
  [clojure.lang.Compiler$InvokeExpr eval Compiler.java 3701]
  [clojure.lang.Compiler$DefExpr eval Compiler.java 457]
  [clojure.lang.Compiler eval Compiler.java 7181]
  [clojure.lang.Compiler load Compiler.java 7635]
  [book.exceptions$eval10157 invokeStatic form-init1214684113266076627.clj 1]
  [book.exceptions$eval10157 invoke form-init1214684113266076627.clj 1]
  [clojure.lang.Compiler eval Compiler.java 7176]
  [clojure.lang.Compiler eval Compiler.java 7131]
  [clojure.core$eval invokeStatic core.clj 3214]
  [clojure.core$eval invoke core.clj 3210]
  [clojure.main$repl$read_eval_print__9068$fn__9071 invoke main.clj 414]
  [clojure.main$repl$read_eval_print__9068 invoke main.clj 414]
  [clojure.main$repl$fn__9077 invoke main.clj 435]
  [clojure.main$repl invokeStatic main.clj 435]
  [clojure.main$repl doInvoke main.clj 345]
  [clojure.lang.RestFn invoke RestFn.java 1523]
  [clojure.tools.nrepl.middleware.interruptible_eval$evaluate$fn__1121 invoke interruptible_eval.clj 87]
  [clojure.lang.AFn applyToHelper AFn.java 152]
  [clojure.lang.AFn applyTo AFn.java 144]
  [clojure.core$apply invokeStatic core.clj 665]
  [clojure.core$with_bindings_STAR_ invokeStatic core.clj 1973]
  [clojure.core$with_bindings_STAR_ doInvoke core.clj 1973]
  [clojure.lang.RestFn invoke RestFn.java 425]
  [clojure.tools.nrepl.middleware.interruptible_eval$evaluate invokeStatic interruptible_eval.clj 85]
  [clojure.tools.nrepl.middleware.interruptible_eval$evaluate invoke interruptible_eval.clj 55]
  [clojure.tools.nrepl.middleware.interruptible_eval$interruptible_eval$fn__1166$fn__1169 invoke interruptible_eval.clj 222]
  [clojure.tools.nrepl.middleware.interruptible_eval$run_next$fn__1161 invoke interruptible_eval.clj 190]
  [clojure.lang.AFn run AFn.java 22]
  [java.util.concurrent.ThreadPoolExecutor runWorker ThreadPoolExecutor.java 1142]
  [java.util.concurrent.ThreadPoolExecutor$Worker run ThreadPoolExecutor.java 617]
  [java.lang.Thread run Thread.java 745]]}
"

#_
(with-out-str
  (clojure.stacktrace/print-stack-trace e))

"
clojure.lang.ExceptionInfo: Get user info error
{:user-id 42}
"

(def ex-map
  (Throwable->map e))


"finally"

"macroses"

"without errors"

"flow macroses"

"monads?"


(require '[slingshot.slingshot :refer [try+ throw+]])


(defn ex-chain
  [^Throwable e]
  (take-while some? (iterate ex-cause e)))


(defn e->message
  [e]
  (let [indent "  "]
    (with-out-str
      (doseq [e (ex-chain e)]
        (println (-> e class .getCanonicalName))
        (print indent)
        (println (ex-message e))
        (when-let [data (ex-data e)]
          (print indent)
          (clojure.pprint/pprint data))))))


(defn install-better-logging
  []
  (alter-var-root
   (var log/log*)
   (fn [log*]
     (fn [logger level throwable message]
       (log* logger level nil
             (if throwable
               (str message \newline (e->message e))
               message))))))
