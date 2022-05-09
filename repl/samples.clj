

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


// Compiled by ClojureScript 1.10.758 {}
goog.provide('foo');
goog.require('cljs.core');
foo.add = (function foo$add(a,b){
return (a + b);
});

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