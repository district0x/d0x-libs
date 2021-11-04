(ns district.ui.web3-chain.effects
  (:require
    [cljs-web3.utils :refer [cljkk->js]]
    [re-frame.core :refer [reg-fx dispatch]]))

(reg-fx
  ::watch-chain
  (fn [{:keys [:on-change]}]
    (when (some-> js/window (aget "ethereum") (aget "on"))
      (js-invoke
        (aget js/window "ethereum")
        "on"
        "chainChanged"
        (fn [chain]
          (dispatch (conj on-change (js->clj chain))))))))

(reg-fx
  ::rpc-request
  (fn [{:keys [:method :params :on-error]}]
    (.catch
      (js-invoke
        (aget js/window "ethereum")
        "request"
        (cljkk->js {:method method :params params}))
      (fn [error#]
        (when on-error
          (dispatch (conj on-error (js->clj error# :keywordize-keys true))))))))

