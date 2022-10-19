(ns tests.all
  (:require [cljs-web3-next.core :as web3-core]
            [cljs-web3-next.eth :as web3-eth]
            [cljs.core.async :refer [<!]]
            [cljs.test :refer-macros [deftest is testing use-fixtures async]]
            [district.server.web3 :refer [web3]]
            [district.shared.async-helpers :as async-helpers]
            [mount.core :as mount])
  (:require-macros [cljs.core.async.macros :refer [go]]))

(async-helpers/extend-promises-as-channels!)

(use-fixtures :each
  {:before (fn []
             (-> (mount/with-args {:web3 {:url "ws://127.0.0.1:8549"}})
                 (mount/start)))
   :after (fn []
            (mount/stop))})

(deftest test-web3
  (async done
         (go
           (let [connected? (<! (web3-eth/is-listening? @web3))
                 accounts (<! (web3-eth/accounts @web3))]
             (is (true? connected?))
             (is (not (nil? accounts)))
             (done)))))
