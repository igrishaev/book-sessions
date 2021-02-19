(component/system-map
 :db   (make-db pool)
 :smtp (make-smtp smtp)
 :server
 (-> jetty
     make-server
     (component/using [:db :smtp])))

(defprotocol API
  :extend-via-metadata true
  (get-user [this id]))


(def api
  ^{`get-user
    (fn [this id]
      {:id id :name (format "User %s" id)})}
  {:any "map"})


(get-user api 5)
{:id 5, :name "User 5"}
