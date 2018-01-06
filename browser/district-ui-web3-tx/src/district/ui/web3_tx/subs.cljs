(ns district.ui.web3-tx.subs
  (:require
    [district.ui.web3-tx.queries :as queries]
    [re-frame.core :refer [reg-sub]]))

(defn- sub-fn [query-fn]
  (fn [db [_ & args]]
    (apply query-fn db args)))

(reg-sub
  ::txs
  queries/txs)

(reg-sub
  ::tx
  (sub-fn queries/tx))

(reg-sub
  ::txs-with-status
  (sub-fn queries/txs-with-status))

