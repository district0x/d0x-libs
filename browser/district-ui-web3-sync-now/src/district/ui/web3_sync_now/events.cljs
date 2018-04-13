(ns district.ui.web3-sync-now.events
  (:require [cljs-web3.eth :as web3-eth]
            [cljs-web3.evm :as web3-evm]
            [district.ui.now.events :as now-events]
            [district.ui.web3.events :as web3-events]
            [district.ui.web3.queries :as web3-queries]
            [district.ui.web3-sync-now.queries :as web3-sync-now-queries]
            [district.web3-utils :as web3-utils]
            [re-frame.core :as re-frame]
            [day8.re-frame.forward-events-fx]))

(def interceptors [re-frame/trim-v])

(re-frame/reg-event-fx
 ::set-now
 [interceptors]
 (fn [{:keys [:db]}]
   (let [web3 (web3-queries/web3 db)]
     {:dispatch [::now-events/set-now (->> (web3-eth/block-number web3)
                                           (web3-eth/get-block web3)
                                           :timestamp
                                           web3-utils/web3-time->date-time)]})))

(re-frame/reg-fx
 :increase-evm-time
 (fn [[web3 millis]]
   (web3-evm/increase-time! web3 millis)))

(re-frame/reg-event-fx
 ::increment-now
 [interceptors]
 (fn [{:keys [:db]} [millis]]
   {:dispatch [::now-events/increment-now millis]
    :increase-evm-time [(web3-queries/web3 db) millis]}))

(re-frame/reg-event-fx
 ::start
 [interceptors]
 (fn [{:keys [:db]}]
   {:forward-events {:register web3-sync-now-queries/db-key
                     :events #{::web3-events/web3-created}
                     :dispatch-to [::set-now]}}))

(re-frame/reg-event-fx
 ::stop
 [interceptors]
 (fn [{:keys [:db]}]
   {:forward-events {:unregister web3-sync-now-queries/db-key}}))
