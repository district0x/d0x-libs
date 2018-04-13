(ns district.ui.web3-sync-now.utils
    (:require [cljs-web3.eth :as web3-eth]
              [district.web3-utils :as web3-utils]))

(defn get-last-block-timestampt [web3]
  (->> (web3-eth/block-number web3)
       (web3-eth/get-block web3)
       :timestamp
       web3-utils/web3-time->date-time))
