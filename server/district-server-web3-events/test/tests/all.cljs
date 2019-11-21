(ns tests.all
  (:require [cljs-web3-next.core :as web3-core]
            [cljs-web3-next.eth :as web3-eth]
            [cljs.core.async :refer [<!]]
            [cljs.test :refer-macros [deftest use-fixtures is testing async]]
            [district.server.smart-contracts :as smart-contracts]
            [district.server.web3 :refer [web3]]
            [district.server.web3-events :refer [register-callback! unregister-callbacks! register-after-past-events-dispatched-callback!]]
            [district.shared.async-helpers :as async-helpers]
            [mount.core :as mount]
            [tests.smart-contracts-test])
  (:require-macros [cljs.core.async.macros :refer [go]]))

(async-helpers/extend-promises-as-channels!)

(use-fixtures
  :each
  {:before (fn []
             (-> (mount/with-args
                   {:web3 {:url "ws://127.0.0.1:8545"}
                    :smart-contracts {:contracts-var #'tests.smart-contracts-test/smart-contracts
                                      :print-gas-usage? true
                                      :auto-mining? true}
                    :web3-events {:events {:my-contract/some-event [:my-contract :SomeEvent]
                                           :my-contract/some-other-event [:my-contract :SomeOtherEvent]}}
                    :logging {:level :info
                              :console? true}})
                 (mount/start-without #'district.server.web3-events/web3-events)))
   :after (fn []
            (mount/stop))})

(deftest test-web3-events
  (async done
         (go
           (let [connected? (<! (web3-eth/is-listening? @web3))
                 my-contract-address (smart-contracts/contract-address :my-contract)
                 some-event-from-past-called? (atom false)
                 some-other-event-from-past-called? (atom false)
                 after-past-events-callback-called? (atom false)
                 some-event-called? (atom false)
                 some-other-event-called? (atom false)]

             (is (true? connected?))
             (is (not= my-contract-address "0x0000000000000000000000000000000000000000"))

             ;; NOTE: this event ordering will only work on just deployed contracts

             ;; fire two past events
             (<! (smart-contracts/contract-send :my-contract :fire-some-event [7]))
             (<! (smart-contracts/contract-send :my-contract :fire-some-other-event [7]))

             (mount/start #'district.server.web3-events/web3-events)

             (register-callback! :my-contract/some-event (fn [err {:keys [:args :latest-event?]}]
                                                           (prn "@@@ some-event" args latest-event?)
                                                           (is (not err))
                                                           (if latest-event?
                                                             (do
                                                               (is (= "7" (:some-param args)))
                                                               (is (true? @some-other-event-called?))
                                                               (reset! some-event-called? true))
                                                             (do
                                                               ;; (is (= "1" (:some-param args)))
                                                               (is (false? @some-other-event-from-past-called?))
                                                               (reset! some-event-from-past-called? true)))))

             (register-callback! :my-contract/some-other-event (fn [err {:keys [:args :latest-event?]}]
                                                                 (prn "@@@ some-other-event" args latest-event?)
                                                                 (if latest-event?
                                                                   (do
                                                                     (is (= "7" (:some-other-param args)))
                                                                     (is (false? @some-event-called?))
                                                                     (reset! some-other-event-called? true))
                                                                   (do
                                                                     (is (true? @some-event-from-past-called?))
                                                                     (reset! some-other-event-from-past-called? true)))))

             (register-after-past-events-dispatched-callback! (fn []
                                                                (prn "@@@ after-past-events")
                                                                (is (true? @some-event-from-past-called?))
                                                                (is (true? @some-other-event-from-past-called?))
                                                                (reset! after-past-events-callback-called? true)))

             ;; fire two latest events (in different order)
             (<! (smart-contracts/contract-send :my-contract :fire-some-other-event [7]))
             (<! (smart-contracts/contract-send :my-contract :fire-some-event [7]))

             (is (true? @after-past-events-callback-called?))

             (done)))))
