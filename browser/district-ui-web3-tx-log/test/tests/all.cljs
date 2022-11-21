(ns tests.all
  (:require
    [cljs.test :refer [deftest is testing run-tests async use-fixtures]]
    [day8.re-frame.test :refer [run-test-async run-test-sync wait-for]]
    [district.ui.smart-contracts.deploy-events :as deploy-events]
    [district.ui.smart-contracts.events :as contracts-events]
    [district.ui.smart-contracts.subs :as contracts-subs]
    [district.ui.smart-contracts]
    [district.ui.web3-accounts.events :as accounts-events]
    [district.ui.web3-accounts.subs :as accounts-subs]
    [district.ui.web3-tx-log.events :as events]
    [district.ui.web3-tx-log.subs :as subs]
    [district.ui.web3-tx-log]
    [district.ui.web3-tx.events :as tx-events]
    [district.ui.web3-tx]
    [mount.core :as mount]
    [tests.mintable-token :as mintable-token]
    [re-frame.core :refer [reg-event-fx dispatch-sync subscribe reg-cofx reg-sub dispatch trim-v]]
    [cljs-web3-next.core :as web3]
    [cljs-web3-next.utils :as web3-utils]))

(use-fixtures
  :each
  {:after
   (fn []
     (mount/stop))})


(deftest tx-success
  (run-test-async
    (let [txs (subscribe [::subs/txs])
          open? (subscribe [::subs/open?])
          settings (subscribe [::subs/settings])
          accounts (subscribe [::accounts-subs/accounts])
          instance (subscribe [::contracts-subs/instance :mintable-token])]
      (-> (mount/with-args
            {:web3 {:url "http://localhost:8549"}
             :smart-contracts {:disable-loading-at-start? true
                               :contracts {:mintable-token mintable-token/token}}
             :web3-tx {:disable-using-localstorage? true}
             :web3-tx-log {:open-on-tx-hash? true
                           :tx-costs-currencies [:USD]
                           :disable-using-localstorage? true}})
        (mount/start))

      (wait-for [::accounts-events/accounts-changed ::accounts-events/accounts-load-failed]
        (is (not @open?))
        (is (not (:from-active-address-only? @settings)))
        (dispatch [::deploy-events/deploy-contract :mintable-token {:from (first @accounts)}])
        (wait-for [::contracts-events/set-contract ::deploy-events/contract-deploy-failed]
          (is (not (nil? @instance)))
          (dispatch [::tx-events/send-tx {:instance @instance
                                          :fn :mint
                                          :args [(second @accounts) (web3/to-wei "1" :ether)]
                                          :tx-opts {:from (first @accounts)}
                                          :tx-log {:name "Tx Name 1"}}])

          (wait-for [::tx-events/tx-loaded ::tx-events/tx-error]
            (is (= (count @txs) 1))
            (is (= (:name (:tx-log (first @txs))) "Tx Name 1"))
            (is (true? @open?))

            (dispatch [::tx-events/send-tx {:instance @instance
                                            :fn :transfer
                                            :args [(first @accounts) (web3/to-wei "0.1" :ether)]
                                            :tx-opts {:from (second @accounts)}}])

            (wait-for [::tx-events/tx-loaded ::tx-events/tx-error]
              (is (= (count @txs) 2))


              (dispatch [::events/set-settings {:from-active-address-only? true}])
              (wait-for [::events/set-settings]
                (is (= (count @txs) 1))
                (is (= (:name (:tx-log (first @txs))) "Tx Name 1"))

                (dispatch [::events/set-open false])
                (wait-for [::events/set-settings]
                  (is (not @open?))
                  (is (= {:from-active-address-only? true :open? false}
                         @settings)))))))))))
