(ns tests.all
  (:require
    [cljs-web3-next.core :as web3]
    [cljs.test :refer [deftest is testing run-tests async use-fixtures]]
    [day8.re-frame.test :refer [run-test-async wait-for run-test-sync]]
    [district.ui.smart-contracts.deploy-events :as deploy-events]
    [district.ui.smart-contracts.events :as contracts-events]
    [district.ui.smart-contracts.subs :as contracts-subs]
    [district.ui.smart-contracts]
    [district.ui.web3-account-balances.events :as events]
    [district.ui.web3.events :as web3-events]
    [district.ui.web3-account-balances.subs :as subs]
    [district.ui.web3-account-balances]
    [district.ui.web3-accounts.events :as accounts-events]
    [district.ui.web3-accounts.subs :as accounts-subs]
    [district.ui.web3-balances.events :as balances-events]
    [district.ui.web3-tx.events :as tx-events]
    [mount.core :as mount]
    [tests.mintable-token :as mintable-token]
    [re-frame.core :refer [reg-event-fx dispatch-sync subscribe reg-cofx reg-fx dispatch]]))


(use-fixtures
  :each
  {:after
   (fn []
     (mount/stop))})

(def ^:dynamic *dnt-addr* (atom "0x0e90d8f85fc3107df47d20444244feaa824d1082"))

(defn get-smart-contracts []
  {:DNT {:address @*dnt-addr*}})

(defn all-set-balance-events-pred [num]
  (let [*counter* (atom 0)]
    (fn [event]
      (when (sequential? event)
        (let [[event-name] event]
          (when (= event-name ::balances-events/set-balance)
            (swap! *counter* inc 1))
          (= @*counter* num))))))


(reg-event-fx
  ::balances-events/balance-load-failed
  (fn [_ [_ & args]]
    (#(do (cljs.pprint/pprint %) %) args)
    nil))


;; Not really a test, but convenient setup for real test
(deftest token-deployment
  (run-test-async
    (let [instance (subscribe [::contracts-subs/instance :mintable-token])
          contract-address (subscribe [::contracts-subs/contract-address :mintable-token])
          accounts (subscribe [::accounts-subs/accounts])]
      (-> (mount/with-args
            {:web3 {:url "http://localhost:8549"}
             :smart-contracts {:disable-loading-at-start? true
                               :contracts {:mintable-token mintable-token/token}}})
        (mount/start))

      (wait-for [::accounts-events/accounts-changed]
        (is (not (empty? @accounts)))

        (dispatch [::deploy-events/deploy-contract :mintable-token {:from (first @accounts)}])

        (wait-for [::contracts-events/set-contract ::deploy-events/contract-deploy-failed]
          (dispatch [::tx-events/send-tx {:instance @instance
                                          :fn :mint
                                          :args [(first @accounts) (web3/to-wei "1" :ether)]
                                          :tx-opts {:from (first @accounts)}}])

          (wait-for [::tx-events/tx-success ::tx-events/tx-error]
            (reset! *dnt-addr* @contract-address)))))))


(deftest tests
  (run-test-async
    (let [active-account-balance-eth (subscribe [::subs/active-account-balance])
          active-account-balance-dnt (subscribe [::subs/active-account-balance :DNT])
          account-balances (subscribe [::subs/accounts-balances])
          accounts (subscribe [::accounts-subs/accounts])]
      (-> (mount/with-args
            {:web3 {:url "http://localhost:8549"}
             :web3-balances {:contracts (get-smart-contracts)}
             :web3-account-balances {:for-contracts [:ETH :DNT]}})
        (mount/except [#'district.ui.smart-contracts/smart-contracts])
        (mount/start))

      (wait-for [::web3-events/web3-created]
        (wait-for [::accounts-events/accounts-changed]
          (is (not (empty? @accounts)))

          (wait-for [(all-set-balance-events-pred (* (count @accounts) 2)) ::balances-events/balance-load-failed]
            (is (= (count @account-balances) (count @accounts)))
            (is (every? identity (vals @account-balances)))

            (is (true? (> @active-account-balance-eth 0)))
            (is (true? (= @active-account-balance-dnt (web3/to-wei "1" :ether))))))))))
