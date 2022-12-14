(ns district.ui.web3-sync-now.events
  (:require [cljs-web3-next.eth :as web3-eth]
            [cljs-web3-next.evm :as web3-evm]
            [day8.re-frame.forward-events-fx]
            [district.ui.logging.events :as logging]
            [district.ui.now.events :as now-events]
            [district.ui.web3-sync-now.queries :as web3-sync-now-queries]
            [district.ui.web3.events :as web3-events]
            [district.ui.web3.queries :as web3-queries]
            [district.web3-utils :as web3-utils]
            [district0x.re-frame.web3-fx]
            [re-frame.core :as re-frame]))

(def interceptors [re-frame/trim-v])
(def success-txt "Success")
(def error-txt "Error")

(re-frame/reg-event-fx
 ::increment-now
 [interceptors]
 (fn [{:keys [:db]} [seconds]]
   (let [web3 (web3-queries/web3 db)]
     {:dispatch [::now-events/increment-now (* seconds 1000)]
      :web3/call {:web3 web3
                  :fns [{:fn web3-evm/increase-time!
                         :args [seconds]
                         :on-success [::logging/info success-txt ::increment-now]
                         :on-error [::logging/error error-txt ::increment-now]}]}})))

(re-frame/reg-event-fx
 ::block-number
 [interceptors]
 (fn [{:keys [:db]}]
   (let [web3 (web3-queries/web3 db)]
     {:web3/call {:web3 web3
                  :fns [{:fn web3-eth/block-number
                         :args []
                         :on-success [::get-block]
                         :on-error [::logging/error error-txt ::block-number]}]}})))

(re-frame/reg-event-fx
 ::get-block
 [interceptors]
 (fn [{:keys [:db]} [number]]
   (let [web3 (web3-queries/web3 db)]
     {:web3/call {:web3 web3
                  :fns [{:fn web3-eth/get-block
                         :args [number]
                         :on-success [::set-now]
                         :on-error [::logging/error error-txt ::get-block]}]}})))

(re-frame/reg-event-fx
 ::set-now
 [interceptors]
 (fn [{:keys [:db]} [block]]
   (let [web3 (web3-queries/web3 db)]
     {:dispatch [::now-events/set-now (-> block
                                          :timestamp
                                          web3-utils/web3-time->date-time)]
      :log/info [success-txt ::set-now]})))

(re-frame/reg-event-fx
 ::start
 [interceptors]
 (fn [{:keys [:db]}]
   {:forward-events {:register web3-sync-now-queries/db-key
                     :events #{::web3-events/web3-created}
                     :dispatch-to [::block-number]}}))

(re-frame/reg-event-fx
 ::stop
 [interceptors]
 (fn [{:keys [:db]}]
   {:forward-events {:unregister web3-sync-now-queries/db-key}}))
