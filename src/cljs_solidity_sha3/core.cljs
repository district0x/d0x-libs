(ns cljs-solidity-sha3.core
  (:require
    [cljs-web3-next.utils :as web3-utils]
    [clojure.string :as string]
    [cljs.pprint :refer [cl-format]]))

(defn remove-0x [s]
  (string/replace s #"0x" ""))


(defn left-pad
  ([s len] (left-pad s len " "))
  ([s len ch]
   (cl-format nil (str "~" len ",'" ch "d") (str s))))


(defn solidity-sha3 [web3 & args]
    (web3-utils/solidity-sha3 web3 (str "0x" (string/join ""
                          (map (fn [arg]
                                 (cond
                                   (and (string? arg) (string/starts-with? arg "0x"))
                                   (remove-0x arg)

                                   (string? arg)
                                   (remove-0x (web3-utils/to-hex web3 arg))

                                   (number? arg)
                                   (left-pad (remove-0x (web3-utils/to-hex web3 arg)) 64 "0")))
                               args)))))
