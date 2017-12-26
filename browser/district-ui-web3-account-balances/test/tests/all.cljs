(ns tests.all
  (:require
    [cljs.test :refer [deftest is testing run-tests async use-fixtures]]
    [day8.re-frame.test :refer [run-test-async wait-for run-test-sync]]
    [district.ui.web3-account-balances.events :as events]
    [district.ui.web3-account-balances.subs :as subs]
    [district.ui.web3-account-balances]
    [district.ui.web3-accounts.events :as accounts-events]
    [district.ui.web3-accounts.subs :as accounts-subs]
    [district.ui.web3-balances.events :as balances-events]
    [mount.core :as mount]
    [re-frame.core :refer [reg-event-fx dispatch-sync subscribe reg-cofx reg-fx dispatch]]))


(use-fixtures
  :each
  {:after
   (fn []
     (mount/stop))})

(def smart-contracts
  {:DNT {:address "0x0abdace70d3790235af448c88547603b945604ea"}})

(defn all-set-balance-events-pred [num]
  (let [*counter* (atom 0)]
    (fn [event]
      (when (sequential? event)
        (let [[event-name] event]
          (when (= event-name ::balances-events/set-balance)
            (swap! *counter* inc 1))
          (= @*counter* num))))))


(deftest tests
  (run-test-async
    (let [active-account-balance-eth (subscribe [::subs/active-account-balance])
          active-account-balance-dnt (subscribe [::subs/active-account-balance :DNT])
          account-balances (subscribe [::subs/account-balances])
          accounts (subscribe [::accounts-subs/accounts])]
      (-> (mount/with-args
            {:web3 {:url "http://localhost:8549"}
             :web3-balances {:contracts smart-contracts}
             :web3-account-balances {:for-contracts [:ETH :DNT]}})
        (mount/start))

      (wait-for [::accounts-events/accounts-changed]
        (is (not (empty? @accounts)))

        (wait-for [(all-set-balance-events-pred (* (count @accounts) 2))]
          (is (= (count @account-balances) (count @accounts)))
          (is (every? identity (vals @account-balances)))

          (is (true? (.gt @active-account-balance-eth 0)))
          (is (true? (.eq @active-account-balance-dnt 0))))))))