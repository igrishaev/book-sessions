(ns book.systems.mount.core
  (:require
   [mount.core :as mount]
   [book.systems.mount.server-better]
   [book.systems.mount.worker]))


(defn -main [& args]
  (mount/start))
