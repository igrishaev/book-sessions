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


(defn break-inner [ns locals]
  (loop []
    (let [input (read-line)
          form (read-string input)]
      (when-not (= form '!exit)
        (let [result
              (case form
                !locals locals
                !help "Help message..."
                (eval+ ns locals form))]
          (println result)
          (recur))))))

(defmacro break [form]
  `(do
     (break-inner *ns* (get-locals))
     ~form))


(defn break-reader [form]
  `(break ~form))

(let [a 1 b 2]
  (break
   (+ a b)))


(let [a 1 b 2]
  #break
  (+ a b))


(defmacro debug [form]
  ...)


'(let [a 1 b 2]
   (+ a b))


(let [a (break 1)
      b (break 2)]
  (break
   (+ a b)))


(let [(break a) (break 1)
      (break b) (break 2)]
  (break
   (+ a b)))


'(let [a (break 1)
       b (break 2)]
   (break
    (+ a b)))




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
  ...)

(defn eval+ [ns locals form]
  (doseq [[sym value] locals]
    (intern ns sym value))
  (let [result
        (binding [*ns* ns]
          (eval form))]
    (doseq [[sym value] locals]
      (ns-unmap ns sym))
    result))



(def ^:dynamic ^:private
  *locals* nil)

(defn eval+ [ns locals form]
  (binding [*locals* locals
            *ns* ns]
    (eval `(let ~(reduce
                  (fn [result sym]
                    (conj result sym `(get *locals* '~sym)))
                  []
                  (keys locals))
             ~form))))


[a (get *locals* 'a)
 b (get *locals* 'b)
 ...
 ]


(eval+ *ns* '{a 1 b 2} '(+ a b))
;; 3

;; locals
{'a 1 'b 2}

;; form
'(+ a b)

;; result
(eval '(let [a 1 b 2]
         (+ a b)))

(def a 1)


(eval '(let [a (get ... 'a)
             b (get ... 'b)]
         (+ a b)))


(def ^:dynamic *foo* nil)

(binding [*foo* 3]
  (eval '(* *foo* *foo*)))


(defn make-eval-form [locals form]
  (list 'let (vec (mapcat identity locals)) form))

(make-eval-form {'a 1 'b 2} '(+ a b))

(make-eval-form
 {'file (new java.io.File "test.txt")}
 '(.getAbsolutePath file))

(let [file #object[java.io.File 0x4e293fac "test.txt"]]
  (.getAbsolutePath file))

(make-eval-form
 {'numbers (list 1 2 3)}
 '(count numbers))

(let [numbers (1 2 3)]
  (count numbers))

(eval+ *ns* {'a 1 'b 2} '(+ a b))
;; 3

a

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


{break my.namespace/break}
