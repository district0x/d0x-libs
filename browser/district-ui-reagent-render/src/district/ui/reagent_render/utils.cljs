(ns district.ui.reagent-render.utils
  (:require [reagent.dom :as r-dom]
            [re-frame.core :as re-frame]))

(defn re-render [{:keys [id target component-var]}]
  (re-frame/clear-subscription-cache!)
  ;; Needs to be async, so subscriptions in components can do dispatch-sync
  ;; so we prevent "dispatch-sync inside event" error
  (js/setTimeout
    (fn [] (r-dom/render [component-var] (cond target target id (.getElementById js/document id))))
    0))
