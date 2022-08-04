(ns nrepl-prod.core
  (:gen-class)
  (:require
   [cider.nrepl]
   [clojure.java.io :as io]
   [clojure.edn :as edn]
   [com.stuartsierra.component :as component]
   [nrepl.server :refer [start-server stop-server]]))

;; (defonce server nil)

;; (defn nrepl-start! []
;;   (alter-var-root
;;    #'server
;;    (constantly
;;     (start-server :bind "0.0.0.0" :port 9911
;;                   :handler cider.nrepl/cider-nrepl-handler))))

;; (defn nrepl-stop! []
;;   (alter-var-root #'server stop-server))


(defrecord nREPLServer
    [options
     server]

  component/Lifecycle

  (start [this]
    (let [options
          (update options :handler #(some-> % resolve deref))

          arg-list
          (mapcat identity options)

          server
          (apply start-server arg-list)]

      (assoc this :server server)))

  (stop [this]
    (when server
      (stop-server server))
    (assoc this :server nil)))


(defn make-nrepl-server [options]
  (map->nREPLServer {:options options}))

(def system-config
  (-> "config.edn"
      io/resource
      slurp
      edn/read-string))

(def system-init
  (component/system-map
   :nrepl (make-nrepl-server (:nrepl system-config))))

(defn -main
  [& _]
  (let [system-started
        (component/start system-init)]
    (println "The nREPL server has been started")))





;; (.println System/out "hello")


#_
(defn add [a b]
  (+ a b))

#_
(spit "out.txt" "some text")

#_
(require '[clojure.java.shell :refer [sh]])

#_
(:out (sh "uname" "-a"))
