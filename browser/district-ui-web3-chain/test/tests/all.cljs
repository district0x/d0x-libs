(ns tests.all
  (:require
    [cljs.test :refer [deftest is testing run-tests async use-fixtures]]
    [day8.re-frame.test :refer [run-test-async wait-for run-test-sync]]
    [district.ui.web3-chain.events :as events]
    [district.ui.web3-chain.subs :as subs]
    [district.ui.web3-chain]
    [mount.core :as mount]
    [re-frame.core :refer [reg-event-fx dispatch-sync subscribe reg-cofx reg-fx dispatch]]))

(defn set-response [chain]
  (reg-fx
    :web3/call
    (fn [{:keys [:fns]}]
      (dispatch (vec (concat (:on-success (first fns))
                             [chain]))))))

(use-fixtures
  :each
  {:after
   (fn []
     (mount/stop))})


(deftest tests
  (run-test-async
    (let [chain (subscribe [::subs/chain])
          mock-chain "1"
          mock-chain2 "4"
          mock-chain3 "137"]

      (set-response mock-chain)

      (-> (mount/with-args
            {:web3 {:url "https://mainnet.infura.io/"}
             :web3-chain {}})
        (mount/start))

      (wait-for [::events/chain-changed ::events/chain-load-failed]
        (is (= @chain mock-chain))

        (dispatch [::events/set-chain mock-chain2])

        (wait-for [::events/chain-changed]
          (is (= @chain mock-chain2))

          (set-response mock-chain3)

          (dispatch [::events/poll-chain])

          (wait-for [::events/chain-changed]
            (is (= @chain mock-chain3))))))))

(deftest invalid-params-tests
  (run-test-sync
    (-> (mount/with-args
          {:web3 {:url "https://mainnet.infura.io/"}
           :web3-chain {}})
      (mount/start))

    (is (thrown? :default (dispatch-sync [::events/set-chain 1])))))

(deftest disable-loading-at-start-tests
  (run-test-sync

    (set-response "1")

    (-> (mount/with-args
          {:web3 {:url "https://mainnet.infura.io/"}
           :web3-chain {:disable-loading-at-start? true}})
      (mount/start))

    (is (nil? @(subscribe [::subs/chain])))))