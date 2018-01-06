(ns district.ui.web3-tx.events
  (:require
    [day8.re-frame.forward-events-fx]
    [district.ui.web3-tx.queries :as queries]
    [district.ui.web3.events :as web3-events]
    [district.ui.web3.queries :as web3-queries]
    [district0x.re-frame.spec-interceptors :refer [validate-first-arg validate-args]]
    [district0x.re-frame.web3-fx]
    [re-frame.core :refer [reg-event-fx trim-v inject-cofx]]
    [cljs.spec.alpha :as s]))

(def interceptors [trim-v])
(s/def ::tx-hash string?)
(s/def ::tx-data map?)

(reg-event-fx
  ::start
  [interceptors (inject-cofx :web3-tx-localstorage)]
  (fn [{:keys [:db :web3-tx-localstorage]} [{:keys [:disable-using-localstorage?]}]]
    (let [txs (if disable-using-localstorage? {} (queries/txs web3-tx-localstorage))]
      {:db (-> db
             (queries/merge-txs txs)
             (queries/assoc-opt :disable-using-localstorage? disable-using-localstorage?))
       :forward-events {:register ::web3-created
                        :events #{::web3-events/web3-created}
                        :dispatch-to [::watch-pending-txs]}})))


(reg-event-fx
  ::watch-pending-txs
  interceptors
  (fn [{:keys [:db]}]
    (let [pending-txs (queries/txs db {:status :tx.status/pending})]
      (when (seq pending-txs)
        {:web3/watch-transactions {:web3 (web3-queries/web3 db)
                                   :transactions (for [tx-hash (keys pending-txs)]
                                                   {:id (str :district.ui.web3-tx tx-hash)
                                                    :tx-hash tx-hash
                                                    :on-tx-success [::tx-success {}]
                                                    :on-tx-error [::tx-error {}]})}}))))


(reg-event-fx
  ::send-tx
  interceptors
  (fn [{:keys [:db]} [{:keys [:instance :fn :tx-opts :args] :as opts}]]
    {:web3/call
     {:web3 (web3-queries/web3 db)
      :fns [(merge opts
                   {:instance instance
                    :fn fn
                    :args args
                    :tx-opts tx-opts
                    :on-tx-hash [::tx-hash opts]
                    :on-tx-hash-error [::tx-hash-error opts]
                    :on-tx-receipt [::tx-receipt opts]
                    :on-tx-success [::tx-success opts]
                    :on-tx-error [::tx-error opts]})]}}))


(reg-event-fx
  ::tx-hash
  interceptors
  (fn [{:keys [:db]} [{:keys [:on-tx-hash]} tx-hash]]
    (merge
      {:dispatch [::add-tx tx-hash]}
      (when on-tx-hash
        {:dispatch-n [(vec (concat on-tx-hash [tx-hash]))]}))))


(reg-event-fx
  ::tx-hash-error
  interceptors
  (fn [{:keys [:db]} [{:keys [:on-tx-hash-error]} & args]]
    (when on-tx-hash-error
      {:dispatch-n [(vec (concat on-tx-hash-error args))]})))


(reg-event-fx
  ::tx-success
  interceptors
  (fn [{:keys [:db]} [{:keys [:on-tx-success]} {:keys [:transaction-hash] :as tx-receipt}]]
    (let [tx-receipt (assoc tx-receipt :status :tx.status/success)]
      (merge
        {:dispatch [::set-tx transaction-hash tx-receipt]}
        (when on-tx-success
          {:dispatch-n [(vec (concat on-tx-success tx-receipt))]})))))


(reg-event-fx
  ::tx-error
  interceptors
  (fn [{:keys [:db]} [{:keys [:on-tx-error]} {:keys [:transaction-hash] :as tx-receipt}]]
    (let [tx-receipt (assoc tx-receipt :status :tx.status/error)]
      (merge
        {:dispatch [::set-tx transaction-hash tx-receipt]}
        (when on-tx-error
          {:dispatch-n [(vec (concat on-tx-error tx-receipt))]})))))


(reg-event-fx
  ::tx-receipt
  interceptors
  (fn [{:keys [:db]} [{:keys [:on-tx-receipt]} & args]]
    (when on-tx-receipt
      {:dispatch-n [(vec (concat on-tx-receipt args))]})))


(reg-event-fx
  ::add-tx
  [interceptors (validate-first-arg ::tx-hash)]
  (fn [{:keys [:db]} [tx-hash]]
    (let [new-db (queries/merge-tx-data db tx-hash {:created-on (js/Date.)
                                                    :transaction-hash tx-hash
                                                    :status :tx.status/pending})]
      (merge
        {:db new-db}
        (when-not (queries/localstorage-disabled? db)
          {:web3-tx-localstorage (select-keys new-db [:district.ui.web3-tx])})))))


(reg-event-fx
  ::set-tx
  [interceptors (validate-args (s/cat :tx-hash ::tx-hash
                                      :tx-data ::tx-data
                                      :args (s/* any?)))]
  (fn [{:keys [:db]} [tx-hash tx-data]]
    (let [new-db (queries/merge-tx-data db tx-hash tx-data)]
      (merge
        {:db new-db}
        (when-not (queries/localstorage-disabled? db)
          {:web3-tx-localstorage (select-keys new-db [:district.ui.web3-tx])})))))


(reg-event-fx
  ::clear-localstorage
  (fn []
    {:web3-tx-localstorage nil}))


(reg-event-fx
  ::stop
  interceptors
  (fn [{:keys [:db]}]
    {:db (queries/dissoc-web3-tx db)
     :web3/stop-watching {:ids (map (fn [[tx-hash]]
                                      (str :district.ui.web3-tx tx-hash))
                                    (queries/txs db {:status :tx.status/pending}))}
     :forward-events {:unregister ::web3-created}}))

