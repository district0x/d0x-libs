(ns district.ui.smart-contracts.subs
  (:require
    [district.ui.smart-contracts.queries :as queries]
    [re-frame.core :refer [reg-sub]]))

(defn- sub-fn [query-fn]
  (fn [db [_ & args]]
    (apply query-fn db args)))

(reg-sub
  ::contracts
  queries/contracts)

(reg-sub
  ::contract
  (sub-fn queries/contract))

(reg-sub
  ::contract-address
  (sub-fn queries/contract-address))

(reg-sub
  ::contract-abi
  (sub-fn queries/contract-abi))

(reg-sub
  ::contract-bin
  (sub-fn queries/contract-bin))

(reg-sub
  ::contract-name
  (sub-fn queries/contract-name))

(reg-sub
  ::instance
  (sub-fn queries/instance))