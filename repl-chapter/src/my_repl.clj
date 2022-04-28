(ns my-repl
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

(defn repl [& [{:keys [exception-handler]}]]
  (let [ex-handler
        (or exception-handler
            default-exception-handler)]
    (loop []
      (...
        (if e
          (ex-handler e))))))


(defn -main [& args]
  (repl {:exception-handler
         (fn [e] (println (type e)))}))
