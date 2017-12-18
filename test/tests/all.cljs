(ns tests.all
  (:require
    [cljs-web3.core :as web3]
    [cljs-web3.eth :as web3-eth]
    [cljs.test :refer-macros [deftest is testing use-fixtures]]
    [district.server.web3 :refer [web3]]
    [mount.core :as mount]))

(use-fixtures
  :each
  {:after
   (fn []
     (mount/stop))})

(deftest test-web3
  (-> (mount/with-args {:web3 {:port 8794}})
    (mount/start))
  (is (true? (web3/connected? @web3)))
  (is (web3/address? (first (web3-eth/accounts @web3)))))