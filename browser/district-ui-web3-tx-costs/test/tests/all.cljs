(ns tests.all
  (:require
    [cljs.spec.alpha :as s]
    [cljs.test :refer [deftest is testing run-tests async use-fixtures]]
    [day8.re-frame.test :refer [run-test-async run-test-sync wait-for]]
    [district.ui.conversion-rates.events :as rates-events]
    [district.ui.web3-tx-costs.events :as events]
    [district.ui.web3-tx-costs]
    [district.ui.web3-tx.events :as tx-events]
    [district.ui.web3-tx.subs :as tx-subs]
    [mount.core :as mount]
    [re-frame.core :refer [reg-event-fx dispatch-sync subscribe reg-cofx reg-sub dispatch trim-v]]))

(s/check-asserts true)

(use-fixtures
  :each
  {:after
   (fn []
     (mount/stop))})


(deftest tests
  (run-test-async
    (let [tx-hash "0xb56aa8d91028b90958bbb05570bf2e7a24b40962cff5557db013ccdf4c93dee4"
          tx (subscribe [::tx-subs/tx tx-hash])]

      (-> (mount/with-args
            {:web3 {:url "https://mainnet.infura.io/"}
             :web3-tx-costs {:currencies [:USD :EUR]}})
        (mount/start))

      (wait-for [::rates-events/set-conversion-rates ::rates-events/conversion-rates-load-failed]

        (dispatch [::tx-events/tx-loaded tx-hash {:gas-used 100000} {:gas-price 20000000000}])

        (wait-for [::events/tx-loaded]
          (wait-for [::tx-events/set-tx]
            (let [{:keys [:USD :EUR :ETH]} (:tx-costs @tx)]
              (is (pos? USD))
              (is (pos? EUR))
              (is (= 0.002 ETH)))))))))