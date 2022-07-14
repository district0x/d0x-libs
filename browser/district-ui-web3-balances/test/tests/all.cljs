(ns tests.all
  (:require
    [cljs.spec.alpha :as s]
    [cljs-web3-next.core :as web3]
    [cljs-web3-next.eth :as web3-eth]
    [cljs.test :refer [deftest is testing run-tests async use-fixtures]]
    [day8.re-frame.test :refer [run-test-async wait-for run-test-sync]]
    [district.ui.web3-balances.events :as events]
    [district.ui.web3-balances.subs :as subs]
    [district.ui.web3-balances]
    [mount.core :as mount]
    [tests.smart-contracts-test :refer [smart-contracts creator]]
    [re-frame.core :refer [reg-event-fx dispatch-sync subscribe reg-cofx reg-fx dispatch]]))

(def abi-balance-of (js/JSON.parse "[{\"inputs\":[{\"internalType\":\"address\",\"name\":\"recipient\",\"type\":\"address\"},{\"internalType\":\"uint256\",\"name\":\"amount\",\"type\":\"uint256\"}],\"name\":\"transfer\",\"outputs\":[{\"internalType\":\"bool\",\"name\":\"\",\"type\":\"bool\"}],\"stateMutability\":\"nonpayable\",\"type\":\"function\"},{\"constant\":true,\"inputs\":[{\"name\":\"who\",\"type\":\"address\"}],\"name\":\"balanceOf\",\"outputs\":[{\"name\":\"\",\"type\":\"uint256\"}],\"payable\":false,\"stateMutability\":\"view\",\"type\":\"function\"},{\"anonymous\":false,\"inputs\":[{\"indexed\":true,\"name\":\"from\",\"type\":\"address\"},{\"indexed\":true,\"name\":\"to\",\"type\":\"address\"},{\"indexed\":false,\"name\":\"value\",\"type\":\"uint256\"}],\"name\":\"Transfer\",\"type\":\"event\"}]"))
(def address1 "0x1111111111111111111111111111111111111111")
(def address2 "0x2222222222222222222222222222222222222222")
(def ganache-url "localhost:8549")
(def web3 (web3/create-web3 (str "http://" ganache-url)))

(s/check-asserts true)

(use-fixtures
  :each
  {:after
   (fn []
     (mount/stop))})


(defn all-set-balance-events-pred [num]
  (let [*counter* (atom 0)]
    (fn [event]
      (when (sequential? event)
        (let [[event-name] event]
          (when (= event-name ::events/set-balance)
            (swap! *counter* inc 1))
          (= @*counter* num))))))


(reg-event-fx
  ::transfer-balance
  (fn [db [_ {:keys [:from :to :amount :contract]}]]
    (let [callback (fn [err res] (if err
                                   (js/console.log "ERROR: " err)
                                   (dispatch [::transfer-balance-done])))]
    (if (= contract :ETH)
      (web3-eth/send-transaction! web3 {:from from :to to :value amount} callback)
      (let [instance (web3-eth/contract-at web3 abi-balance-of (:address (get smart-contracts contract)))]
        (web3-eth/contract-send instance :transfer [to amount] {:from from}callback)))
    {:db db})))

(reg-event-fx
  ::transfer-balance-done
  (constantly nil))

(defn launch-test
  [url]
  (run-test-async
    (let [addr1-eth (subscribe [::subs/balance address1])
          addr1-eth2 (subscribe [::subs/balance address1 :ETH])
          addr2-eth (subscribe [::subs/balance address2])
          addr1-gnt (subscribe [::subs/balance address1 :GNT])
          addr2-gnt (subscribe [::subs/balance address2 (:address (:GNT smart-contracts))])
          addr1-icn (subscribe [::subs/balance address1 :ICN])
          addr2-icn (subscribe [::subs/balance address2 :ICN])
          addr1-omg (subscribe [::subs/balance address1 (web3-eth/contract-at web3 abi-balance-of (:address (:OMG smart-contracts)))])
          addr2-omg (subscribe [::subs/balance address2 :OMG])]

      (-> (mount/with-args
            {:web3 {:url url}
             :web3-balances {:contracts smart-contracts}})
        (mount/start))

      (dispatch [::events/load-balances [{:address address1 :watch? true}
                                         {:address address1 :contract :GNT :watch? true}
                                         {:address address1 :contract (web3-eth/contract-at web3 abi-balance-of (:address (:ICN smart-contracts))) :watch? true}
                                         {:address address1 :contract :OMG :watch? true}
                                         {:address address2 :watch? true}
                                         {:address address2 :contract :ICN :watch? true}
                                         {:address address2 :contract :OMG :watch? true}
                                         {:address address2 :contract (:address (:GNT smart-contracts)) :watch? true}]])

      (wait-for [(all-set-balance-events-pred 8) ::balance-load-failed]
        (is (true? (> (js/parseInt @addr1-eth) 0)))
        (is (true? (> (js/parseInt @addr1-eth2) 0)))
        (is (true? (= (js/parseInt @addr2-eth) 0)))
        (is (true? (> (js/parseInt @addr1-gnt) 0)))
        (is (true? (> (js/parseInt @addr2-gnt) 0)))
        (is (true? (> (js/parseInt @addr1-icn) 0)))
        (is (true? (= (js/parseInt @addr2-icn) 0)))
        (is (true? (> (js/parseInt @addr1-omg) 0)))
        (let [current-addr2-omg @addr2-omg
              current-addr1-eth (str @addr1-eth)]

        (dispatch [::transfer-balance {:from creator :to address2 :amount "10" :contract :OMG}])

        (wait-for [(all-set-balance-events-pred 3) ::balance-load-failed]
                  (is (true? (= @addr2-omg (str (+ (js/BigInt 10) (js/BigInt current-addr2-omg))))))


          (dispatch [::transfer-balance {:from creator :to address1 :amount "10" :contract :ETH}])

          (wait-for [(all-set-balance-events-pred 2) ::balance-load-failed]
                    (is (true? (= @addr1-eth (str (+ (js/BigInt 10) (js/BigInt current-addr1-eth))))))
                    (dispatch [::events/stop-watching-all]))))))))


(deftest test-http
  (launch-test (str "http://" ganache-url)))

(deftest test-ws
  (launch-test (str "ws://" ganache-url)))
