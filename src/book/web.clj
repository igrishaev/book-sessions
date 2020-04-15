(ns book.web
  (:require
   [bidi.bidi :as bidi]
   [clojure.java.jdbc :as jdbc]
   [ring.adapter.jetty :refer [run-jetty]]
   [ring.middleware.cookies :refer [wrap-cookies]]
   [compojure.core :refer [GET defroutes]]))


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


#_
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


(defn page-user [request]
  (let [user-id (-> request :params :id)
        user (get-user-by-id user-id)
        {:keys [fname lname]} user]
    {:status 200
     :body (format "User %s is %s %s"
                   user-id fname lname)}))


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
