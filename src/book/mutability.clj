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
