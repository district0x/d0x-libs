(ns district.ui.web3-balances.subs
  (:require
    [district.ui.web3-balances.queries :as queries]
    [re-frame.core :refer [reg-sub]]))

(defn- sub-fn [query-fn]
  (fn [db [_ & args]]
    (apply query-fn db args)))

(reg-sub
  ::contracts
  queries/contracts)

(reg-sub
  ::contract-address
  (sub-fn queries/contract-address))

(reg-sub
  ::balances
  queries/balances)

(reg-sub
  ::balance
  (sub-fn queries/balance))