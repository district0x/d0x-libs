(ns tests.smart-contracts-tests
  (:require
    [cljs.test :refer [deftest is testing run-tests async use-fixtures]]
    [day8.re-frame.test :refer [run-test-async wait-for run-test-sync]]
    [district.ui.smart-contracts.deploy-events :as deploy-events]
    [district.ui.smart-contracts.events :as events]
    [district.ui.smart-contracts.subs :as subs]
    [district.ui.smart-contracts]
    [district.ui.web3-accounts.events :as accounts-events]
    [district.ui.web3-accounts.subs :as accounts-subs]
    [district.ui.web3-accounts]
    [mount.core :as mount]
    [re-frame.core :refer [subscribe reg-fx dispatch]]
    [cljs-web3-next.core :as web3]))

(def responses
  {"./Contract1.abi" "[{\"constant\":true,\"inputs\":[],\"name\":\"owner\",\"outputs\":[{\"name\":\"\",\"type\":\"address\"}],\"payable\":false,\"stateMutability\":\"view\",\"type\":\"function\",\"signature\":\"0x8da5cb5b\"},{\"constant\":false,\"inputs\":[{\"name\":\"newOwner\",\"type\":\"address\"}],\"name\":\"transferOwnership\",\"outputs\":[],\"payable\":false,\"stateMutability\":\"nonpayable\",\"type\":\"function\",\"signature\":\"0xf2fde38b\"},{\"inputs\":[],\"payable\":false,\"stateMutability\":\"nonpayable\",\"type\":\"constructor\"}]"
   "./Contract1.bin" "12306040523415600e57600080fd5b603580601b6000396000f3006060604052600080fd00a165627a7a72305820b36bcb7ce114631229920f09a26ec479d8bcac7fbe4c0857cfd512d76c7df91f0029"
   "./Contract2.abi" "[{\"constant\":true,\"inputs\":[],\"name\":\"owner\",\"outputs\":[{\"name\":\"\",\"type\":\"address\"}],\"payable\":false,\"stateMutability\":\"view\",\"type\":\"function\",\"signature\":\"0x8da5cb5b\"},{\"constant\":false,\"inputs\":[{\"name\":\"newOwner\",\"type\":\"address\"}],\"name\":\"transferOwnership\",\"outputs\":[],\"payable\":false,\"stateMutability\":\"nonpayable\",\"type\":\"function\",\"signature\":\"0xf2fde38b\"},{\"inputs\":[],\"payable\":false,\"stateMutability\":\"nonpayable\",\"type\":\"constructor\"}]"
   "./Contract2.bin" "45606040523415600e57600080fd5b603580601b6000396000f3006060604052600080fd00a165627a7a72305820b36bcb7ce114631229920f09a26ec479d8bcac7fbe4c0857cfd512d76c7df91f0029"})

(def smart-contracts
  {:contract1 {:name "Contract1" :address "0xfbb1b73c4f0bda4f67dca266ce6ef42f520fbb98"}
   :contract2 {:name "Contract2" :address "0x38cefd943120474e031b72a841e4a891f1ba3648"}
   :deploy-test-contract {:name "DeployTestContract"
                          :abi (js/JSON.parse "[{\"inputs\":[{\"name\":\"someNumber\",\"type\":\"uint256\"}],\"payable\":false,\"stateMutability\":\"nonpayable\",\"type\":\"constructor\"}]")
                          :bin "0x60606040523415600e57600080fd5b604051602080607183398101604052808051915050801515602e57600080fd5b50603580603c6000396000f3006060604052600080fd00a165627a7a72305820f6c231e485f5b65831c99412cbcad5b4e41a4b69d40f3d4db8de3a38137701fb0029"}})


(reg-fx
  :http-xhrio
  (fn [requests]
    (doseq [{:keys [:uri :on-success]} requests]
      (dispatch (vec (concat on-success [(responses uri)]))))))


(use-fixtures
  :each
  {:after
   (fn []
     (mount/stop))})


