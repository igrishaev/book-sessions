

(require '[cljs.repl :as repl])
(require '[cljs.repl.browser :as browser])
(require '[cljs.repl.node :as node])

(def env (browser/repl-env))
(def env (node/repl-env))

(repl/repl env)


;; clj -R:nrepl -m nrepl.cmdline --middleware "[cider.piggieback/wrap-cljs-repl]"


fs.readdirSync('./dirpath', {withFileTypes: true})
.filter(item => !item.isDirectory())
.map(item => item.name)

const fs = require("fs")

(def js (js/require "fs"))

(let [files (fs.readdirSync "/Users/ivan" #js {:withFileTypes true})]
  (doseq [file files]
    (println file.name)))


clj -M -m cljs.main --repl-env node
clj -M -m cljs.main --repl-env browser

;; Starting server via /usr/local/bin/clojure -Sdeps '{:deps {nrepl/nrepl {:mvn/version "0.9.0"} cider/cider-nrepl {:mvn/version "0.28.3"} cider/piggieback {:mvn/version "0.5.2"}} :aliases {:cider/nrepl {:main-opts ["-m" "nrepl.cmdline" "--middleware" "[cider.nrepl/cider-middleware,cider.piggieback/wrap-cljs-repl]"]}}}' -M:cider/nrepl


(defn add [a b]
  (+ a b))

// Compiled by ClojureScript 1.10.758 {}
goog.provide('foo');
goog.require('cljs.core');
foo.add = (function foo$add(a,b){
return (a + b);
});



(set! js/window.document.title "New Title")


http://localhost:9000/repl?xpc={"cn":"CZLSiVWGAi","ppu":"http://localhost:9000/robots.txt","lpu":"http://localhost:9000/robots.txt"}


{"repl":"main","form":"(function (){try{return cljs.core.pr_str.call(null,(function (){var ret__6698__auto__ = ((1) + (2));\n(cljs.core._STAR_3 = cljs.core._STAR_2);\n\n(cljs.core._STAR_2 = cljs.core._STAR_1);\n\n(cljs.core._STAR_1 = ret__6698__auto__);\n\nreturn ret__6698__auto__;\n})());\n}catch (e617){var e__6699__auto__ = e617;\n(cljs.core._STAR_e = e__6699__auto__);\n\nthrow e__6699__auto__;\n}})()"}

{"repl":"main","form":"(function () {
  try {
    return cljs.core.pr_str.call(null, (function () {
      var ret__6698__auto__ = ((1) + (2));
      (cljs.core._STAR_3 = cljs.core._STAR_2);
      (cljs.core._STAR_2 = cljs.core._STAR_1);
      (cljs.core._STAR_1 = ret__6698__auto__);
      return ret__6698__auto__;
    })());
  } catch (e617) {
    var e__6699__auto__ = e617;
    (cljs.core._STAR_e = e__6699__auto__);
    throw e__6699__auto__;
  }
})()"}

(js/window.localStorage.setItem "key-1" "val-1")

(js/window.localStorage.getItem "key-1")

localStorage.setItem('myCat', 'Tom');

(defn environment []
  (persistent!
   (reduce
    (fn [result var-name]
      (assoc! result
              (keyword var-name)
              (aget js/process.env var-name)))
    (transient {})
    (js-keys js/process.env))))

//# sourceMappingURL=foo.js.map


{:aliases
 {:rebl
  {:extra-deps {com.cognitect/rebl          {:mvn/version "0.9.245"}
                org.openjfx/javafx-fxml     {:mvn/version "15-ea+6"}
                org.openjfx/javafx-controls {:mvn/version "15-ea+6"}
                org.openjfx/javafx-swing    {:mvn/version "15-ea+6"}
                org.openjfx/javafx-base     {:mvn/version "15-ea+6"}
                org.openjfx/javafx-web      {:mvn/version "15-ea+6"}}
   :main-opts ["-m" "cognitect.rebl"]}}}



(-->
  id        "27"
  op        "eval"
  session   "444ea459-4165-4f82-afbc-b9cfbae4d2c5"
  code      "(+ 1 2)"
  column    6
  line      28
  ns        "foo"
)
(<--
  id         "27"
  session    "444ea459-4165-4f82-afbc-b9cfbae4d2c5"
  time-stamp "2022-06-18 17:15:30.451402000"
  value      "3"
)


(use 'clojure.repl)

(apropos "update")
(clojure.core/update clojure.core/update-in clojure.core/update-keys clojure.core/update-proxy clojure.core/update-vals)

(dir clojure.string)
blank?
capitalize
ends-with?
escape
includes?
index-of
join
last-index-of
lower-case
re-quote-replacement
replace
replace-first
reverse
split
split-lines
starts-with?
trim
trim-newline
triml
trimr
upper-case


user=> (doc update)
-------------------------
clojure.core/update
([m k f] [m k f x] [m k f x y] [m k f x y z] [m k f x y z & more])
  'Updates' a value in an associative structure, where k is a
  key and f is a function that will take the old value
  and any supplied args and return the new value, and returns a new
structure.  If the key does not exist, nil is passed as the old value.


(pst)
ArithmeticException Divide by zero
	clojure.lang.Numbers.divide (Numbers.java:190)
	clojure.lang.Numbers.divide (Numbers.java:3911)
	user/eval154 (NO_SOURCE_FILE:1)
	user/eval154 (NO_SOURCE_FILE:1)
	clojure.lang.Compiler.eval (Compiler.java:7194)
	clojure.lang.Compiler.eval (Compiler.java:7149)
	clojure.core/eval (core.clj:3215)
	clojure.core/eval (core.clj:3211)
	clojure.main/repl/read-eval-print--9206/fn--9209 (main.clj:437)
	clojure.main/repl/read-eval-print--9206 (main.clj:437)
	clojure.main/repl/fn--9215 (main.clj:458)
    clojure.main/repl (main.clj:458)


(source update)

(source update)
(defn update
  "'Updates' a value in an associative structure, where k is a
  key and f is a function that will take the old value
  and any supplied args and return the new value, and returns a new
  structure.  If the key does not exist, nil is passed as the old value."
  {:added "1.7"
   :static true}
  ([m k f]
   (assoc m k (f (get m k))))
  ([m k f x]
   (assoc m k (f (get m k) x)))
  ([m k f x y]
   (assoc m k (f (get m k) x y)))
  ([m k f x y z]
   (assoc m k (f (get m k) x y z)))
  ([m k f x y z & more]
   (assoc m k (apply f (get m k) x y z more))))


(source parse-uuid)
(defn parse-uuid
  {:doc "Parse a string representing a UUID and return a java.util.UUID instance,
  or nil if parse fails.

  Grammar: https://docs.oracle.com/javase/8/docs/api/java/util/UUID.html#toString--"
   :added "1.11"}
  ^java.util.UUID [^String s]
  (try
    (java.util.UUID/fromString s)
    (catch IllegalArgumentException _ nil)))
nil
