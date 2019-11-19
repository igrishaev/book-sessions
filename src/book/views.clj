(ns book.views
  (:require [clj-http.client :as client]
            [compojure.core :refer [GET defroutes]]

            [ring.middleware.json :refer [wrap-json-response
                                          wrap-json-body]]
            [ring.middleware.params :refer [wrap-params]]
            [ring.middleware.keyword-params :refer [wrap-keyword-params]]

            ))





(def config {})


(defn get-sites-by-location
  [{:keys [lat lon]}]
  (-> {:method :get
       ;; :url "http://127.0.0.1:8808/search/v1/"
       :url (str (:maps-base-url config ) "/search/v1/")
       ;; :url "https://maps.yandex.ru/search/v1/"
       :as :json
       :query-params {:apikey "....."
                      :lat lat :lon lon
                      :type "cafe,restaurant"}}
      client/request
      :body))


(defn get-events-by-location
  [{:keys [lat lon]}]
  (-> {:method :get
       :url "http://127.0.0.1:8808/search/v1/"
       ;; :url "https://events.yandex.ru/search/v1/"
       :as :json
       :query-params {:apikey "....."
                      :date "2019-11-01"
                      :lat lat :lon lon
                      :type "movie,theatre"}}
      client/request
      :body))


(defn view-main-page [request]
  (let [location (-> request :params (select-keys [:lat :lon]))
        sites (get-sites-by-location location)
        events (get-events-by-location location)]
    {:status 200
     :body {:sites sites :events events}}))



;;;;;;;;;;;;;;;;;


(defn page-index
  [request]
  {:status 200
   :headers {:content-type "text/plain"}
   :body "Learning Web for Clojure"})

(defn page-hello
  [request]
  {:status 200
   :headers {:content-type "text/plain"}
   :body "Hi there and keep trying!"})

(defn page-404
  [request]
  {:status 404
   :headers {:content-type "text/plain"}
   :body "No such a page."})


(defroutes app-naked
  (GET "/"      request (page-index request))
  (GET "/hello" request (page-hello request))
  page-404)


(def app
  (-> app-naked
      wrap-keyword-params
      wrap-params
      wrap-json-body
      wrap-json-response))
