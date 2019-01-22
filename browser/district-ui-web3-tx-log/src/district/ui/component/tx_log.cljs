(ns district.ui.component.tx-log
  (:require [district.format :as format]
            [district.ui.web3-accounts.subs :as accounts-subs]
            [district.ui.web3-tx-log.events :as events]
            [district.ui.web3-tx-log.subs :as subs]
            [district.ui.web3-tx.events :as tx-events]
            [district.web3-utils :as web3-utils]
            [re-frame.core :refer [subscribe dispatch]]
            [reagent.core :as r]
            [soda-ash.core :as ui]))


(defn header [{:keys [:text] :as props
               :or {text "Transaction Log"}}]
  [:div.header
   (dissoc props :text)
   text])

(defn from-active-address-only-toggle [props]
  [ui/Checkbox
   (r/merge-props
    {:toggle true
     :label "Show transactions from active address only."
     :on-change #(dispatch [::events/set-settings {:from-active-address-only? (aget %2 "checked")}])}
    props)])


(defn settings []
  (let [settings (subscribe [::subs/settings])]
    (fn [{:keys [:from-active-address-only-toggle-props :from-active-address-only-toggle-el]
          :or {from-active-address-only-toggle-el from-active-address-only-toggle}
          :as props}]
      (let [{:keys [:from-active-address-only?]} @settings]
        [:div.settings
         (dissoc props :from-active-address-only-toggle-props)
         [from-active-address-only-toggle-el
          (merge
           {:checked from-active-address-only?}
           from-active-address-only-toggle-props)]]))))


(defn tx-created-on [{:keys [:tx :label] :as props
                      :or {label "Sent "}}]
  [:div.tx-created-on
   (dissoc props :tx :label)
   label
   (format/time-ago (:created-on tx))])


(defn tx-gas [{:keys [:tx :tx-cost-currency :label]
               :or {label "Gas used: "}
               :as props}]
  (let [{:keys [:status :gas-used :tx-costs]} tx]
    [:div.tx-gas
     (dissoc props :tx :tx-cost-currency :label)
     label
     (if (contains? #{:tx.status/success :tx.status/failure} status)
       (str (format/format-number gas-used {:max-fraction-digits 0})
            (when (and tx-cost-currency
                       (get tx-costs tx-cost-currency))
              (str "(" (format/format-currency
                        (get tx-costs tx-cost-currency)
                        {:currency tx-cost-currency})
                   ")")))
       "...")]))


(defn tx-from [{:keys [:tx :label]
                :or {label "From: "}
                :as props}]
  (let [from (:from tx)]
    [:div.tx-sender
     (dissoc props :tx :label)
     label
     [:a
      {:href (format/etherscan-addr-url from)
       :target :_blank}
      from]]))


(defn tx-id [{:keys [:tx :label]
              :or {label "Tx ID: "}
              :as props}]
  (let [{:keys [:hash]} tx]
      [:div.tx-id
       (dissoc props :tx :label)
       label
       [:a
        {:href (format/etherscan-tx-url hash)
         :target :_blank}
        hash]]))


(defn tx-value [{:keys [:tx] :as props}]
  [:div.tx-value
   (dissoc props :tx)
   (format/format-eth (web3-utils/wei->eth (:value tx)))])


(defn tx-name [{:keys [:tx] :as props}]
  (let [{:keys [:name]} (:tx-log tx)]
    [:div.tx-name
     (dissoc props :tx)
     name]))


(def tx-status->text
  {:tx.status/success "Completed"
   :tx.status/failure "Failed"
   :tx.status/pending "Pending"})


(defn tx-status [{:keys [:tx] :as props}]
  (let [{:keys [:status]} tx
        status-text (tx-status->text status)]
    [:div.tx-status
     (r/merge-props
      {:class (name status)}
      (dissoc props :tx))
     [:i.icon]
     [:div.tx-status-text status-text]]))


(defn tx-remove [{:keys [:tx] :as props}]
  [:i.icon.tx-remove
   (r/merge-props
    {:on-click (fn [e]
                 (dispatch [::tx-events/remove-tx (:hash tx)])
                 (.stopPropagation e))}
    (dissoc props :tx))])


(defn transaction [{:keys [:tx
                           :tx-name-props :tx-name-el
                           :tx-created-on-props :tx-created-on-el
                           :tx-gas-props :tx-gas-el
                           :tx-from-props :tx-from-el
                           :tx-id-props :tx-id-el
                           :tx-status-props :tx-status-el
                           :tx-value-props :tx-value-el
                           :tx-remove-props :tx-remove-el]
                    :or {tx-name-el tx-name
                         tx-created-on-el tx-created-on
                         tx-gas-el tx-gas
                         tx-from-el tx-from
                         tx-id-el tx-id
                         tx-status-el tx-status
                         tx-value-el tx-value
                         tx-remove-el tx-remove}
                    :as props}]
  (let [{:keys [:tx-log]} tx
        {:keys [:related-href]} tx-log]
    [:div.transaction
     (merge
      {:href related-href
       :on-click #(dispatch [::events/set-open false])}
      (dissoc props
              :tx :tx-name-props :tx-name-el
              :tx-created-on-props :tx-created-on-el
              :tx-gas-props :tx-gas-el
              :tx-from-props :tx-from-el
              :tx-id-props :tx-id-el
              :tx-status-props :tx-status-el
              :tx-value-props :tx-value-el
              :tx-remove-props :tx-remove-el
              :tx-cost-currency))
     [tx-name-el (assoc tx-name-props :tx tx)]
     [tx-created-on-el (assoc tx-created-on-props :tx tx)]
     [tx-gas-el (merge tx-gas-props (select-keys props [:tx :tx-cost-currency]))]
     [tx-from-el (assoc tx-from-props :tx tx)]
     [tx-id-el (assoc tx-id-props :tx tx)]
     [tx-status-el (assoc tx-status-props :tx tx)]
     [tx-value-el (assoc tx-value-props :tx tx)]
     [tx-remove-el (assoc tx-remove-props :tx tx)]]))


(defn no-transactions [{:keys [:text]
                        :or {text "You haven't made any transactions yet."}
                        :as props}]
  [:div.no-transactions
   (dissoc props :text)
   text])


(defn transactions []
  (let [tx-log (subscribe [::subs/txs])]
    (fn [{:keys [:tx-cost-currency :transaction-props :transaction-el :no-transactions-props :no-transactions-el]
          :or {transaction-el transaction
               no-transactions-el no-transactions}
          :as props}]
      (let [tx-log-items @tx-log]
        (if (seq tx-log-items)
          [:div.transactions
           (dissoc props :tx-cost-currency :transaction-props :transaction-el :no-transactions-props :no-transactions-el)
           (for [{:keys [:transaction-hash] :as tx} tx-log-items]
             [transaction-el
              (merge
               {:key transaction-hash
                :tx tx
                :tx-cost-currency tx-cost-currency}
               transaction-props)])]
          [no-transactions-el no-transactions-props])))))


(defn tx-log []
  (let [active-account (subscribe [::accounts-subs/active-account])
        open? (subscribe [::subs/open?])]
    (fn [{:keys [:header-props :header-el :settings-props :settings-el :transactions-props :transactions-el]
          :or {header-el header
               settings-el settings
               transactions-el transactions}
          :as props}]
      (when @active-account
        [:div.tx-log
         (merge
          {:class (when @open? "open")}
          (dissoc props :header-props :header-el :settings-props :settings-el :transactions-props :transactions-el))
         [header-el header-props]
         [:div.tx-content
          {:class (when @open? "open")}
          [settings-el settings-props]
          [transactions-el (merge transactions-props
                                  (select-keys props [:tx-cost-currency]))]]]))))
