(ns book.spec)

(require '[clojure.spec.alpha :as s])

(s/def ::string string?)

(s/get-spec ::string)

(s/valid? ::string 1)

(s/valid? ::string "test")

(s/def ::ne-string
  (fn [val]
	(and (string? val)
     	 (not (empty? val)))))

(s/def ::ne-string
  (every-pred string? not-empty))

(s/valid? ::ne-string "test")

(s/valid? ::ne-string "")

(s/def ::ne-string
  (s/and ::string not-empty))

(s/def ::url
  (partial re-matches #"(?i)^http(s?)://.*"))

(s/valid? ::url "test")

(s/valid? ::url "http://test.com")

;; (s/valid? ::url nil)

(s/def ::url
  (s/and
   ::ne-string
   (partial re-matches #"(?i)^http(s?)://.*")))

(s/valid? ::url nil)
