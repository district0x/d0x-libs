(ns district.validation
  (:require
    [cljs-web3.core :as web3]
    [clojure.string :as string]
    [district.web3-utils :as web3-utils]
    [goog.format.EmailAddress :as email-address]))


(defn js-date? [x]
  (instance? js/Date x))

(defn cljs-time? [x]
  (instance? goog.date.DateTime x))


(defn length?
  ([x max-length]
   (length? x 0 max-length))
  ([x min-length max-length]
   (and (string? x)
        (<= (or min-length 0) (count (string/trim x)) max-length))))


(defn email? [x & [{:keys [:allow-empty?]}]]
  (let [valid? (email-address/isValidAddress x)]
    (if allow-empty?
      (or (empty? x) valid?)
      valid?)))


(def web3-address? web3/address?)


(defn sha3? [x]
  (and (string? x)
       (= (count x) 66)
       (string/starts-with? x "0x")))


(def not-neg? (complement neg?))
(def not-nil? (complement nil?))

(def http-url-pattern #"(?i)^(?:(?:https?)://)(?:\S+(?::\S*)?@)?(?:(?!(?:10|127)(?:\.\d{1,3}){3})(?!(?:169\.254|192\.168)(?:\.\d{1,3}){2})(?!172\.(?:1[6-9]|2\d|3[0-1])(?:\.\d{1,3}){2})(?:[1-9]\d?|1\d\d|2[01]\d|22[0-3])(?:\.(?:1?\d{1,2}|2[0-4]\d|25[0-5])){2}(?:\.(?:[1-9]\d?|1\d\d|2[0-4]\d|25[0-4]))|(?:(?:[a-z\u00a1-\uffff0-9]-*)*[a-z\u00a1-\uffff0-9]+)(?:\.(?:[a-z\u00a1-\uffff0-9]-*)*[a-z\u00a1-\uffff0-9]+)*(?:\.(?:[a-z\u00a1-\uffff]{2,}))\.?)(?::\d{2,5})?(?:[/?#]\S*)?$")


(defn http-url? [x & [{:keys [:allow-empty?]}]]
  (if (and allow-empty? (empty? x))
    true
    (when (string? x)
      (boolean (re-matches http-url-pattern x)))))


(defn eth-value? [x & [{:keys [:allow-empty?]}]]
  (or
    (and allow-empty? (empty? x))
    (string? (web3-utils/eth->wei (str x)))))


(defn not-neg-eth-value? [x & [{:keys [:allow-empty?]}]]
  (or
    (and allow-empty? (empty? x))
    (let [x (web3-utils/eth->wei (str x))]
      (and (string? x)
           (not= "-" (first x))))))


(defn pos-eth-value? [x & [{:keys [:allow-empty?]}]]
  (or
    (and allow-empty? (empty? x))
    (let [x (web3-utils/eth->wei (str x))]
      (and (string? x)
           (not= "0" x)
           (not= "-" (first x))))))




