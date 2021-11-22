(ns district.ui.web3-chain.events
  (:require [cljs-web3.core :as web3]
            [cljs-web3.utils :refer [cljkk->js]]
            [cljs.spec.alpha :as s]
            [day8.re-frame.forward-events-fx]
            [district.ui.web3-chain.effects :as effects]
            [district.ui.web3-chain.queries :as queries]
            [district.ui.web3.events :as web3-events]
            [district.ui.web3.queries :as web3-queries]
            [district.ui.window-focus.queries :as window-focus-queries]
            [district0x.re-frame.interval-fx]
            [district0x.re-frame.spec-interceptors :as spec-interceptors]
            [district0x.re-frame.web3-fx]
            [re-frame.core :as re-frame]))

(def interceptors [re-frame/trim-v])

(s/def ::chain-id (s/nilable string?))

(re-frame/reg-event-fx
 ::start
 interceptors
 (fn [{:keys [:db]} [{:keys [:disable-polling? :polling-interval-ms :disable-loading-at-start?]
                      :or {polling-interval-ms 4000}
                      :as opts}]]
   (merge
    {:db (queries/assoc-chain db nil)}
    (when-not disable-loading-at-start?
      {:forward-events {:register ::load-chain
                        :events #{::web3-events/web3-created}
                        :dispatch-to [::load-chain opts]}})
    (when (and (not disable-polling?)
               (not disable-loading-at-start?))
      (if (some-> js/window (aget "ethereum") (aget "on"))
        {::effects/watch-chain {:on-change [::set-chain]}}
        {:dispatch-interval {:dispatch [::poll-chain opts]
                             :id ::poll-chain
                             :ms polling-interval-ms}})))))

(re-frame/reg-event-fx
 ::poll-chain
 interceptors
 (fn [{:keys [:db]} [opts]]
   (merge
    (when (and (window-focus-queries/focused? db)         ;; Important perf optimisation
               (web3-queries/web3 db))
      {:dispatch [::load-chain opts]})
    (when (and (web3-queries/web3 db)
               (not (web3-queries/web3-injected? db)))
      {:clear-interval {:id ::poll-chain}}))))

(re-frame/reg-event-fx
 ::load-chain
 interceptors
 (fn [{:keys [:db]} [{:keys [:load-injected-chain-only?]}]]
   (when-let [web3 (web3-queries/web3 db)]
     (if (and load-injected-chain-only?
              (not (web3-queries/web3-injected? db)))
       {:dispatch [::set-chain nil]}
       {:web3/call {:web3 web3
                    :fns [{:fn web3/version-network
                           :on-success [::set-chain]
                           :on-error [::chain-load-failed]}]}}))))

(re-frame/reg-event-fx
 ::chain-load-failed
 interceptors
 (fn []
   {:dispatch [::set-chain nil]}))

(re-frame/reg-event-fx
 ::set-chain
 [interceptors (spec-interceptors/validate-first-arg ::chain-id)]
 (fn [{:keys [:db]} [chain-id]]
   (let [chain-id (str (web3/to-decimal chain-id))]
    (when (not= chain-id (queries/chain db))
      {:db (queries/assoc-chain db chain-id)
       :dispatch [::chain-changed {:new chain-id :old (queries/chain db)}]}))))

(re-frame/reg-event-fx
  ::request-switch-chain
  [interceptors]
  (fn [{:keys [:db]} [chain-id {:keys [:chain-info]}]]
    {::effects/rpc-request {:method "wallet_switchEthereumChain"
                            :params [{:chain-id (web3/to-hex chain-id)}]
                            :on-error [::switch-chain-error {:chain-info chain-info}]}}))

(re-frame/reg-event-fx
  ::switch-chain-error
  [interceptors]
  (fn [{:keys [:db]} [{:keys [:chain-info]} error]]
    (when (and chain-info (= 4902 (:code error))) ; 4902 is the error code when chain is unrecognized. It'll try to add it first
      {:dispatch [::request-add-chain chain-info]})))

(re-frame/reg-event-fx
  ::request-add-chain
  [interceptors]
  (fn [{:keys [:db]} [{:keys [:chain-id] :as chain-info}]]
    {::effects/rpc-request {:method "wallet_addEthereumChain"
                            :params [(assoc chain-info :chain-id (web3/to-hex chain-id))]}}))

(re-frame/reg-event-fx
 ::chain-changed
 (constantly nil))

(re-frame/reg-event-fx
 ::stop
 interceptors
 (fn [{:keys [:db]} [{:keys [:disable-loading-at-start?]}]]
   (merge
    {:db (queries/dissoc-web3-chain db)
     :clear-interval {:id ::poll-chain}}
    (when-not disable-loading-at-start?
      {:forward-events {:unregister ::load-chain}}))))
