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

(try+
 (client/get "http://example.com/test")
 (catch [:status 500] e
   (println "The service is unavailable"))
 (catch [:type :client/unexceptional-status] e
   (println "The response was not 200"))
 (catch ... e
   (println "There was a connection error")))


(defn pcall
  [f & args]
  (try
    [true (apply f args)]
    (catch Exception e
      [false e])))


(let [[ok? result-error] (pcall inc 1)]
  (if ok?
    (println (str "The result is " result-error))
    (println "Failure")))


(defn pcall-js
  [f & args]
  (try
    [nil (apply f args)]
    (catch Exception e
      [e nil])))


(defn pcall-retry
  [n f & args]
  (loop [attempt n]
    (let [[ok? res] (apply pcall f args)]
      (cond
        ok? res

        (< attempt n)
        (do
          (Thread/sleep (* attempt 1000))
          (recur (inc n)))

        :else
        (throw res)))))


#_
(defn pcall-retry
  [n f & args]
  (loop [attempt n]
    (try
      (apply pcall f args)
      (catch Exception e
        (recur (inc n))))))



(defn error!
  [message & [data e]]
  (throw (ex-info message (or data {}) e)))


(defn errorf!
  [template & args]
  (let [message (apply format template args)]
    (throw (new Exception ^String message))))


(defmacro with-safe
  [& body]
  `(try
     ~@body
     (catch Exception e#)))

(with-safe (/ 0 0))
nil


(defmacro with-file
  [f path]
  `(let [~f (open ~path)]
     (try
       ~@body
       (finally
         (.close ~f)))))

(with-file f "/path/to/file.txt"
  (.write f "A")
  (.write f "B")
  (.write f "C"))


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

(require 'cheshire.core)
(cheshire.core/generate-string (:via (Throwable->map e)))


(-> e
    Throwable->map
    :via
    (cheshire.core/generate-string {:pretty true}))


(Throwable->map e)

(defn wrap-exception
  [handler]
  (fn [request]
    (try
      (handler request)
      (catch Exception e
        (try
          (sentry/send DSN e)
          (catch Exception e-sentry
            (log/errorf e-sentry "Sentry error: %s" DSN)
            (log/error e "HTTP Error"))
          (finally
            {:status 500
             :body "Internal error, please try later"}))))))



(defn account-handler
  [request]
  (if (check-this request)
    (if (check-that request)
      (if (check-quotas request)
        {:status 200
         :body (get-data-from-db)}
        (quotas-reached "Request rate is limited"))
      (not-found "No such an account"))
    (bad-request "Wrong input data")))


(require '[ring.util.http-response
           :refer [not-found!
                   bad-request!
                   enhance-your-calm!]])



(defn account-handler
  [request]

  (when-not (check-this request)
    (bad-request! "Wrong input data"))

  (when-not (check-that request)
    (not-found! "No such an account"))

  (when-not (check-quotas request)
    (enhance-your-calm! "Request rate is limited"))

  {:status 200
   :body (get-data-from-db)})


(require '[ring.middleware.http-response
           :refer [wrap-http-response]])


(def app
  (-> app-naked
      wrap-params
      wrap-session
      wrap-cookies
      wrap-http-response))

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
2019-05-03 17:36:04,001 INFO  book.exceptions - Hello Logback!
"

#_
(log/error e "Error while processing the request")

"
clojure.lang.ExceptionInfo: Get user info error
{:user-id 42}
"

"
2019-05-03 17:41:03,913 ERROR book.exceptions - Error while processing the request
clojure.lang.ExceptionInfo: Get user info error
	at java.lang.Thread.run(Thread.java:745)
    ...
Caused by: clojure.lang.ExceptionInfo: Auth error
	at clojure.lang.Compiler$InvokeExpr.eval(Compiler.java:3701)
	... 30 common frames omitted
Caused by: clojure.lang.ExceptionInfo: HTTP error
	at clojure.lang.Compiler$InvokeExpr.eval(Compiler.java:3701)
	... 31 common frames omitted
"


(defn ex-chain
  [^Throwable e]
  (take-while some? (iterate ex-cause e)))


(defn ex-print
  [^Throwable e]
  (let [indent "  "]
    (doseq [e (ex-chain e)]
      (println (-> e class .getCanonicalName))
      (print indent)
      (println (ex-message e))
      (when-let [data (ex-data e)]
        (print indent)
        (clojure.pprint/pprint data)))))



(defn log-error
  [^Throwable e & [^String message]]
  (log/error
   (with-out-str
     (println (or message "Error"))
     (ex-print e))))

"
2019-05-03 19:00:05,590 ERROR book.exceptions - An error occurred during request
clojure.lang.ExceptionInfo
  Get user info error
  ...
"






(def ex-map
  (Throwable->map e))


"finally"
"macroses"
"without errors"
"flow macroses"
"monads?"


