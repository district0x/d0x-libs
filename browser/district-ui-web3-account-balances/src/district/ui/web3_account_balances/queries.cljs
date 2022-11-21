(ns district.ui.web3-account-balances.queries
  (:require
    [district.ui.web3-accounts.queries :as accounts-queries]
    [district.ui.web3-balances.queries :as balances-queries]))

(defn active-account-balance [db & [contract]]
  (balances-queries/balance db (accounts-queries/active-account db) contract))

(defn accounts-balances [db & [contract]]
  (reduce
    (fn [acc account]
      (assoc acc account (balances-queries/balance db account contract)))
    {}
    (accounts-queries/accounts db)))