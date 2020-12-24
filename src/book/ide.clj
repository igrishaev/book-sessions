(ns book.ide
  (:require
   [clojure.spec.alpha :as s]))


(def data
  [
   {:command "open"}
   {:command "click"}
   {:command "if"}
   {:command "foo1"}
   {:command "foo2"}
   {:command "else"}
   {:command "bar"}

   {:command "set" :var "$x" :val 3}


   {:command "if" :condition "$x > 5"}
   {:command "for"}
   {:command "print"}
   {:command "end"}
   {:command "end"}

   {:command "end"}
   ]
  )


(def data2
  [
   {:command "print" :text "starting"}
   {:command "if" :condition "TRUE"}
   {:command "print" :text "it was true"}
   {:command "else"}
   {:command "print" :text "it was false"}
   {:command "end"}
   {:command "print" :text "finishing"}
   ]
  )


(s/def ::flow
  (s/+
   (s/alt :if ::flow-if
          :for ::flow-for
          :cmd ::command)))


(def flow-tags #{"if" "else" "end" "for"})


(defn tag= [tag]
  (fn [obj]
    (= (get obj :command) tag)))


(s/def ::flow-if
  (s/cat :this (tag= "if")
         :flow ::flow
         :else (s/? (s/cat :this (tag= "else")
                           :flow ::flow))
         :end (tag= "end")))


(s/def ::flow-for
  (s/cat :this (tag= "for")
         :flow ::flow
         :end (tag= "end")))


(s/def ::command
  (fn [obj]
    (when-let [command (get obj :command)]
      (not (contains? flow-tags command)))))


(defn parse-flow [data]
  (s/conform ::flow data))


;; -------

(defmulti do-flow
  (fn [[tag command]]
    tag))


(defmulti do-cmd
  (fn [command]
    (-> command :command keyword)))


;; ------

(defn run-flow [flow]
  (doseq [flow flow]
    (do-flow flow)))


(defn run-data [data]
  (let [result (parse-flow data)]
    (if (s/invalid? result)
      (println "Wrong data!")
      (run-flow result))))


;; ---------

(defmethod do-flow :cmd
  [[_ command]]
  (do-cmd command))


(defmethod do-cmd :print
  [{:keys [text]}]
  (println text))


#_
(defmethod do-flow :if
  [[_ {:keys [this flow else]}]]
  (if 1
    (run-flow flow)
    (when else
      (let [{:keys [this flow]} else]
        (run-flow)))))


(defmethod do-flow :if
  [[_ flow-if]]
  (let [{:keys [this flow else]} flow-if
        {:keys [condition]} this]
    (if (= condition "TRUE")
      (run-flow flow)
      (when else
        (let [{:keys [this flow]} else]
          (run-flow flow))))))


#_
(s/conform ::commands data)
