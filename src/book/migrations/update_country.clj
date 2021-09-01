(ns book.migrations.update-country
  (:require
   [clojure.java.jdbc :as jdbc]
   [clj-http.client :as client]))


(defn get-ip-info [ip]
  (:body
   (client/post "https://iplocation.com"
                {:form-params {:ip ip}
                 :as :json})))


(defn migrate-up [{:keys [db]}]
  (let [rows (jdbc/query db "select id, ip from requests where country_code is null")]
    (doseq [{:keys [id ip]} rows]
      (let [{:keys [country_code]} (get-ip-info ip)]
        (jdbc/update! db :requests
                      {:country_code country_code}
                      ["id = ?" id])))))


(defn migrate-down [{:keys [db]}]
  (jdbc/update! db :requests
                {:country_code nil}
                ["true"]))
