

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
