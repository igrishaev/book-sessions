(ns book.mutability)


(let [result (atom [])
      data [1 2 3 4 5]]
  (doseq [item data]
    (let [new-item (* 2 item)]
      (swap! result conj new-item)))
  @result)

(map (partial * 2) [1 2 3 4 5])

(for [n [1 2 3 4 5]]
  (* n 2))


(def store (atom 42))

@store ;; 42

(reset! store nil)
(reset! store {:items [1 2 3]})
(reset! store (ex-info "error" {:id 42}))


(def counter (atom 0))

(swap! counter inc)

(swap! counter + 3) ;; increase by 3
(swap! counter - 2) ;; decrease by 2


;; (+ <current> 3)
;; (- <current> 2)


(def usage
  (atom {:cpu 35
         :store 63466734
         :memory 10442856}))


(defn get-used-store
  []
  (rand-int 99999999))


(let [store (get-used-store)]
  (swap! usage assoc :store store))


(defn get-file-event
  []
  (rand-nth
   [{:action :delete
     :path "/path/to/deleted/file.txt"
     :size 563467}
    {:action :create
     :path "/path/to/new/photo.jpg"
     :size 7345626}]))

(let [{:keys [action size]} (get-file-event)]

  (cond
    (= action :delete)
    (swap! usage update :store - size)

    (= action :create)
    (swap! usage update :store + size)))


