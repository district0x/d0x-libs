(ns district.ui.component.tx-button
  (:require [district.ui.web3-accounts.subs :as accounts-subs]
            [re-frame.core :as re-frame]
            [reagent.core :as r]
            [soda-ash.core :as ui]))

(defn tx-button [{:keys [:pending? :pending-text :raised-button?] :as props
                  :or {:pending-text "Sending..."}} & children]
  (into [ui/Button
         (r/merge-props
          (dissoc props :raised-button? :pending-text :pending?)
          (merge
           (when-not @(re-frame/subscribe [::accounts-subs/active-account])
             {:disabled true})
           (when pending?
             {:disabled true})))]
        (if pending?
          [pending-text]
          children)))
