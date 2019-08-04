(ns book.systems)


;; mount.core.DerefableState
;; mount.core.NotStartedState
;; #object[org.eclipse.jetty.server.Server 0xcafbe9 "Server@cafbe9{STARTED}[9.4.12.v20180830]"]

(-> 'mount.core/meta-state resolve deref deref)

(def _state @@(resolve 'mount.core/meta-state))

(->> _state
     (sort-by (fn [[_ {:keys [order]}]] order))
     (map (fn [[k v]] k)))

(->> _state
     (sort-by #(-> % second :order))
     (map first))

(->> _state
     vals
     (sort-by :order)
     (map #(-> % :var meta :name)))


(mount/start #'book.systems.mount.config/config
             #'book.systems.mount.db/db
             #'book.systems.mount.worker/worker)

(mount/start #'book.systems.mount.db/db
             #'book.systems.mount.worker/worker)


(-> [#'book.systems.mount.server/server]
    mount/except
    mount/start)


;; init the pool
(def cm (clj-http.conn-mgr/make-reusable-conn-manager
         {:timeout 2 :threads 3}))

;; make a request within the pool
(client/get "http://example.org/"
            {:connection-manager cm})

;; finally, shut it down
(clj-http.conn-mgr/shutdown-manager cm)
