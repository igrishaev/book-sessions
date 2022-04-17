

(require '[cljs.repl :as repl])
(require '[cljs.repl.browser :as browser])
(require '[cljs.repl.node :as node])

(def env (browser/repl-env))
(def env (node/repl-env))

(repl/repl env)


;; clj -R:nrepl -m nrepl.cmdline --middleware "[cider.piggieback/wrap-cljs-repl]"
