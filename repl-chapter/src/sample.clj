(ns sample
  (:require
   [clj-http.client :as client]
   [clojure.test :refer [deftest is]]))


(deftest test-orwell
  (is (= 5 (* 2 2))))


(defn multiply [a b]
  (* a b))


(defn ->fahr [cel]
  (+ (* cel 1.8) 32))


(defn make-db-spec [host port dbname user password])

(require '[clojure.string :as str])


(defn remap-props [props]
  (reduce-kv
   (fn [result k v]
     (let [path
           (mapv keyword (str/split k #"\."))]
       (assoc-in result path v)))
   {}
   props))

(defn remap-props [props]
  (reduce-kv
   (fn [result k v]
     (println ">>> " k v) ;; this
     (let [path
           (mapv keyword (str/split k #"\."))]
       (assoc-in result path v)))
   {}
   props))


(remap-props {"db.host" "127.0.0.1"
              "db.port" 5432
              "db.settings.ssl" false})

{:db
 {:host "127.0.0.1"
  :port 5432
  :settings {:ssl false}}}


(remap-props {"db.host" "127.0.0.1"
              "db.settings.ssl" false
              :db/port 5432})


(require '[clojure.pprint :as pprint])

(fn [result k v]
  (pprint/pprint {:key k :value v})
  ...)



#_
(clojure.test/test-var #'test-orwell)



  Show: Project-Only All
  Hide: Clojure Java REPL Tooling Duplicates  (14 frames hidden)

1. Unhandled java.lang.ClassCastException
   class clojure.lang.Keyword cannot be cast to class java.lang.CharSequence
   (clojure.lang.Keyword is in unnamed module of loader 'app';
   java.lang.CharSequence is in module java.base of loader 'bootstrap')

                string.clj:  219  clojure.string/split
                string.clj:  219  clojure.string/split
                      REPL:   28  sample/remap-props/fn
   PersistentArrayMap.java:  377  clojure.lang.PersistentArrayMap/kvreduce
                  core.clj: 6845  clojure.core/fn
                  core.clj: 6830  clojure.core/fn
             protocols.clj:  175  clojure.core.protocols/fn/G
                  core.clj: 6856  clojure.core/reduce-kv
                  core.clj: 6847  clojure.core/reduce-kv
                      REPL:   24  sample/remap-props
                      REPL:   23  sample/remap-props
                      REPL:   45  sample/eval7267
                      REPL:   45  sample/eval7267
             Compiler.java: 7176  clojure.lang.Compiler/eval
             Compiler.java: 7131  clojure.lang.Compiler/eval
                  core.clj: 3214  clojure.core/eval
                  core.clj: 3210  clojure.core/eval
    interruptible_eval.clj:   87  nrepl.middleware.interruptible-eval/evaluate/fn/fn
                  AFn.java:  152  clojure.lang.AFn/applyToHelper
                  AFn.java:  144  clojure.lang.AFn/applyTo
                  core.clj:  665  clojure.core/apply
                  core.clj: 1973  clojure.core/with-bindings*
                  core.clj: 1973  clojure.core/with-bindings*
               RestFn.java:  425  clojure.lang.RestFn/invoke
    interruptible_eval.clj:   87  nrepl.middleware.interruptible-eval/evaluate/fn
                  main.clj:  414  clojure.main/repl/read-eval-print/fn
                  main.clj:  414  clojure.main/repl/read-eval-print
                  main.clj:  435  clojure.main/repl/fn
                  main.clj:  435  clojure.main/repl
                  main.clj:  345  clojure.main/repl
               RestFn.java: 1523  clojure.lang.RestFn/invoke
    interruptible_eval.clj:   84  nrepl.middleware.interruptible-eval/evaluate
    interruptible_eval.clj:   56  nrepl.middleware.interruptible-eval/evaluate
    interruptible_eval.clj:  152  nrepl.middleware.interruptible-eval/interruptible-eval/fn/fn
                  AFn.java:   22  clojure.lang.AFn/run
               session.clj:  218  nrepl.middleware.session/session-exec/main-loop/fn
               session.clj:  217  nrepl.middleware.session/session-exec/main-loop
                  AFn.java:   22  clojure.lang.AFn/run


>>>  db.user Ivan
>>>  db.host 127.0.0.1
>>>  :db/port 5432


(require '[clj-http.client :as client])

(defn get-joke [lang]
  (let [request
        {:url "https://v2.jokeapi.dev/joke/Programming"
         :method :get
         :query-params {:contains lang}
         :as :json}

        response
        (client/request request)

        {:keys [body]}
        response

        {:keys [setup delivery]}
        body]
    (format "%s %s" setup delivery)))


(get-joke "C#")



(-> "#break (+ 1 2)"
    read-string
    meta)


(meta (cider.nrepl.middleware.debug/breakpoint-reader '(+ 1 2)))

#:cider.nrepl.middleware.util.instrument
{:breakfunction
 #'cider.nrepl.middleware.debug/breakpoint-with-initial-debug-bindings}


(cider.nrepl.middleware.util.instrument/instrument-tagged-code
 (read-string "#break (+ 1 2)"))


(cider.nrepl.middleware.util.instrument/instrument-tagged-code
 (read-string "#dbg (get-joke \"python\")"))


(require
 '[cider.nrepl.middleware.util.instrument :refer [instrument-tagged-code]])

(instrument-tagged-code
 (read-string "#dbg (defn add [a b] (+ a b))"))


(instrument-tagged-code
 (read-string "#dbg [a b c]"))


{:user
 :injections
 [(require 'clojure.pprint)
  (require 'clojure.inspector)]}


cider.nrepl.middleware.debug


(#'.../breakpoint-with-initial-debug-bindings
 (def
  add
  (fn*
   ([a b]
    (#'.../breakpoint-if-interesting
     (+
      (#'.../breakpoint-if-interesting
       a
       {:coor [3 1]}
       a)
      (#'.../breakpoint-if-interesting
       b
       {:coor [3 2]}
       b))
     {:coor [3]}
     (+ a b)))))
 {:coor []}
 (defn add [a b] (+ a b)))


(instrument-tagged-code
 (read-string "#dbg [a b c]"))

[(#'.../breakpoint-if-interesting a {:coor [0]} a)
 (#'.../breakpoint-if-interesting b {:coor [1]} b)
 (#'.../breakpoint-if-interesting c {:coor [2]} c)]

(instrument-tagged-code
 (read-string "#dbg [1 \"hello\" :foobar]"))

[1 "hello" :foobar]


(instrument-tagged-code
 (read-string "#dbg #{a b c d e}"))


(instrument-tagged-code
 (read-string "#dbg {1 a 2 b}"))

(instrument-tagged-code
 (read-string "#dbg {c a 2 b}"))


(instrument-tagged-code
 (read-string "#dbg {a a b b c c d d e e f f g g h h i i}"))

(instrument-tagged-code
 (read-string "#dbg (loop [x 0] (recur (inc x)))"))


(loop [x 0]
  #break
  (when (< x 10)
    (println x)
    (recur (inc x))))


docker run -it --rm -p 9911:9911 -v `pwd`:/project -w /project clojure lein with-profile +docker repl

:profilesy
{:docker
 {:repl-options {:port 9911
                 :host "0.0.0.0"}
  :plugins [[cider/cider-nrepl "0.28.3"]]}}
