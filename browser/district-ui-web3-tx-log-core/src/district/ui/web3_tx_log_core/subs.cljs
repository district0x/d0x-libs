(ns district.ui.web3-tx-log-core.subs
  (:require
    [district.ui.web3-tx-log-core.queries :as queries]
    [re-frame.core :refer [reg-sub]]))

(defn- sub-fn [query-fn]
  (fn [db [_ & args]]
    (apply query-fn db args)))

(reg-sub
  ::txs
  (sub-fn queries/txs))

(reg-sub
  ::tx-hashes
  (sub-fn queries/tx-hashes))