(def usage-all
  (atom
   {1005 {:cpu 35
          :store 63466734
          :memory 10442856
          :pids #{6266, 5426, 6542}}}))

(swap! usage-all update-in [1005 :pids] conj 9999)

(swap! usage-all update-in [1005 :pids] disj 9999)


;; {:count 0}
;; update :count + 2 ;; {:count 2}
;; update :count + 3 ;; {:count 3}


(def sample (atom {:number 0}))

(defn +slow
  [num delta timeout]
  (println (format "Current: %s, timeout: %s" num timeout))
  (Thread/sleep timeout)
  (+ num delta))


(defn +slow
  [a b timeout]
  (println (format "Current: %s, timeout: %s" a timeout))
  (Thread/sleep timeout)
  (+ a b))



#_
(swap! sample update :number +slow 1 2000)

(do
  (future (swap! sample update :number +slow 1 2000))
  (future (swap! sample update :number +slow 2 5000)))


(def counter (atom 2))

(set-validator! counter (complement neg?))

(swap! counter dec)

"
Execution error (IllegalStateException) at ....
Invalid reference state
"

(require '[clojure.tools.logging :as log])


#_
(defn install-better-logging
  []
  (alter-var-root
   (var clojure.tools.logging/log*)
   (fn [log*]
     (fn [logger level throwable message]
       (log* logger level nil
             (if throwable
               (str message \newline (e->message e))
               message))))))


(defn install-better-logging
  []
  (alter-var-root
   (var clojure.tools.logging/log*)
   (fn [log-old]
     (fn log-new [logger level throwable message]
       (if throwable
         (log-old logger level nil
                  (str message \newline
                       (with-out-str
                         (ex-print throwable))))
         (log-old logger level throwable message))))))


#_
(defn install-better-logging
  []
  (alter-var-root
   (var clojure.tools.logging/log*)
   (fn [log*]
     (fn [logger level throwable message]

       (let [message* (if throwable
                        (str message \newline
                             (with-out-str
                               (ex-print throwable)))
                        message)]

         (log* logger level nil message*))))))


(def STORE_LIMIT (* 1024 1024 1024 25)) ;; 25 Gb

(defn store-watcher
  [_key _atom _old value]

  (let [{:keys [store]} value]
    (when (> store STORE_LIMIT)
      (log/errorf "Disk usage %s has reached the limit %s"
                  store STORE_LIMIT))))


(def usage
  (atom {:cpu 35
         :store 63466734
         :memory 10442856}))


(add-watch usage :store store-watcher)

(swap! usage update :store + STORE_LIMIT)


#_
(defn memoize
  [f]
  (let [mem (atom {})]
    (fn [& args]
      (if-let [e (find @mem args)]
        (val e)
        (let [ret (apply f args)]
          (swap! mem assoc args ret)
          ret)))))

(def +mem (memoize +slow))

(time (+mem 1 2 2000))
"Elapsed time: 2004.699832 msecs"

(time (+mem 1 2 2000))
"Elapsed time: 0.078052 msecs"


(def page-counter
  (atom {"/" 0}))


(defn wrap-page-counter
  [handler]
  (fn [request]
    (let [{:keys [request-method uri]} request]
      (when (= request-method :get)
        (swap! page-counter update uri (fnil inc 0)))
      (handler request))))


(defn page-seen
  [uri]
  (get @page-counter uri 0))


(defn footer
  [uri]
  [:div {:class "footer"}
   (let [seen (page-seen uri)]
     [:p "This page has been seen " seen " times."])])


;; 2019-05-22 18:37:03,635 ERROR book.mutability - Disk usage's 26907012334 reached the limit 26843545600


(def vusage
  (volatile! nil))

(vreset! vusage
         {:cpu 35
          :store 63466734
          :memory 10442856})

(vswap! vusage update :store + (* 1024 1024 5))

(println "Disk usage is" (get @vusage :store))


(def vsample (volatile! {:number 0}))

(do
  (future (vswap! vsample update :number +slow 1 2000))
  (future (vswap! vsample update :number +slow 2 5000)))

;; Current: 0, timeout: 2000
;; Current: 0, timeout: 5000

@vsample ;; {:number 2}


(def data
  {:items [{:result {:value 74}}
           {:result {:value 74}}]
   :records [{:usage 99 :date "2018-09-09"}
             {:usage 52 :date "2018-11-05"}]})

(let [result (volatile! [])]

  ;; see section 5.4 from the doc: http://...
  (when-let [a (some-> data :items first :result :value)]
    (when-let [b (some-> data :records last :usage)]
      (when (> a b)
        (vswap! result conj (* a b)))))

  ;; more and more expressions

  @result)


(let [items* (transient [1 2 3])]
  (conj! items* :a)
  (conj! items* :b)
  (pop! items*)
  (persistent! items*))

;; [1 2 3 :a]


(let [params* (transient {:a 1})]
  (assoc! params* :b 2)
  (assoc! params* :c 3)
  (dissoc! params* :b)
  (persistent! params*))

;; {:a 1, :c 3}


(let [params* (transient {:a 1})]
  (assoc! params* :b 2)
  (let [result (persistent! params*)]
    (assoc! params* :c 3)
    result))

;; IllegalAccessError: Transient used after persistent! call

(let [result* (transient [])
      push! (fn [item]
              (conj! result* item))]

  ;; see section 5.4 from the doc: http://...
  (when-let [a (some-> data :items first :result :value)]
    (when-let [b (some-> data :records last :usage)]
      (when (> a b)
        (push! (* a b)))))

  ;; more and more expressions

  (persistent! result*))


(def nums (range 999999))


(loop [result []
       nums nums]
  (if-let [n (first nums)]
    (recur (conj result n) (rest nums))
    result))
;; Elapsed time: 166.688721 msecs


(loop [result* (transient [])
       nums nums]
  (if-let [n (first nums)]
    (recur (conj! result* n) (rest nums))
    (persistent! result*)))
;; Elapsed time: 69.415038 msecs

;; 166.688721 msecs
;;  69.415038 msecs

(time (do (persistent!
           (reduce
            (fn [result* n]
              (conj! result* n))
            (transient [])
            nums))
          nil))

(time (do
        (reduce
         (fn [result n]
           (conj result n))
         []
         nums)
        nil))


(reduce
 (fn [result n]
   (conj result n))
 []
 nums)

(persistent!
 (reduce
  (fn [result* n]
    (conj! result* n))
  (transient [])
  nums))

#_
(time (do (doall (map (partial * 2) nums)) nil))

#_
(time
 (do
   (let [result* (transient [])]
     (doseq [n nums]
       (conj! result* (* n 2)))
     (persistent! result*))
   nil))

(def size (atom 0))


#_
(def server (jetty/server {:port 8080}))

(require '[ring.adapter.jetty :refer [run-jetty]])

(defn app
  [request]
  {:status 200
   :body "hello"})


(def server nil)


(defn start!
  []
  (alter-var-root
   (var server)
   (fn [server]
     (if-not server
       (run-jetty app {:port 8080 :join? true})
       server))))


(defn stop!
  []
  (alter-var-root
   (var server)
   (fn [server]
     (when server
       (.stop server))
     nil)))


#_
(def alter-server! (partial alter-var-root (var server)))

#_
(defn start!
  []
  (alter-server!
   (fn [server]
     (if-not server
       (jetty/run-jetty app {:port 8080})
       server))))

#_
(defn stop!
  []
  (alter-server!
   (fn [server]
     (when server
       (.stop server))
     nil)))

(require '[clojure.java.jdbc :as jdbc])


(jdbc/query *db* "select 1 as result")

(defn get-user-by-id
  [user-id]
  (jdbc/get-by-id *db* :users user-id))


(require 'clojure.pprint)


(println [1 2 3 4 5 {:foo 42 :bar [1 2 3 4 5 {:foo 42 :bar [1 2 3 4 5 {:foo 42 :bar nil}]}]}])

"
[1 2 3 4 5 {:foo 42, :bar [1 2 3 4 5 {:foo 42, :bar [1 2 3 4 5 {:foo 42, :bar nil}]}]}]
"

(alter-var-root
 (var println)
 (fn [_] clojure.pprint/pprint))

"
[1
 2
 3
 4
 5
 {:foo 42, :bar [1 2 3 4 5 {:foo 42, :bar [1 2 3 4 5 {#, #}]}]}]
"

"
(println [{:foo 42 :bar [1 2 3 4 5 {:foo 42 :bar [1 2 {:foo ... ;; more
"



{:profiles
 :dev  {:source-paths ["env/dev"]}
 :test {:source-paths ["env/test"]}}


(require '[clojure.java.io :as io])

(def *o* *out*)
(def f (io/writer "aaa.txt"))
(set! *out* f)
(println {:some {:data 1}})


{:profiles
 :dev {:global-vars {*warn-on-reflection* true
                     *assert* true}}
 :uberjar {:global-vars {*warn-on-reflection* false
                         *assert* false}}}

(def ^:dynamic *data* nil)

(set! *data* {:user 1})


(binding [*print-level* 8
          *print-length* 4]
  (println {:foo {:bar {:baz (repeat 1)}}}))


(with-open [out (io/writer "dump.edn")]
  (binding [*out* out]
    (clojure.pprint/pprint {:test 42})))


(defn dump-data
  [path data]
  (with-open [out (io/writer path)]
    (binding [*out* out
              *print-level* 32
              *print-length* 256]
      (clojure.pprint/pprint data))))


#_
(-> "sample.edn" slurp read-string)


(def tr-map
  {:en {:ui/add-to-cart "Add to Cart"}
   :ru {:ui/add-to-cart "Добавить в корзину"}})

#_
(defn tr
  [locale tag]
  (get-in tr-map [locale tag]
          (format "<%s%s>" locale tag)))


(def ^:dynamic *locale*)

(defmacro with-locale
  [locale & body]
  `(binding [*locale* ~locale]
     ~@body))

(defn tr
  [tag]
  (get-in tr-map [*locale* tag]))

(with-locale :en
  (tr :ui/add-to-cart))
;; "Add to Cart"

(with-locale :ru
  (tr :ui/add-to-cart))
;; "Добавить в корзину"

(defn wrap-locale
  [handler]
  (fn [request]
    (let [locale (some-> request :params :lang (or :en))]
      (with-locale locale
        (handler request)))))

(require '[selmer.filters :refer [add-filter!]])

(add-filter! :tr
 (fn [line]
   (-> line keyword tr)))


(with-local-vars [a 1 b 2]
  (var-set a 2)
  (var-set b 3)
  (* @a @b)
  #_
  (* (var-get a)
     (var-get b)))


(with-local-vars [a 0]
  a)
;; #<Var: --unnamed-->

(defn calc-billing [data]
  (with-local-vars
    [a 0 b 0 c 0]

    ;; find a
    (when-let [usage (->some data :usage last)]
      (when-let [days (->some data :days first)]
        (var-set a (* usage days))))

    ;; find b
    (when-let [limits ...]
      (when-let [vms ...]
        (var-set b (* limits vms))))

    ;; find c
    ;; ...

    ;; result
    (+ (* @a @b) @c)))

(with-local-vars [user {:name "Ivan"}]
  ;; (var-set user assoc :age 33) ;; won't work
  (var-set user (assoc (var-get user) :age 33))
  @user)


(with-redefs
  [println (fn [_] (print "fake print"))]
  @(future (println 42)))

#_
(defn search-restrs
  []
  (let [url "https://api.google.com/geosearch"
        params {:as :json
                :query-params
                {:key "........."
                 :type :restaurant
                 :radius "100m"}}]
    (:body (client/get url params))))

#_
(defn nearme
  [request]
  (let [response (search-restrs)
        html (selmer/render
              "nearme.html" {:response response})]
    {:status 200
     :body html}))

#_
(deftest test-google-resp-ok
  (with-redefs
    [search-restrs (constantly
                    {:items [{:title ""}
                             {:title ""}]})]

    (let [url "http://127.0.0.1:8080/nearme"
          {:keys [status body]} (client/get url)]

      (is (= 200 status))
      (is (re-matches #"2 restaurants found" body)))))

#_
(deftest test-google-resp-ok
  (with-redefs
    [search-restrs
     (fn [_ &]
       (throw (ex-info "403 Access denied"
                       {:status 403
                        :body "..."})))]

    (let [;; url "http://127.0.0.1:8080/nearme"
          {:keys [status body]} (app {:request-method :get
                                      :uri "/nearme"})]

      (is (= 200 status))
      (is (re-matches #"2 restaurants found" body)))))


(defn location-handler
  [request]
  (let [{:keys [params]} request
        point (select-keys params [:lat :lon])
        place (geo/place-info point)]
    (db/create-location (merge {} place point))
    {:status 200 :body "OK"}))

(defn location-handler
  [request]
  (let [{:keys [params]} request
        point (select-keys params [:lat :lon])
        row-id (db/create-point point)]
    (future
      (let [place (geo/place-info point)]
        (db/update-place row-id place)))
    {:status 200 :body "OK"}))

(require 'geo)

(defmacro with-place-info
  [result & body]
  `(with-redefs [geo/place-info
                 (fn [~'point] ~result)]
     ~@body))


(with-redefs [geo/place-info
              (fn [point]
                {:some :result})]
  ;; the body of the test
  )

(with-place-info
  (throw (new Exception "AAAA"))
  1

  )


(deftest test-place-ok
  (with-place-info
    {:title "test_title"
     :country "test_country"}

    (let [request {:params {:lat 11.111 :lon 22.222}}
          {:keys [status body]} (location-handler request)]

      (is (= 200 status))
      (is (= "OK" body))

      (Thread/sleep 100)

      (let [location (db/get-last-location)
            {:keys [title country]} location]

        (is (= title "test_title"))
        (is (= country "test_country"))))))

(def ex-quota
  (ex-info "429 Quota reached"
           {:status 428
            :headers {}
            :body {:error_code :QUOTA_REACHED
                   :error_message "..."}}))

(deftest test-place-quota-reached
  (with-place-info
    (throw ex-quota)

    (let [request {:params {:lat 11.111 :lon 22.222}}
          {:keys [status body]} (location-handler request)]

      ;; ...
      )))

(deftest test-place-conn-err
  (with-place-info
    (throw (new java.net.ConnectException "test_timeout"))
    ;; ...
    ))


(with-redefs-fn
  {#'geo/place-info (fn [point] {:title "test"})}
  (fn []
    (geo/place-info {:lat 1 :lon 2})))

(with-redefs-fn
  {#'geo/place-info (fn [point] {:title "test"})}

  #(let [point {:lat 1 :lon 2}
         place (geo/place-info point)]
     ;; ...
     ))

(def ^:dynaimc *data* nil)

(set! *data* {:user 1})


(-> [1 2 3]
    (transient)
    (conj! :a)
    (conj! :b)
    (pop!)
    (persistent!))

;; [1 2 3 :a]


(-> {:a 1}
    (transient)
    (assoc! :b 2)
    (assoc! :c 3)
    (dissoc! :b)
    (persistent!))

;; {:a 1 :c 3}


(let [result* (transient [])
      push! (fn [item]
              (conj! result* item))]

  (when-let [a ...]
    (when-let [b ...]
      (when (> a b)
        (push! (- a b)))))

  ;; ... more of when/push! blocks

  (persistent! result*))

(defn fast-merge [map1 map2]
  (let [map-tr (transient map1)]
    (doseq [[k v] map2]
      (assoc! map-tr k v))
    (persistent! map-tr)))

#_
(fast-merge
 {:a 1 :b 2 :c 3 :d 4 :e 5 :f 6 :g 7 :h 8}
 {:extra 9})

(defn fast-merge [map1 map2]
  (persistent!
   (reduce-kv
    (fn [map-tr k v]
      (assoc! map-tr k v))
    (transient map1)
    map2)))
