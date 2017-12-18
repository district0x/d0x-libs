(ns tests.all
  (:require
    [cljs.test :refer-macros [deftest is testing use-fixtures]]
    [district.server.web3 :refer [web3]]
    [district.server.web3-watcher]
    [mount.core :as mount]))

(use-fixtures
  :each
  {:after
   (fn []
     (mount/stop))})

(deftest test-web3-watcher
  ;; Cannot write tests until Ganache starts supporting synchronous requests
  ;; https://github.com/trufflesuite/ganache-core/issues/15
  ;; because web3/connected? will otherwise hang
  ;; Write tests once it's supported
  (is (= 1 1)))
