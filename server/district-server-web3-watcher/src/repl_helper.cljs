(ns repl-helper
  (:require
            [district.server.web3-watcher :as web3-watcher]
            [district.server.web3 :refer [web3]]
            [cljs-web3-next.core :as w3n]
            [mount.core :as mount]))

; To get started in terminal:
; 1. lein repl :connect 30333
; 2. (shadow/repl :node-repl)
; 3. (in-ns 'repl-helper)

(defn start-mount []
  (-> (mount/with-args {:web3 {:host "ws://localhost" :port 8549}
                       :web3-watcher {:interval 100
                                      :confirmations 4
                                      :on-online #(println "ON ONLINE")
                                      :on-offline #(println "ON OFFLINE")}})
     (mount/start-without #'web3-watcher/web3-watcher)))

(defn main [& args]
  (println "As long as this is running, you can connect to REPL server"))

