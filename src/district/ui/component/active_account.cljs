(ns district.ui.component.active-account
  (:require
    [district.ui.web3-accounts.events :as accounts-events]
    [district.ui.web3-accounts.subs :as accounts-subs]
    [re-frame.core :refer [subscribe dispatch]]
    [reagent.core :as r]
    [soda-ash.core :as ui]))

(defn active-account []
  (let [accounts (subscribe [::accounts-subs/accounts])
        active-acc (subscribe [::accounts-subs/active-account])]
    (fn [{:keys [:select-props :single-account-props]}]
      (when (seq @accounts)
        [:div.active-account
         (if (= 1 (count @accounts))
           [:span.single-account
            single-account-props
            @active-acc]
           [ui/Select
            (r/merge-props
              {:select-on-blur false
               :class "active-account-select"
               :value @active-acc
               :on-change (fn [e data]
                            (dispatch [::accounts-events/set-active-account (aget data "value")]))
               :fluid true
               :options (doall (for [acc @accounts]
                                 {:value acc :text acc}))}
              select-props)])]))))