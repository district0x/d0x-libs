(ns tests.all
  (:require
    [cljs-web3.core :as web3]
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
    [re-frame.core :refer [reg-event-fx dispatch-sync subscribe reg-cofx reg-fx dispatch]]))

;; MintableToken
(def mintable-token-abi (clj->js (js/JSON.parse "[{\"constant\":true,\"inputs\":[],\"name\":\"mintingFinished\",\"outputs\":[{\"name\":\"\",\"type\":\"bool\"}],\"payable\":false,\"stateMutability\":\"view\",\"type\":\"function\"},{\"constant\":false,\"inputs\":[{\"name\":\"_spender\",\"type\":\"address\"},{\"name\":\"_value\",\"type\":\"uint256\"}],\"name\":\"approve\",\"outputs\":[{\"name\":\"\",\"type\":\"bool\"}],\"payable\":false,\"stateMutability\":\"nonpayable\",\"type\":\"function\"},{\"constant\":true,\"inputs\":[],\"name\":\"totalSupply\",\"outputs\":[{\"name\":\"\",\"type\":\"uint256\"}],\"payable\":false,\"stateMutability\":\"view\",\"type\":\"function\"},{\"constant\":false,\"inputs\":[{\"name\":\"_from\",\"type\":\"address\"},{\"name\":\"_to\",\"type\":\"address\"},{\"name\":\"_value\",\"type\":\"uint256\"}],\"name\":\"transferFrom\",\"outputs\":[{\"name\":\"\",\"type\":\"bool\"}],\"payable\":false,\"stateMutability\":\"nonpayable\",\"type\":\"function\"},{\"constant\":false,\"inputs\":[{\"name\":\"_to\",\"type\":\"address\"},{\"name\":\"_amount\",\"type\":\"uint256\"}],\"name\":\"mint\",\"outputs\":[{\"name\":\"\",\"type\":\"bool\"}],\"payable\":false,\"stateMutability\":\"nonpayable\",\"type\":\"function\"},{\"constant\":false,\"inputs\":[{\"name\":\"_spender\",\"type\":\"address\"},{\"name\":\"_subtractedValue\",\"type\":\"uint256\"}],\"name\":\"decreaseApproval\",\"outputs\":[{\"name\":\"\",\"type\":\"bool\"}],\"payable\":false,\"stateMutability\":\"nonpayable\",\"type\":\"function\"},{\"constant\":true,\"inputs\":[{\"name\":\"_owner\",\"type\":\"address\"}],\"name\":\"balanceOf\",\"outputs\":[{\"name\":\"balance\",\"type\":\"uint256\"}],\"payable\":false,\"stateMutability\":\"view\",\"type\":\"function\"},{\"constant\":false,\"inputs\":[],\"name\":\"finishMinting\",\"outputs\":[{\"name\":\"\",\"type\":\"bool\"}],\"payable\":false,\"stateMutability\":\"nonpayable\",\"type\":\"function\"},{\"constant\":true,\"inputs\":[],\"name\":\"owner\",\"outputs\":[{\"name\":\"\",\"type\":\"address\"}],\"payable\":false,\"stateMutability\":\"view\",\"type\":\"function\"},{\"constant\":false,\"inputs\":[{\"name\":\"_to\",\"type\":\"address\"},{\"name\":\"_value\",\"type\":\"uint256\"}],\"name\":\"transfer\",\"outputs\":[{\"name\":\"\",\"type\":\"bool\"}],\"payable\":false,\"stateMutability\":\"nonpayable\",\"type\":\"function\"},{\"constant\":false,\"inputs\":[{\"name\":\"_spender\",\"type\":\"address\"},{\"name\":\"_addedValue\",\"type\":\"uint256\"}],\"name\":\"increaseApproval\",\"outputs\":[{\"name\":\"\",\"type\":\"bool\"}],\"payable\":false,\"stateMutability\":\"nonpayable\",\"type\":\"function\"},{\"constant\":true,\"inputs\":[{\"name\":\"_owner\",\"type\":\"address\"},{\"name\":\"_spender\",\"type\":\"address\"}],\"name\":\"allowance\",\"outputs\":[{\"name\":\"\",\"type\":\"uint256\"}],\"payable\":false,\"stateMutability\":\"view\",\"type\":\"function\"},{\"constant\":false,\"inputs\":[{\"name\":\"newOwner\",\"type\":\"address\"}],\"name\":\"transferOwnership\",\"outputs\":[],\"payable\":false,\"stateMutability\":\"nonpayable\",\"type\":\"function\"},{\"anonymous\":false,\"inputs\":[{\"indexed\":true,\"name\":\"to\",\"type\":\"address\"},{\"indexed\":false,\"name\":\"amount\",\"type\":\"uint256\"}],\"name\":\"Mint\",\"type\":\"event\"},{\"anonymous\":false,\"inputs\":[],\"name\":\"MintFinished\",\"type\":\"event\"},{\"anonymous\":false,\"inputs\":[{\"indexed\":true,\"name\":\"previousOwner\",\"type\":\"address\"},{\"indexed\":true,\"name\":\"newOwner\",\"type\":\"address\"}],\"name\":\"OwnershipTransferred\",\"type\":\"event\"},{\"anonymous\":false,\"inputs\":[{\"indexed\":true,\"name\":\"owner\",\"type\":\"address\"},{\"indexed\":true,\"name\":\"spender\",\"type\":\"address\"},{\"indexed\":false,\"name\":\"value\",\"type\":\"uint256\"}],\"name\":\"Approval\",\"type\":\"event\"},{\"anonymous\":false,\"inputs\":[{\"indexed\":true,\"name\":\"from\",\"type\":\"address\"},{\"indexed\":true,\"name\":\"to\",\"type\":\"address\"},{\"indexed\":false,\"name\":\"value\",\"type\":\"uint256\"}],\"name\":\"Transfer\",\"type\":\"event\"}]")))
(def mintable-token-bin "0x606060405260038054600160a860020a03191633600160a060020a0316179055610a078061002e6000396000f3006060604052600436106100c45763ffffffff7c010000000000000000000000000000000000000000000000000000000060003504166305d2035b81146100c9578063095ea7b3146100f057806318160ddd1461011257806323b872dd1461013757806340c10f191461015f578063661884631461018157806370a08231146101a35780637d64bcb4146101c25780638da5cb5b146101d5578063a9059cbb14610204578063d73dd62314610226578063dd62ed3e14610248578063f2fde38b1461026d575b600080fd5b34156100d457600080fd5b6100dc61028e565b604051901515815260200160405180910390f35b34156100fb57600080fd5b6100dc600160a060020a036004351660243561029e565b341561011d57600080fd5b61012561030a565b60405190815260200160405180910390f35b341561014257600080fd5b6100dc600160a060020a0360043581169060243516604435610310565b341561016a57600080fd5b6100dc600160a060020a0360043516602435610492565b341561018c57600080fd5b6100dc600160a060020a036004351660243561059f565b34156101ae57600080fd5b610125600160a060020a0360043516610699565b34156101cd57600080fd5b6100dc6106b4565b34156101e057600080fd5b6101e861073f565b604051600160a060020a03909116815260200160405180910390f35b341561020f57600080fd5b6100dc600160a060020a036004351660243561074e565b341561023157600080fd5b6100dc600160a060020a0360043516602435610849565b341561025357600080fd5b610125600160a060020a03600435811690602435166108ed565b341561027857600080fd5b61028c600160a060020a0360043516610918565b005b60035460a060020a900460ff1681565b600160a060020a03338116600081815260026020908152604080832094871680845294909152808220859055909291907f8c5be1e5ebec7d5bd14f71427d1e84f3dd0314c0f7b2291e5b200ac8c7c3b9259085905190815260200160405180910390a350600192915050565b60005481565b6000600160a060020a038316151561032757600080fd5b600160a060020a03841660009081526001602052604090205482111561034c57600080fd5b600160a060020a038085166000908152600260209081526040808320339094168352929052205482111561037f57600080fd5b600160a060020a0384166000908152600160205260409020546103a8908363ffffffff6109b316565b600160a060020a0380861660009081526001602052604080822093909355908516815220546103dd908363ffffffff6109c516565b600160a060020a03808516600090815260016020908152604080832094909455878316825260028152838220339093168252919091522054610425908363ffffffff6109b316565b600160a060020a03808616600081815260026020908152604080832033861684529091529081902093909355908516917fddf252ad1be2c89b69c2b068fc378daa952ba7f163c4a11628f55a4df523b3ef9085905190815260200160405180910390a35060019392505050565b60035460009033600160a060020a039081169116146104b057600080fd5b60035460a060020a900460ff16156104c757600080fd5b6000546104da908363ffffffff6109c516565b6000908155600160a060020a038416815260016020526040902054610505908363ffffffff6109c516565b600160a060020a0384166000818152600160205260409081902092909255907f0f6798a560793a54c3bcfe86a93cde1e73087d944c0ea20544137d41213968859084905190815260200160405180910390a2600160a060020a03831660007fddf252ad1be2c89b69c2b068fc378daa952ba7f163c4a11628f55a4df523b3ef8460405190815260200160405180910390a350600192915050565b600160a060020a033381166000908152600260209081526040808320938616835292905290812054808311156105fc57600160a060020a033381166000908152600260209081526040808320938816835292905290812055610633565b61060c818463ffffffff6109b316565b600160a060020a033381166000908152600260209081526040808320938916835292905220555b600160a060020a0333811660008181526002602090815260408083209489168084529490915290819020547f8c5be1e5ebec7d5bd14f71427d1e84f3dd0314c0f7b2291e5b200ac8c7c3b925915190815260200160405180910390a35060019392505050565b600160a060020a031660009081526001602052604090205490565b60035460009033600160a060020a039081169116146106d257600080fd5b60035460a060020a900460ff16156106e957600080fd5b6003805474ff0000000000000000000000000000000000000000191660a060020a1790557fae5184fba832cb2b1f702aca6117b8d265eaf03ad33eb133f19dde0f5920fa0860405160405180910390a150600190565b600354600160a060020a031681565b6000600160a060020a038316151561076557600080fd5b600160a060020a03331660009081526001602052604090205482111561078a57600080fd5b600160a060020a0333166000908152600160205260409020546107b3908363ffffffff6109b316565b600160a060020a0333811660009081526001602052604080822093909355908516815220546107e8908363ffffffff6109c516565b600160a060020a0380851660008181526001602052604090819020939093559133909116907fddf252ad1be2c89b69c2b068fc378daa952ba7f163c4a11628f55a4df523b3ef9085905190815260200160405180910390a350600192915050565b600160a060020a033381166000908152600260209081526040808320938616835292905290812054610881908363ffffffff6109c516565b600160a060020a0333811660008181526002602090815260408083209489168084529490915290819020849055919290917f8c5be1e5ebec7d5bd14f71427d1e84f3dd0314c0f7b2291e5b200ac8c7c3b92591905190815260200160405180910390a350600192915050565b600160a060020a03918216600090815260026020908152604080832093909416825291909152205490565b60035433600160a060020a0390811691161461093357600080fd5b600160a060020a038116151561094857600080fd5b600354600160a060020a0380831691167f8be0079c531659141344cd1fd0a4f28419497f9722a3daafe3b4186f6b6457e060405160405180910390a36003805473ffffffffffffffffffffffffffffffffffffffff1916600160a060020a0392909216919091179055565b6000828211156109bf57fe5b50900390565b6000828201838110156109d457fe5b93925050505600a165627a7a72305820749e47101bc93b8c11c90834f85ed4d495ba036d551d6a8aaeb3331198973e650029")
(def mintable-token {:abi mintable-token-abi
                     :bin mintable-token-bin
                     :name "MintableToken"})

(use-fixtures
  :each
  {:after
   (fn []
     (mount/stop))})

(def *dnt-addr* (atom "0x0e90d8f85fc3107df47d20444244feaa824d1082"))

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
                               :contracts {:mintable-token mintable-token}}})
        (mount/start))

      (wait-for [::accounts-events/accounts-changed]
        (is (not (empty? @accounts)))

        (dispatch [::deploy-events/deploy-contract :mintable-token {:from (first @accounts)}])

        (wait-for [::contracts-events/set-contract ::deploy-events/contract-deploy-failed]
          (dispatch [::tx-events/send-tx {:instance @instance
                                          :fn :mint
                                          :args [(first @accounts) (web3/to-wei 1 :ether)]
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

            (is (true? (.gt @active-account-balance-eth 0)))
            (is (true? (.eq @active-account-balance-dnt (web3/to-wei 1 :ether))))))))))
