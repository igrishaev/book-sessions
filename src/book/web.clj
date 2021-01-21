(ns book.web
  (:require
   [ring.middleware.content-type
    :refer [wrap-content-type]]
   [clojure.java.io :as io]
   [ring.middleware.params :refer [wrap-params]]
   [ring.middleware.keyword-params :refer [wrap-keyword-params]]
   [ring.middleware.json :refer [wrap-json-response
                                 wrap-json-body]]
   [ring.middleware.file :refer [wrap-file]]
   [ring.middleware.resource :refer [wrap-resource]]
   [clojure.walk :refer [keywordize-keys stringify-keys]]
   [ring.middleware.session :refer [wrap-session]]
   [clj-http.client :as client]
   [bidi.bidi :as bidi]
   [clojure.java.jdbc :as jdbc]
   [ring.adapter.jetty :refer [run-jetty]]
   [ring.middleware.cookies :refer [wrap-cookies]]
   [compojure.core :refer [GET defroutes wrap-routes context]]))


(def routes
  ["/" {""      :page-index
        "hello" :page-hello
        true    :not-found}])


(defn wrap-handler [handler]
  (fn [request]
    (let [{:keys [uri]} request
          request* (bidi/match-route* routes uri request)]
      (handler request*))))


(defmulti multi-handler :handler)

(defmethod multi-handler :page-index
  [request]
  {:status 200
   :headers {"content-type" "text/plain"}
   :body "Learning Clojure"})

(defmethod multi-handler :page-hello
  [request]
  {:status 200
   :headers {"content-type" "text/plain"}
   :body "Hello!"})

(defmethod multi-handler :not-found
  [request]
  {:status 404
   :headers {"content-type" "text/plain"}
   :body "Page not found."})


(def app (wrap-handler multi-handler))

#_
(app {:request-method :get
      :uri "/hello?foo=42"
      :handler :page-hello})


(def routes
  [["/content/order/" :id] {"/view" {:get  :page-view}
                            "/edit" {:get  :page-form
                                     :post :page-save}}])


(def routes
  ["/" {["content/order/" :id]
        {"/view" {:get  :page-view}
         "/edit" {:get  :page-form
                  :post :page-save}}}])

#_
(bidi/match-route* routes "/content/order/1/view" {:request-method :get})

#_
(bidi/match-route* routes "/content/order/1/edit" {:request-method :get})

#_
(bidi/match-route* routes "/content/order/1/edit" {:request-method :post})


(defn page-404 [request]
  {:status 404
   :headers {"content-type" "text/plain"}
   :body "Page not found."})


(def *db*)
(def get-order-by-id)
(def render-order-page)

(defmethod multi-handler :page-view
  [request]
  (if-let [order (some-> request :route-params
                         :id get-order-by-id)]
    {:status 200
     :headers {"content-type" "text/html"}
     :body (render-order-page {:order order})}
    page-404))


(defn page-index [request]
  {:status 200
   :headers {"content-type" "text/plain"}
   :body "Learning Clojure"})

(defn page-hello [request]
  {:status 200
   :headers {"content-type" "text/plain"}
   :body "Hi there! Keep trying!"})

(defn page-404 [request]
  {:status 404
   :headers {"content-type" "text/plain"}
   :body "Page not found."})


(defroutes app
  (GET "/"      request (page-index request))
  (GET "/hello" request (page-hello request))
  page-404)

(defn app [request]
  (let [{:keys [uri]} request]
    (case uri
      "/"      (page-index request)
      "/hello" (page-hello request)
      (page-404 request))))

#_
(defn page-seen [request]
  (let [{:keys [cookies]} request
        seen-path ["seen" :value]
        seen? (get-in cookies seen-path)
        cookies* (assoc-in cookies seen-path true)]
    {:status 200
     :cookies cookies*
     :body (if seen?
             "Already seen."
             "The first time you see it!")}))

(defn page-seen [request]
  (let [{:keys [cookies]} request
        seen-path ["seen" :value]
        seen? (get-in cookies seen-path)
        cookies* (assoc cookies "seen"
                        {:value true :http-only true})]
    {:status 200
     :cookies cookies*
     :body (if seen?
             "Already seen."
             "The first time you see it!")}))


#_
(let [cookies* (assoc cookies "seen"
                      {:value true :http-only true})]
  ...)


(def app (-> page-seen
             wrap-cookies))


;; Set-Cookie: seen=true

;; Cookie: ring-session=ec7fe2a4-3660-4c69-bc3f-dcaf227677c4; seen=true

;; document.cookie
;; "ring-session=ec7fe2a4-3660-4c69-bc3f-dcaf227677c4; seen=true"

