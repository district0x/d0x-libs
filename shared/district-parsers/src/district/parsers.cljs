(ns district.parsers
  (:require
    [cljs-web3-next.core :as web3]
    [cljs.spec.alpha :as s]
    [clojure.string :as string]))

(defn parse-float [x & [default]]
  (cond
    (string? x)
    (let [number (js/parseFloat (string/replace x \, \.))]
      (if-not (js/isNaN number)
        number
        default))

    (number? x) x
    :else default))

(defn parse-int [x & [default]]
  (cond
    (string? x)
    (let [x (js/parseInt x)]
      (if-not (js/isNaN x)
        x
        default))

    (number? x) x
    :else default))

(defn parse-keyword [x & [default]]
  (cond
    (string? x)
    (if (string/starts-with? x ":")
      (keyword (subs x 1))
      (keyword x))

    (keyword? x) x
    :else default))

(defn parse-boolean [x & [default]]
  (cond
    (string? x)
    (condp = (string/lower-case x)
      "true" true
      "false" false
      default)

    (boolean? x) x
    :else default))

(defn parse-non-empty-str [x & [default]]
  (cond
    (and (string? x) (seq x)) x
    (number? x) (str x)
    :else default))

(defn parse-web3-address [x & [default]]
  (if (web3/address? x)
    (string/lower-case x)
    default))

(defn parse-seq-fn [parse-fn]
  (fn [coll & [default]]
    (let [coll (if (sequential? coll) coll [coll])]
      (map #(parse-fn % default) coll))))

(def parse-float-seq (parse-seq-fn parse-float))
(def parse-int-seq (parse-seq-fn parse-int))
(def parse-keyword-seq (parse-seq-fn parse-keyword))
(def parse-boolean-seq (parse-seq-fn parse-boolean))
(def parse-web3-address-seq (parse-seq-fn parse-web3-address))
(def parse-non-empty-str-seq (parse-seq-fn parse-non-empty-str))
