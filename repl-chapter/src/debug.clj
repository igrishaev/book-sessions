(ns debug
  (:require [clojure.main :as main]))

(defn format-user
  [{:keys [username email]}]
  (main/repl :prompt #(print "REPL>> "))
  (format "%s <%s>" username email))


(defmacro get-locals []
  (into {} (for [sym (keys &env)]
             [(list 'quote sym) sym])))


(let [a 1
      b 2]
  (get-locals))


{'a a 'b b}

{a 1 b 2}

(let [ns *ns*]
  (do-something-witn-ns ns))


(defn debug-inner [ns locals]
  (loop []
    (let [input (read-line)
          form (read-string input)]
      (when-not (= form :repl/exit)
        (let [result (eval+ ns locals form)]
          (println result)
          (recur))))))

(defmacro debug []
  `(debug-inner *ns* (get-locals)))


(let [a 1 b 2]
  (debug)
  (+ a b))


(intern *ns* 'hello "test")
;; #'debug/hello

hello
;; "test"


(ns-unmap *ns* 'hello)
nil

hello
Syntax error compiling at (repl-chapter:localhost:62378(clj)*:1:8441).
Unable to resolve symbol: hello in this context

(defn eval+ [ns locals form]
  (doseq [[sym value] locals]
    (intern ns sym value))
  (let [result
        (binding [*ns* ns]
          (eval form))]
    (doseq [[sym value] locals]
      (ns-unmap ns sym))
    result))


(eval+ *ns* {'a 1 'b 2} '(+ a b))

a
Syntax error compiling at (repl-chapter:localhost:62378(clj)*:1:8441).
Unable to resolve symbol: a in this context

#_
(let [a 1
      b 2]
  (main/repl :prompt #(print "REPL>> ")))


#_
(format-user {:username "John" :email "john@test.com"})

#_
(format-user {:username "John"
              :email "john@test.com"})
;; "John <john@test.com>"
