(ns tests.all
  (:require
    [cljs-web3.core :as web3]
    [cljs-web3.eth :as web3-eth]
    [cljs.test :refer [deftest is testing run-tests async use-fixtures]]
    [day8.re-frame.test :refer [run-test-async wait-for run-test-sync]]
    [district.ui.web3-balances.events :as events]
    [district.ui.web3-balances.subs :as subs]
    [district.ui.web3-balances]
    [mount.core :as mount]
    [re-frame.core :refer [reg-event-fx dispatch-sync subscribe reg-cofx reg-fx dispatch]]))

(def abi-balance-of (js/JSON.parse "[{\"constant\":true,\"inputs\":[{\"name\":\"who\",\"type\":\"address\"}],\"name\":\"balanceOf\",\"outputs\":[{\"name\":\"\",\"type\":\"uint256\"}],\"payable\":false,\"stateMutability\":\"view\",\"type\":\"function\"},{\"anonymous\":false,\"inputs\":[{\"indexed\":true,\"name\":\"from\",\"type\":\"address\"},{\"indexed\":true,\"name\":\"to\",\"type\":\"address\"},{\"indexed\":false,\"name\":\"value\",\"type\":\"uint256\"}],\"name\":\"Transfer\",\"type\":\"event\"}]"))
(def address1 "0x0000000000000000000000000000000000000000")
(def address2 "0x000000000000000000000000000000000000dEaD")
(def web3 (web3/create-web3 "https://mainnet.infura.io/"))

(def smart-contracts
  {:GNT {:address "0xa74476443119A942dE498590Fe1f2454d7D4aC0d"}
   :ICN {:address "0x888666CA69E0f178DED6D75b5726Cee99A87D698"}
   :OMG {:address "0xd26114cd6EE289AccF82350c8d8487fedB8A0C07"}})

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


(deftest tests
  (run-test-async
    (let [addr1-eth (subscribe [::subs/balance address1])
          addr1-eth2 (subscribe [::subs/balance address1 :ETH])
          addr2-eth (subscribe [::subs/balance address2])
          addr1-gnt (subscribe [::subs/balance address1 :GNT])
          addr2-gnt (subscribe [::subs/balance address2 (:address (:GNT smart-contracts))])
          addr1-icn (subscribe [::subs/balance address1 :ICN])
          addr2-omg (subscribe [::subs/balance address2 (web3-eth/contract-at web3 abi-balance-of (:address (:OMG smart-contracts)))])]

      (-> (mount/with-args
            {:web3 {:url "https://mainnet.infura.io/"}
             :web3-balances {:contracts smart-contracts}})
        (mount/start))

      ;; Even though infura servers will return error for :watch? true, because they don't support events,
      ;; it's okay for purposes of tests
      (dispatch [::events/load-balances [{:address address1 :watch? true}
                                         {:address address1 :contract :GNT :watch? true}
                                         {:address address1 :contract (web3-eth/contract-at web3 abi-balance-of (:address (:ICN smart-contracts))) :watch? true}
                                         {:address address2 :watch? true}
                                         {:address address2 :contract :OMG :watch? true}
                                         {:address address2 :contract (:address (:GNT smart-contracts)) :watch? true}]])

      (wait-for [(all-set-balance-events-pred 6) ::balance-load-failed]
        (is (true? (.gt @addr1-eth 0)))
        (is (true? (.gt @addr1-eth2 0)))
        (is (true? (.gt @addr2-eth 0)))
        (is (true? (.gt @addr1-gnt 0)))
        (is (true? (.gt @addr2-gnt 0)))
        (is (true? (.gt @addr1-icn 0)))
        (is (true? (.gt @addr2-omg 0)))

        (dispatch [::events/stop-watching-all])))))