(ns district.ui.web3-tx-id.subs
  (:require
    [district.ui.web3-tx-id.queries :as queries]
    [re-frame.core :refer [reg-sub]]))

(defn- sub-fn [query-fn]
  (fn [db [_ & args]]
    (apply query-fn db args)))

(reg-sub
  ::tx-hash
  (sub-fn queries/tx-hash))

(reg-sub
  ::tx
  (sub-fn queries/tx))

(reg-sub
  ::tx-status
  (sub-fn queries/tx-status))

(reg-sub
  ::tx-pending?
  (sub-fn queries/tx-pending?))

(reg-sub
  ::tx-success?
  (sub-fn queries/tx-success?))

(reg-sub
  ::tx-error?
  (sub-fn queries/tx-error?))


