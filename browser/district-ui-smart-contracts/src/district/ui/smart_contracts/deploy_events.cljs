(ns district.ui.smart-contracts.deploy-events
  (:require
    [cljs-web3-next.eth :as web3-eth]
    [cljs.spec.alpha :as s]
    [district.ui.smart-contracts.events :as events]
    [district.ui.smart-contracts.queries :as queries]
    [district.ui.web3.queries :as web3-queries]
    [district0x.re-frame.spec-interceptors :refer [validate-first-arg validate-args]]
    [district0x.re-frame.web3-fx]
    [re-frame.core :refer [reg-event-fx trim-v console]]
    [cljs-web3-next.core :as web3]))

(def interceptors [trim-v])

(s/def ::from web3/address?)
(s/def ::arguments sequential?)
(s/def ::deploy-opts (s/keys :req-un [::from] :opt-un [::arguments]))


(reg-event-fx
  ::deploy-contract
  [interceptors (validate-args (s/cat :contract-key :district.ui.smart-contracts/contract-key
                                      :deploy-opts ::deploy-opts
                                      :args (s/* any?)))]
  (fn [{:keys [:db]} [contract-key {:keys [:arguments] :as opts}]]
    (let [{:keys [:abi :bin]} (queries/contract db contract-key)]
      {:web3/call {:web3 (web3-queries/web3 db)
                   :fns [{:fn web3-eth/contract-new
                          :args
                          (concat [abi]
                                  [{:data bin
                                    :arguments arguments
                                    }]
                                  [(merge {:gas 5000000}
                                          (dissoc opts :arguments))])
                          :on-success [::contract-deployed* contract-key opts]
                          :on-error [::contract-deploy-failed contract-key opts]}]}})))


(reg-event-fx
  ::contract-deployed*
  interceptors
  (fn [{:keys [:db]} [contract-key {:keys [:on-success]} instance]]
    (let [address (aget instance "options" "address")]
      (merge
        {:dispatch [::events/set-contract contract-key {:address address}]}
        (when on-success
          {:dispatch (vec (concat on-success [contract-key instance]))})))))


(reg-event-fx
  ::contract-deploy-failed
  interceptors
  (fn [{:keys [:db]} [contract-key {:keys [:on-error]} error]]
    (console :error contract-key error)
    (when on-error
      {:dispatch (vec (concat on-error [contract-key error]))})))

