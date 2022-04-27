(ns my-repl
  (:gen-class))


(defn -main [& args]
  (loop []
    (let [input (read-line)
          expr (read-string input)
          result (eval expr)]
      (println result)
      (recur))))


;; (-main)
