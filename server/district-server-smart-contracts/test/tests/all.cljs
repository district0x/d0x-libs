(ns tests.all
  (:require
    [cljs.test :refer-macros [deftest is testing use-fixtures]]
    [district.server.smart-contracts :as contracts]
    [mount.core :as mount]
    [tests.smart-contracts]))

(use-fixtures
  :each
  {:before
   (fn []
     (-> (mount/with-args
           {:web3 {:port 8753}
            :smart-contracts {:contracts-var #'tests.smart-contracts/smart-contracts
                              :print-gas-usage? true
                              :auto-mining? true}})
       (mount/start)))
   :after
   (fn []
     (mount/stop))})

(deftest test-smart-contracts
  (is (false? (empty? (contracts/contract-abi :my-contract))))
  (is (false? (empty? (contracts/contract-bin :my-contract))))
  (is (= (contracts/contract-address :my-contract) "0x0000000000000000000000000000000000000000"))
  (is (= (contracts/contract-name :my-contract) "MyContract"))

  (is (map? (contracts/deploy-smart-contract! :my-contract {:arguments [1]})))

  (is (not= (contracts/contract-address :my-contract) "0x0000000000000000000000000000000000000000"))

  (is (= 1 (.toNumber (contracts/contract-call :my-contract :counter))))

  (is (= 5 (.toNumber (contracts/contract-call :my-contract :my-plus 2 3))))

  (let [tx-hash (contracts/contract-call :my-contract :increment-counter 2 {:gas 500000})]
    (is (string? tx-hash))
    (let [{:keys [:args]} (contracts/contract-event-in-tx tx-hash :my-contract :on-counter-incremented)]
      (is (= 3 (.toNumber (:counter args))))))

  (is (= 3 (.toNumber (contracts/contract-call :my-contract :counter)))))
