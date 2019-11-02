(ns book.views
  (:require [clj-http.client :as client]))


(defn get-sites-by-location
  [{:keys [lat lon]}]
  (-> {:method :get
       :url "https://maps.yandex.ru/search/v1/"
       :as :json
       :query-params {:apikey "....."
                      :lat lat :lon lon
                      :type "cafe,restaurant"}}
      client/request
      :body))


(defn get-events-by-location
  [{:keys [lat lon]}]
  (-> {:method :get
       :url "https://events.yandex.ru/search/v1/"
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
