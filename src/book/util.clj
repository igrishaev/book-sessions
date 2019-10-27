(ns book.util)


(defn ->fahr [cel]
  (+ (* cel 1.8) 32))

#_
(defn ->fahr [cel]
  (when cel
    (+ (* cel 1.8) 32)))

(defn ->fahr [cel]
  (if cel
    (+ (* cel 1.8) 32)
    (throw (new IllegalArgumentException
            "Fahrenheit temperature should be a real number"))))
