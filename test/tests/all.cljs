(ns tests.all
  (:require
    [cljs.test :refer-macros [deftest is testing use-fixtures async]]
    [cljs-web3-next.core :as w3n]
    [district.server.web3 :refer [web3]]
    [district.server.web3-watcher :as web3-watcher]
    [mount.core :as mount]))

(use-fixtures
  :each
  {:after
   (fn []
     (mount/stop))})

(def went-online-count (atom 0))
(def went-offline-count (atom 0))

(defn start-library-mount-modules []
    (-> (mount/with-args {:web3 {:host "ws://localhost" :port 8549}
                          :web3-watcher {:interval 500
                                         :confirmations 2
                                         :on-online #(swap! went-online-count inc)
                                         :on-offline #(swap! went-offline-count inc)}})
      (mount/start)))

(defn connect-testnet []
  (w3n/connect @web3))

(defn disconnect-testnet []
  (w3n/disconnect @web3))

(defn do-after [millis func]
  (js/setTimeout func millis))

; This can be lower locally but on CI it fails with < 1000
(def wait-time-between-testnet-connections 2000)

(deftest test-web3-watcher
  (start-library-mount-modules)
  (connect-testnet)

  (async done
         (do-after wait-time-between-testnet-connections
             (fn []
               (is (= 1 @went-online-count))
               (disconnect-testnet)
               (do-after wait-time-between-testnet-connections
                         (fn []
                           (is (= 1 @went-offline-count))
                           (connect-testnet)
                           (do-after wait-time-between-testnet-connections
                                     (fn []
                                       (is (= 2 @went-online-count))
                                       (done)))))))))
