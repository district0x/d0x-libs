(ns ipfs-tryout
  (:require [cljs-ipfs-api.core :as ipfs-core]
            [cljs-ipfs-api.utils :as ipfs-utils]
            ["node:buffer" :refer [Blob Buffer]]
            ; ["buffer" :refer [Blob]]
            ))

(defn main [& args]
  (ipfs-core/init-ipfs {:host "http://host-machine:5001" :endpoint "/api/v0"})
  (ipfs-utils/api-call @ipfs-core/*ipfs-instance*
                        "add"
                        [(.from Buffer "Hello World from Node.js")]
                        ; [(new Buffer ["Hello World"])]
                        (merge {:callback (fn [err data]
                                            (println "IPFS add callback: " {:err err :data data}))}))
  (println "ipfs-tryout"))
