(ns district.ui.component.main
  (:require [re-frame.core :as re-frame]
            [district.ui.now.subs :as now-subs]
            [district.ui.web3-sync-now.events :as sync-now-events]
            [cljs-time.core :as t]))

;; (defn button-1 []
;;   (let [handle-click (fn [_]
;;                        (re-frame/dispatch [::now-events/set-now (t/minus (t/now) (t/years 1))]))]
;;     (fn []
;;       [:button {:class "btn-send"
;;                 :type "button"
;;                 :on-click #(handle-click %)}
;;        "SET TO ONE YEAR BACK"])))

(defn button-2 []
  (let [handle-click (fn [_]
                       (re-frame/dispatch [::sync-now-events/increment-now (t/in-millis (t/interval (t/now)
                                                                                              (t/plus (t/now) (t/months 1))))]))]
    (fn []
      [:button {:class "btn-send"
                :type "button"
                :on-click #(handle-click %)}
       "INCREMENT BY ONE MONTH"])))

(defn counter []
  (let [now (re-frame/subscribe [::now-subs/now])]
    (fn []
      [:div "Current time: " (str @now)])))

(defn main-panel []
  [:div.app
   [:div.header
    [:div.header-content
     [:div.topbar
      #_[button-1]
      [button-2]]
     [counter]
     [:div.bottombar
      [:div "BOTTOM"]]]]])
