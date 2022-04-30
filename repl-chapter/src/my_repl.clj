(ns my-repl
  (:require
   [clojure.string :as str]
   [clojure.pprint :as pprint])
  (:gen-class))

#_
(defn repl []
  (loop []
    (let [input (read-line)
          expr (read-string input)
          result (eval expr)]
      (println result)
      (recur))))



;; (-main)


#_
(defn repl []
  (loop []
    (let [input (read-line)
          expr (read-string input)]
      (when-not (= expr :repl/exit)
        (let [result (eval expr)]
          (println result)
          (recur))))))





#_
(let [input (read-line)
      expr (read-string input)]
  (when-not (= expr :repl/exit)
    ...))



#_
(defn repl []
  (loop []
    (let [[result e]
          (try
            [(-> (read-line)
                 (read-string)
                 (eval))
             nil]
            (catch Throwable e
              [nil e]))]
      (if e
        (binding [*out* *err*]
          (println (ex-message e)))
        (println result))
      (recur))))



(defn default-exception-handler [e]
  (binding [*out* *err*]
    (println (ex-message e))))

#_
(defn repl []
  (loop []
    (let [[result e]
          (try
            [(-> (read-line)
                 (read-string)
                 (eval))
             nil]
            (catch Throwable e
              [nil e]))]
      (if e
        (exception-handler e)
        (println result))
      (recur))))



#_
(defn repl [& [{:keys [exception-handler]}]]
  (let [ex-handler
        (or exception-handler
            default-exception-handler)]
    (loop []
      (let [[result e]
            (try
              [(-> (read-line)
                   (read-string)
                   (eval))
               nil]
              (catch Throwable e
                [nil e]))]
        (if e
          (ex-handler e)
          (println result))
        (recur)))))

#_
(defn repl [& [{:keys [exception-handler]}]]
  (let [ex-handler
        (or exception-handler
            default-exception-handler)]
    (loop []
      (...
       (if e
         (ex-handler e))))))


#_
(defn repl []
  (loop []
    (let [input (read-line)
          expr (read-string input)
          result (eval expr)]
      (pprint/pprint result)
      (recur))))


#_
(defn repl [& [{:keys [print-level
                       print-length]}]]
  (binding [*print-level*
            (or print-level *print-level*)
            *print-length*
            (or print-length *print-length*)]
    (loop []
      (let [input (read-line)
            expr (read-string input)
            result (eval expr)]
        (pprint/pprint result)
        (recur)))))


#_
(defn repl [& [{:keys [print-level
                       print-length]}]]
  (binding [*print-level*
            (or print-level *print-level*)
            *print-length*
            (or print-length *print-length*)]
    ...))




#_
(defn repl []
  (loop []
    (print "repl=> ")
    (flush)
    (let [input (read-line)
          expr (read-string input)
          result (eval expr)]
      (println result)
      (recur))))


(defn get-prompt [the-ns]
  (format "%s=> " (ns-name the-ns)))

#_
(defn repl []
  (binding [*ns* *ns*]
    (loop []
      (print (get-prompt *ns*))
      (flush)
      (let [input (read-line)
            expr (read-string input)
            result (eval expr)]
        (println result)
        (recur)))))


#_
(defn repl []
  (binding [*ns* *ns*]
    (loop []
      (print (get-prompt *ns*))
      ...)))




#_
(defn repl []
  (with-local-vars [-*e nil -*r nil]
    (loop []
      (let [input (read-line)
            expr (read-string input)
            result
            (case expr
              *e (var-get -*e)
              *r (var-get -*r)
              (eval
               `(let [~'*r ~(var-get -*r)
                      ~'*e ~(var-get -*e)]
                  ~expr)))]
        (var-set -*r result)
        (println result)
        (recur)))))


(defn make-stack []
  (let [-stack (atom nil)]
    (fn stack
      ([cmd]
       (case cmd
         :count (count @-stack)
         :empty? (zero? (count @-stack))
         :pop (let [item (first @-stack)]
                (swap! -stack rest)
                item)))
      ([cmd arg]
       (case cmd
         :push
         (swap! -stack conj arg))))))


;; (stack :get)
;; (stack :push 1)
;; (stack :push 2)
;; (stack :push 3)
;; (stack :get)
;; (stack :pop)
;; (stack :pop)
;; (stack :pop)


(defn make-indent [stack]
  (str/join (repeat (* (stack :count) 2) ".")))

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
            (get brace-oppos char)
            char-lead
            (stack :pop)]
        (when-not (= char-lead char-oppos)
          (throw
           (new Exception
                (format "Unbalanced expressiong: %s...%s"
                        char-lead char))))))))

(defn multi-input []
  (let [stack (make-stack)]
    (loop [result ""]
      (print (make-indent stack))
      (flush)
      (let [line (read-line)
            result (str result " " line)]
        (consume-line stack line)
        (if (stack :empty?)
          result
          (recur result))))))



(defn -main [& args]
  (repl))
