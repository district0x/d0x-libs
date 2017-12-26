(ns district.ui.web3-account-balances.subs
  (:require
    [district.ui.web3-account-balances.queries :as queries]
    [re-frame.core :refer [reg-sub]]))

(defn- sub-fn [query-fn]
  (fn [db [_ & args]]
    (apply query-fn db args)))

(reg-sub
  ::active-account-balance
  (sub-fn queries/active-account-balance))

(reg-sub
  ::account-balances
  (sub-fn queries/account-balances))


