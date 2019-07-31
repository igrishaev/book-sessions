(ns book.systems)


(require 'langohr.core)

langohr.core/*default-config*


(require '[langohr.core      :as rmq]
         '[langohr.channel   :as lch]
         '[langohr.queue     :as lq]
         '[langohr.consumers :as lc]
         '[langohr.basic     :as lb])


(defn message-handler
  [ch {:keys [content-type delivery-tag type] :as meta} ^bytes payload]
  (println (format "[consumer] Received a message: %s, delivery tag: %d, content type: %s, type: %s"
                   (String. payload "UTF-8") delivery-tag content-type type)))


(def ^{:const true}
  default-exchange-name "")


(defn -main
  [& args]
  (let [conn  (rmq/connect)
        ch    (lch/open conn)
        qname "langohr.examples.hello-world"]
    (println (format "[main] Connected. Channel id: %d" (.getChannelNumber ch)))
    (lq/declare ch qname {:exclusive false :auto-delete true})
    (lc/subscribe ch qname message-handler {:auto-ack true})
    (lb/publish ch default-exchange-name qname "Hello!" {:content-type "text/plain" :type "greetings.hi"})
    (Thread/sleep 2000)
    (println "[main] Disconnecting...")
    (rmq/close ch)
    (rmq/close conn)))


;; mount.core.DerefableState
;; mount.core.NotStartedState
;; #object[org.eclipse.jetty.server.Server 0xcafbe9 "Server@cafbe9{STARTED}[9.4.12.v20180830]"]
