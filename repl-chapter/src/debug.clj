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


(defmacro debug []
  (let [ns *ns*
        locals (get-locals)]
    (debug-inner ns locals)))


(intern *ns* 'hello "test")
;; #'debug/hello

hello
;; "test"


(ns-unmap *ns* 'hello)
nil

hello
Syntax error compiling at (repl-chapter:localhost:62378(clj)*:1:8441).
Unable to resolve symbol: hello in this context

(defn eval+ [the-ns locals form]
  (doseq [[sym value] locals]
    (intern the-ns sym value))
  (binding [*ns* the-ns]
    (eval form))
  (doseq [[sym value] locals]
    (ns-unmap the-ns sym)))

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
