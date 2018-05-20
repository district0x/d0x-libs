(ns tests.all
  (:require [cljs-time.core :as t]
            [cljs.test :refer [deftest is testing run-tests async use-fixtures]]
            [day8.re-frame.test :refer [run-test-async run-test-sync wait-for]]
            [district.ui.now.events :as now-events]
            [district.ui.now.subs :as now-subs]
            [district.ui.now]
            [district.ui.web3-sync-now.events :as sync-now-events]
            [district.ui.web3-sync-now]
            [district.ui.web3.subs :as web3-subs]
            [district.ui.web3.events :as web3-events]
            [district.ui.web3]
            [mount.core :as mount]
            [re-frame.core :as re-frame]))

(use-fixtures :each
  {:after (fn [] (mount/stop))})

(deftest tests
  (run-test-async
   (let [web3 (re-frame/subscribe [::web3-subs/web3])
         now (re-frame/subscribe [::now-subs/now])
         *prev-now* (atom nil)]

     (-> (mount/with-args {:web3 {:url "http://localhost:8549"}})
         (mount/start))

     (wait-for [::web3-events/web3-created]
       (wait-for [::now-events/update-now]
         (reset! *prev-now* @now)
         (re-frame/dispatch-sync [::sync-now-events/increment-now 8.64e+7])
         (wait-for [::now-events/update-now]
           (is (= 1000 (t/in-days (t/interval  @*prev-now* @now))))))))))
