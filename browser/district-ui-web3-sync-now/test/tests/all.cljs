(ns tests.all
  (:require [cljs.spec.alpha :as s]
            [cljs-time.core :as t]
            [cljs.test :refer [deftest is testing run-tests async use-fixtures]]
            [day8.re-frame.test :refer [run-test-async run-test-sync wait-for]]
            [district.ui.now.events :as now-events]
            [district.ui.now.subs :as now-subs]
            [district.ui.now]
            [district.ui.web3-sync-now.events :as sync-now-events]
            [district.ui.web3-sync-now]
            [district.ui.web3.queries :as web3-queries]
            [district.ui.web3.events :as web3-events]
            [district.ui.web3]
            [mount.core :as mount]
            [re-frame.core :as re-frame]))

(s/check-asserts true)

(use-fixtures :each
  {:after (fn [] (mount/stop))})


(re-frame/reg-event-fx
  ::mine-block
  (fn [{:keys [:db]} [_ _a]]
    (let [web3 (web3-queries/web3 db)
          callback (fn [err _] (if err
                                   (js/console.log "ERROR: " err)
                                   (re-frame/dispatch [::sync-now-events/block-number])))]
      (cljs-web3-next.evm/mine-block! web3)
      {:db db})))


(deftest tests
  (run-test-async
   (let [now (re-frame/subscribe [::now-subs/now])
         *prev-now* (atom nil)]

     (-> (mount/with-args {:web3 {:url "http://localhost:8549"}})
         (mount/start))

     (wait-for [::web3-events/web3-created]
       (wait-for [::now-events/update-now]
         (reset! *prev-now* @now)
         (re-frame/dispatch-sync [::sync-now-events/increment-now 3600])
         (wait-for [::now-events/update-now]
           (is (= 1 (t/in-hours (t/interval  @*prev-now* @now))))
           (re-frame/dispatch-sync [::mine-block])
           (wait-for [::now-events/update-now]
              (is (= 1 (t/in-hours (t/interval  @*prev-now* @now)))))))))))
