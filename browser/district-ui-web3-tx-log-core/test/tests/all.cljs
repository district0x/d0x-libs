(ns tests.all
  (:require
    [cljs.test :refer [deftest is testing run-tests async use-fixtures]]
    [day8.re-frame.test :refer [run-test-async run-test-sync wait-for]]
    [district.ui.web3-tx-log-core.events :as events]
    [district.ui.web3-tx-log-core.subs :as subs]
    [district.ui.web3-tx-log-core]
    [district.ui.web3-tx.events :as tx-events]
    [mount.core :as mount]
    [re-frame.core :refer [reg-event-fx dispatch-sync subscribe reg-cofx reg-sub dispatch trim-v]]))

(reg-event-fx
  ::clear-localstorage
  (fn [{:keys [:db]}]
    {:dispatch [::tx-events/clear-localstorage]
     ;; Hack so the storage gets actually cleared before tests cut off execution
     :dispatch-later [{:ms 50 :dispatch [::localstorage-cleared]}]}))

(reg-event-fx
  ::localstorage-cleared
  (constantly nil))


(use-fixtures
  :each
  {:after
   (fn []
     (mount/stop))})


(deftest tests
  (run-test-async
    (let [txs (subscribe [::subs/txs {:status :tx.status/pending}])
          tx-hashes (subscribe [::subs/tx-hashes])
          tx-hash "0xdb42d65cf74fe4a65e5e6fd613c12492e7e1130f95be2ec5f6ca31eb170d3604"
          tx-hash2 "0xfce3412ed5bac2b964d1ea88dce0adeea1ca52906e7c03b5f4492418f9046d6d"]

      (-> (mount/with-args
            {:web3 {:url "https://mainnet.infura.io/"}})
        (mount/start))

      (dispatch [::tx-events/tx-hash {:tx-log {:a 1 :b 2}} tx-hash])

      (wait-for [::events/add-tx-hash]
        (is (= @tx-hashes (list tx-hash)))
        (is (empty? @(subscribe [::subs/txs {:status :tx.status/success}])))
        (let [{:keys [:transaction-hash :status :tx-log]} (first @txs)]
          (is (= tx-hash transaction-hash))
          (is (= status :tx.status/pending))
          (is (= tx-log {:a 1 :b 2})))

        (dispatch [::tx-events/add-tx tx-hash2])

        (wait-for [::events/add-tx-hash]
          (is (= @tx-hashes (list tx-hash2 tx-hash)))
          (is (= 2 (count @txs)))

          (let [{:keys [:transaction-hash :status]} (first @txs)]
            (is (= tx-hash2 transaction-hash))
            (is (= status :tx.status/pending))

            (dispatch [::tx-events/remove-tx tx-hash])
            (wait-for [::events/remove-tx-hash]
              (is (= 1 (count @txs)))
              (is (= @tx-hashes (list tx-hash2)))

              (dispatch [::clear-localstorage])
              (wait-for [::localstorage-cleared]))))))))
