(ns book.web
  (:require
   [ring.middleware.params :refer [wrap-params]]
   [ring.middleware.keyword-params :refer [wrap-keyword-params]]
   [ring.adapter.jetty :refer [run-jetty]]))


(def wrap-params+ (comp wrap-params wrap-keyword-params))


(defn app-naked [request]
  {:status 200
   :body (pr-str (:params request))})


(def app (-> app-naked
             wrap-params+))


#_
(def server (run-jetty app {:port 8080 :join? false}))
