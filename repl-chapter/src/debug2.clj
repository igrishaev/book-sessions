(ns debug2)


(def ^:dynamic ^:private
  *locals* nil)

(defn eval+ [ns locals form]
  (binding [*locals* locals
            *ns* ns]
    (eval `(let ~(reduce
                  (fn [result sym]
                    (conj result sym `(get *locals* '~sym)))
                  []
                  (keys locals))
             ~form))))


(defn break-inner [ns locals]
  (loop []
    (let [input (read-line)
          form (read-string input)]
      (when-not (= form '!exit)
        (let [result
              (case form
                !locals locals
                !help "Help message..."
                (eval+ ns locals form))]
          (println result)
          (recur))))))


(defmacro get-locals []
  (into {} (for [sym (keys &env)]
             [(list 'quote sym) sym])))


(defmacro break [form]
  `(do
     (break-inner *ns* (get-locals))
     ~form))


(defn break-reader [form]
  `(break ~form))




#_
(let [a 1
      b 2]
  #my/break
  (+ a b))


;; (defn example []
;;   (let [a 1
;;         b 2]
;;     #break
;;     (+ a b)))


;; (example)


;; {dbg cider.nrepl.middleware.debug/debug-reader
;;  break cider.nrepl.middleware.debug/breakpoint-reader
;;  light cider.nrepl.middleware.enlighten/light-reader}


;; (def form (read-string "(let [a 1 b 2] #break (+ a b))"))


;; (ins/instrument-tagged-code -f)


(defn get-joke [lang]
  (let [request
        {:url "https://v2.jokeapi.dev/joke/Programming"
         :method :get
         :query-params {:contains lang} => "C#" ;; <
         :as :json}


        response
        (client/request request) => {:url "https:..." :method :get ...} ;; <
