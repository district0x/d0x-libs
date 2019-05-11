(ns tests.all
  (:require
    [cljs-web3.core :as web3]
    [cljs.test :refer-macros [deftest use-fixtures is testing async]]
    [district.server.smart-contracts :as contracts]
    [district.server.web3-events :refer [register-callback! unregister-callbacks! register-after-past-events-dispatched-callback!]]
    [mount.core :as mount]
    [tests.smart-contracts]))


(use-fixtures
  :each
  {:before (fn []
             (-> (mount/with-args
                   {:web3 {:port 8549}
                    :smart-contracts {:contracts-var #'tests.smart-contracts/smart-contracts
                                      :print-gas-usage? true
                                      :auto-mining? true}
                    :web3-events {:events {:my-contract/some-event [:my-contract :SomeEvent {} {:from-block 0 :to-block "latest"}]
                                           :my-contract/some-other-event [:my-contract :SomeOtherEvent {} {:from-block 0 :to-block "latest"}]}}
                    :logging {:level :info
                              :console? true}})
               (mount/start-without #'district.server.web3-events/web3-events)))
   :after (fn []
            (mount/stop))})


(deftest test-web3-events

  (async done
    (-> (contracts/deploy-smart-contract! :my-contract [])

      (.then #(is (not= (contracts/contract-address :my-contract) "0x0000000000000000000000000000000000000000")))

      (.then #(contracts/contract-call :my-contract :fire-some-other-event [4]))

      (.then #(contracts/contract-call :my-contract :fire-some-event [20]))

      (.then (fn []
               (mount/start #'district.server.web3-events/web3-events)

               (let [some-other-event-called? (atom false)
                     after-past-events-callback-called? (atom false)]

                 (register-after-past-events-dispatched-callback! (fn []
                                                                    (is (true? @some-other-event-called?))
                                                                    (reset! after-past-events-callback-called? true)))

                 (register-callback! :my-contract/some-other-event (fn [err {:keys [:args :latest-event?]}]
                                                                     (is (not err))
                                                                     (is (= 4 (web3/to-decimal (:some-other-param args))))
                                                                     (is (nil? latest-event?))
                                                                     (reset! some-other-event-called? true)))
                 (register-callback! :my-contract/some-event (fn [err {:keys [:args :latest-event?]}]
                                                               (is (not err))
                                                               (is (= 20 (web3/to-decimal (:some-param args))))
                                                               (is (true? latest-event?))
                                                               (is (true? @some-other-event-called?))
                                                               (is (true? @after-past-events-callback-called?))
                                                               (done)))))))))