(require '[slingshot.slingshot :refer [try+ throw+]])


#_
(try+
 (throw+ {:type ::user-error
          :user 42
          :action :update
          :data {:name "Ivan"}})
 (catch [:type ::user-error] e
   (clojure.pprint/pprint e)))


(defn special-user-case?
  [data]
  (when (map? data)
    (let [{:keys [type user]} data]
      (and (= type ::user-error)
           (= user 1)))))


(try+
 (throw+ {:type ::user-error
          :user 1
          :action :delete})
 (catch special-user-case? e
   (println "Attempt to delete a system account")))


(defn aws-special-case?
  [e]
  (and
   (instance? AmazonS3Exception e)
   (some?
    (re-find
     #"(?i)The Content-Md5 you specified did not match"
     (ex-message e)))))



#_
(let [path "/var/lib/file.txt"]
  (try
    (slurp path)
    (catch Exception e
      (throw+ {:path path} e "Cannot open a file %s" path))))


"
clojure.lang.ExceptionInfo
  Get user info error
  {:user-id 42}
clojure.lang.ExceptionInfo
  Auth error
  {:api-key "........."}
clojure.lang.ExceptionInfo
  HTTP error
  {:method "POST", :url "http://api.site.com"}
"






(try
  (-> {:context :map}
      (validate-data)
      (send-email)
      (update-the-db)
      (something-else))
  (catch IOException e
    (failure-branch-io))
  (catch Exception e
    (failure-branch-common)))


(-> {:context :map}

    (d/chain

     (validate-data)
     (send-email)
     (update-the-db)
     (something-else))

    (d/catch IOException
      (fn [e]
        (failure-branch e))))


(log/errorf e "There was an error, user: %s" user-id)



#_
(try
  (let [path "/var/lib/company.billing/3.552.534/clients/23532345677/billing/20190505.csv"]
    (slurp path))
  (catch Exception e
    (ex-message e)))

;; /var/lib/company.billing/3.552.534/clients/23532345677/billing/20190505.csv (No such file or directory)



#_
(try
  (let [path "/var/lib/company.billing/3.552.534/clients/23532345677/billing/20190505.csv"]
    (try
      (slurp path)
      (catch java.io.IOException e
        (throw (ex-info "Cannot access your billing information"
                        {:path path
                         :user-id 42})))))
  (catch Exception e
    (ex-message e)))

           ;; Cannot access your billing information


(require '[sentry-clj.core :as sentry])



;; (def _dsn "https://...............@sentry.io/........")
;; (sentry/init! _dsn)
;; (sentry/send-event {:throwable e})



(defmacro with-result
  [x handler]
  `(~handler ~x))


(defmacro with-catch
  [x handler]
  `(try
     ~x
     (catch Throwable e#
       (~handler e#))))


#_
(-> (do-this {:init :data})

    (with-result
      (fn [result]
        (process-result)))

    (with-catch
      (fn [e]
        (recover e)))

    (with-result
      (fn [result]
        (something-other result)))

    (with-catch
      (fn [e]
        (report-exception e)
        (error! "Failure" {:context :map} e))))



(defn get-user [id]
  (let [url (format "http://api.host.com/user/%s" id)]
    (:body (client/get url {:as :json}))))



(comment

  (try
    (get-user 42)
    (catch Exception e
      (log/error e "HTTP error")))

  )


#_
(slurp "/var/lib/secret_service/ver.2.3005.beta/accounts/5634534563/billing/csv")


(let [user-id 42
      url (format "http://api.host.com/user/%s" user-id)
      params {:throw-exceptions? false
              :coerce :always
              :as :json}]

  (->

   (client/get url params)

   (with-result
     (fn [response]
       (let [{:keys [status body]} response]
         (if (= status 200)
           body
           (throw (ex-info
                   (format "Non-200 response: %s" status)
                   response))))))

   (with-catch
     (fn [^Exception e]
       (let [message (ex-message e)
             {:keys [status body]} (ex-data e)]
         (throw
          (ex-info
           (format "Registrar error: %s" message)
           {:type ::registrar-error
            :url url
            :user-id user-id
            :params params
            :message message
            :http-status status
            :http-body (when (coll? body) body)})))))))







(try
  (do-stuff 1)
  (catch Exception e
    (throw
     (ex-info "Didn't manage to do the stuff"
              {:user 1
               :action "create"
               :more {:data {:code "STUFF"}}}
              e))))


(import '[java.io File FileWriter])

(let [out (new FileWriter (new File "test.txt"))]
  (try
    (.write out "Hello")
    (.write out " ")
    (.write out "Clojure")
    (finally
      (.close out))))


(defmacro with-file-writer
  [[bind path] & body]
  `(let [~bind (new FileWriter (new File ~path))]
     (try
       ~@body
       (finally
         (.close ~bind)))))


(with-file-writer [out "test.txt"]
  (.write out "Hello from macros"))


(comment

  1


  (try
    (/ 100 0)
    (catch ArithmeticException e
      (println "error")))

  (let [a 100 b 0]
    (when (zero? b)
      (throw (new Exception "b is zero"))))


  (defn with-exception
    [handler]
    (fn [request]
      (try
        (handler request)
        (catch Throwable e
          (log/error e "HTTP error")
          {:status 500
           :body "Internal error."})))))
