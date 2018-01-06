(ns district.ui.web3-tx-log-core.events
  (:require
    [day8.re-frame.forward-events-fx]
    [district.ui.web3-tx-log-core.queries :as queries]
    [district.ui.web3-tx.events :as tx-events]
    [district.ui.web3-tx.queries :as tx-queries]
    [district0x.re-frame.spec-interceptors :refer [validate-first-arg validate-args]]
    [district0x.re-frame.web3-fx]
    [re-frame.core :refer [reg-event-fx trim-v inject-cofx]]
    [cljs.spec.alpha :as s]))

(def interceptors [trim-v])
(s/def ::tx-hash string?)

(reg-event-fx
  ::start
  [interceptors (inject-cofx :web3-tx-log-core-localstorage)]
  (fn [{:keys [:db :web3-tx-log-core-localstorage]}]
    {:db (queries/assoc-tx-hashes db (if (tx-queries/localstorage-disabled? db)
                                       '()
                                       (or (queries/tx-hashes web3-tx-log-core-localstorage) '())))
     :forward-events [{:register ::tx-event
                       :events #{::tx-events/add-tx ::tx-events/remove-tx}
                       :dispatch-to [::tx-event-fired]}
                      {:register ::clear-localstorage
                       :events #{::tx-events/clear-localstorage}
                       :dispatch-to [::clear-localstorage]}]}))


(reg-event-fx
  ::tx-event-fired
  [interceptors]
  (fn [{:keys [:db]} [[event tx-hash]]]
    {:dispatch (if (= event ::tx-events/add-tx)
                 [::add-tx-hash tx-hash]
                 [::remove-tx-hash tx-hash])}))


(defn- tx-event-fn [action-fn]
  (fn [{:keys [:db]} [tx-hash]]
    (let [new-db (action-fn db tx-hash)]
      (merge
        {:db new-db}
        (when-not (tx-queries/localstorage-disabled? db)
          {:web3-tx-log-core-localstorage (select-keys new-db [:district.ui.web3-tx-log-core])})))))


(reg-event-fx
  ::add-tx-hash
  [interceptors (validate-first-arg ::tx-hash)]
  (tx-event-fn queries/add-tx-hash))


(reg-event-fx
  ::remove-tx-hash
  [interceptors (validate-first-arg ::tx-hash)]
  (tx-event-fn queries/remove-tx-hash))


(reg-event-fx
  ::clear-localstorage
  (fn []
    {:web3-tx-log-core-localstorage nil}))


(reg-event-fx
  ::stop
  interceptors
  (fn [{:keys [:db]}]
    {:db (queries/dissoc-web3-tx-log-core db)
     :forward-events [{:unregister ::tx-event}
                      {:unregister ::clear-localstorage}]}))