(deftest tests
  (run-test-async
    (let [contracts (subscribe [::subs/contracts])
          contract1 (subscribe [::subs/contract :contract1])
          contract2 (subscribe [::subs/contract :contract2])

          contract1-addr (subscribe [::subs/contract-address :contract1])
          contract2-addr (subscribe [::subs/contract-address :contract2])

          contract1-abi (subscribe [::subs/contract-abi :contract1])
          contract2-abi (subscribe [::subs/contract-abi :contract2])

          contract1-bin (subscribe [::subs/contract-bin :contract1])
          contract2-bin (subscribe [::subs/contract-bin :contract2])

          contract1-instance (subscribe [::subs/instance :contract1])
          contract2-instance (subscribe [::subs/instance :contract2])

          contract1-name (subscribe [::subs/contract-name :contract1])
          contract2-name (subscribe [::subs/contract-name :contract2])]

      (-> (mount/with-args
            {:web3 {:url "https://mainnet.infura.io/ "}
             :web3-accounts {:disable-loading-at-start? true}
             :smart-contracts {:contracts smart-contracts
                               :load-bin? true
                               :contracts-path "./"}})
        (mount/start))

      (wait-for [::events/contracts-loaded ::events/contract-load-failed]
        (is (= (-> @contracts :contract1 :name)
               (-> smart-contracts :contract1 :name)
               (:name @contract1)
               @contract1-name))

        (is (= (-> @contracts :contract2 :name)
               (-> smart-contracts :contract2 :name)
               (:name @contract2)
               @contract2-name))

        (is (= (-> @contracts :contract1 :address)
               (-> smart-contracts :contract1 :address)
               (:address @contract1)
               @contract1-addr
               (clojure.string/lower-case (aget @contract1-instance "options" "address"))))

        (is (= (-> @contracts :contract2 :address)
               (-> smart-contracts :contract2 :address)
               (:address @contract2)
               @contract2-addr
               (clojure.string/lower-case (aget @contract2-instance "options" "address"))))

        (is (= (js/JSON.stringify (-> @contracts :contract1 :abi))
               (responses "./Contract1.abi")
               (js/JSON.stringify (:abi @contract1))
               (js/JSON.stringify @contract1-abi)
               (js/JSON.stringify (aget @contract1-instance "options" "jsonInterface"))))

        (is (= (js/JSON.stringify (-> @contracts :contract2 :abi))
               (responses "./Contract2.abi")
               (js/JSON.stringify (:abi @contract2))
               (js/JSON.stringify @contract2-abi)
               (js/JSON.stringify (aget @contract2-instance "options" "jsonInterface"))
               ))

        (is (= (-> @contracts :contract1 :bin)
               (str "0x" (responses "./Contract1.bin"))
               (:bin @contract1)
               @contract1-bin))

        (is (= (-> @contracts :contract2 :bin)
               (str "0x" (responses "./Contract2.bin"))
               (:bin @contract2)
               @contract2-bin))))))

(deftest tests2
  (run-test-async
    (let [contracts (subscribe [::subs/contracts])
          contract1-bin (subscribe [::subs/contract-bin :contract1])
          contract1-abi (subscribe [::subs/contract-abi :contract1])]
      (-> (mount/with-args
            {:web3 {:url "https://mainnet.infura.io/ "}
             :web3-accounts {:disable-loading-at-start? true}
             :smart-contracts {:contracts {}
                               :contracts-path "./"
                               :disable-loading-at-start? true}})
        (mount/start))

      (is (= {} @contracts))

      (dispatch [::events/load-contracts {:contracts (select-keys smart-contracts [:contract1])
                                          :contracts-path "./"}])
      (wait-for [::events/contracts-loaded ::events/contract-load-failed]
        (is (= (js/JSON.stringify (-> @contracts :contract1 :abi))
               (responses "./Contract1.abi")
               (js/JSON.stringify @contract1-abi)))

        (testing "Doesn't load bin unless explicitly told"
          (is (nil? @contract1-bin)))))))


(deftest deploy-tests
  (run-test-async
    (let [contract-address (subscribe [::subs/contract-address :deploy-test-contract])
          contract-instance (subscribe [::subs/instance :deploy-test-contract])
          active-account (subscribe [::accounts-subs/active-account])]

      (-> (mount/with-args
            {:web3 {:url "http://localhost:8549"}
             :smart-contracts {:contracts smart-contracts
                               :disable-loading-at-start? true}})
        (mount/start))


      (is (nil? @contract-address))
      (is (nil? @contract-instance))

      (wait-for [::accounts-events/active-account-changed ::accounts-events/accounts-load-failed]
        (dispatch [::deploy-events/deploy-contract :deploy-test-contract {:arguments [1]
                                                                          :from @active-account}])

        (wait-for [::events/set-contract ::deploy-events/contract-deploy-failed]
          (is (web3/address? @contract-address))
          (is (not (nil? @contract-instance))))))))


