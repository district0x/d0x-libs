(ns district.ui.component.active-account
  (:require
    [district.ui.web3-accounts.events :as accounts-events]
    [district.ui.web3-accounts.subs :as accounts-subs]
    [re-frame.core :refer [subscribe dispatch]]
    [reagent.core :as r]))

(defn active-account []
  (let [accounts (subscribe [::accounts-subs/accounts])
        active-acc (subscribe [::accounts-subs/active-account])]
    (fn [{:keys [:select-props :single-account-props]}]
      (if (seq @accounts)
        [:div.active-account
         (if (= 1 (count @accounts))
           [:span.single-account
            single-account-props
            @active-acc]
           [:select
            (r/merge-props
              {:class "active-account-select"
               :value @active-acc
               :on-change (fn [item]
                            (let [val (-> item .-target .-value)]
                              (dispatch [::accounts-events/set-active-account val])))}
              select-props)
            (map (fn [account]
                   [:option {:key account :value account} account]) @accounts)])]
        [:div
         "Ethereum wallet not connected"]))))