;; document.cookie
;; "ring-session=ec7fe2a4-3660-4c69-bc3f-dcaf227677c4"


(defn app [request]
  (let [{:keys [uri request-method]} request]
    {:status 200
     :headers {"Content-Type" "text/plain"}
     :body (format "You requested %s %s"
                   (-> request-method name .toUpperCase)
                   uri)}))



(defn app [request]
  (let [{:keys [uri request-method]} request]
    {:status 200
     :headers {"Content-Type" "text/plain"}
     :body (with-out-str
             (clojure.pprint/pprint request))}))

#_
(def _s (run-jetty app {:port 8088 :join? false}))
#_
(.stop _s)


(defn handler [request]
  (let [content (-> request :body slurp)]
    #_(process-content content)
    {:status 200
     :body (format "The content was %s" content)}))


(def get-user-by-id)


(defn page-user [request]
  (let [user-id (-> request :params :id)
        user (get-user-by-id user-id)
        {:keys [fname lname]} user]
    {:status 200
     :body (format "User %s is %s %s"
                   user-id fname lname)}))


(def response-404)

(defmethod multi-handler :page-view
  [request]
  (if-let [order (some-> request
                         :route-params
                         :id
                         get-order-by-id)]
    {:status 200
     :headers {"content-type" "text/html"}
     :body (render-order-page {:order order})}
    response-404))


(defn wrap-headers-kw [handler]
  (fn [request]
    (-> request
        (update :headers keywordize-keys)
        handler
        (update :headers stringify-keys))))


(defn app* [request]
  (let [{:keys [headers]} request
        {:keys [host]} headers]
    {:status 200
     :headers {:content-type "text/html"}
     :body (format "<h1>Host header: %s</h1>" host)}))


(def app (wrap-headers-kw app*))


(defn account-cart [_]
  {:status 200 :body "cart"})

(defn account-orders [_]
  {:status 200 :body "orders"})

(defn account-profile [_]
  {:status 200 :body "profile"})


(defn wrap-auth-user-only [handler]
  (fn [request]
    (if (:user request)
      (handler request)
      {:status 403
       :headers {"content-type" "text/plain"}
       :body "Please sign in to access this page."})))

(defroutes app-account
  (GET "/cart"    _ "cart")
  (GET "/orders"  _ "orders")
  (GET "/profile" _ "profile"))

(defroutes app
  (GET "/"     _ "index")
  (GET "/help" _ "help")
  (context "/account" []
    (wrap-routes app-account wrap-auth-user-only)))


(defn handler [request]
  {:status 200 :body "Response from Clojure"})


(def app
  (wrap-resource handler "public"))


#_
(require '[clj-http.client :as client])

#_
(-> response
    (select-keys [:status :body :headers])
    (update :headers select-keys ["Content-Type"]))

#_
(defn app-proxy [request]
  (let [response (client/get "https://ya.ru" {:stream? true})
        {:keys [status headers body]} response
        headers* (select-keys headers ["Content-Type"])]
    {:status 200
     :headers headers*
     :body body}))

(defn app-proxy [request]
  (-> "https://ya.ru"
      (client/get {:stream? true})
      (select-keys [:status :body :headers])
      (update :headers select-keys ["Content-Type"])))


(defn page-terminals [request]
  {:status 200
   :headers {"content-type" "application/json"}
   :body (slurp "terminals.json")})

(defn page-terminals [request]
  {:status 200
   ;; :body (io/file "terminals.json")
   :body (new java.io.File "terminals.json")})


(require '[clojure.java.io :as io])

(def app (wrap-content-type page-terminals {:mime-types {"json" "application/json"}}))

(def app (wrap-content-type page-terminals))

(def page-departments)

(defroutes app
  (GET "/terminals.json"   request (page-terminals request))
  (GET "/departments.json" request (page-departments request)))

#_
(def app (-> app-naked
             (wrap-file "/var/www/public")))


#_
(def app (-> app-naked
             (wrap-resource "public")))


#_
(require '[ring.middleware.content-type :refer [wrap-content-type]])

(def app
  (-> app-naked
      (wrap-content-type
       {:mime-types {"json" "application/json"}})))


(def app (wrap-content-type page-terminals {:mime-types {"json" "application/json"}}))



(defn app [request]
  {:status 200
   :headers {"content-type" "image/png"}
   :body (slurp "/path/to/image.png")})



#_
(defn app [req]
  (clojure.pprint/pprint req)
  {:status 200
   :body {:foo 42}})


#_
(def app*
  (-> app
      wrap-keyword-params
      wrap-params
      wrap-json-response
      wrap-json-body))

#_
(def server (run-jetty app* {:port 8088 :join? false}))
