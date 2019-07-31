(ns book.systems.mount.core
  (:require
   [mount.core :as mount]
   book.systems.mount.server
   book.systems.mount.worker))

(defn start []
  (mount/start))
