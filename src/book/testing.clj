(ns book.testing)


(defn ->fahr [cel]
  (+ (* cel 1.8) 32))

#_
(defn ->fahr [cel]
  (+ (* cel 1.9) 32))


(defn test-fahr []
  (assert (= (int (->fahr 20)) 68))
  (assert (= (int (->fahr 100)) 212)))



(defn square-roots [a b c]
  (let [D (- (* b b) (* 4 a c))]
    (cond
      (pos? D) [(/ (+ (- b) (Math/sqrt D)) (* 2 a))
                (/ (- (- b) (Math/sqrt D)) (* 2 a))]
      (zero? D) (/ (- b) (* 2 a))
      (neg? D) nil)))


(defn test-square-roots-two-roots []
  (let [[x1 x2] (square-roots 1 -5 6)]
    (assert (= [(int x1) (int x2)] [3 2]))))

(defn test-square-roots-one-root []
  (assert (= (square-roots 1 6 9) -3)))

(defn test-square-roots-no-roots []
  (assert (= (square-roots 2 4 7) nil)))


;; +(defn square-roots [a b c]
;; +  (let [D (- (* b b) (* 4 a c))]
;; +    (cond
;; +      (pos? D) [(/ (+ (- b) (Math/sqrt D)) (* 2 a))
;; +                (/ (- (- b) (Math/sqrt D)) (* 2 a))]
;; -      (zero? D) (/ (- b) (* 2 a))
;; -      (neg? D) nil)))


(defn sign-params [params secret-key])


(defn test-sign-params []
  (let [api-key "2Ag48&@%776^634Tsdf23"
        params {:action :postComment
                :user_id 42
                :post_id 1999
                :comment "This is a great article!"}
        signature "e36b331823b..."]
    (assert (= (sign-params params api-key)
               (assoc params :signature signature)))))


(defn remap-line [line])

#_
(defn process-csv [path]
  (let [content (slurp path)]
    (for [line (clojure.string/split content #"\n")]
      (remap-line line))))

(defn process-csv-content [content]
  (for [line (clojure.string/split content #"\n")]
    (remap-line line)))

(defn process-csv [path]
  (process-csv-content (slurp path)))


(def CONTENT
  (str "Ivan;ivan@test.ru;http://example.ru"
       \newline
       "John;john@test.com;http://example.com"))


#_
(assert (= (process-csv-content CONTENT)
           [{:name "Ivan" :email ...}
            {:name "John" :email ...}]))
