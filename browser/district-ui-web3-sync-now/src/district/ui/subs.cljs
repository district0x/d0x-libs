(ns district.ui.subs
  (:require [re-frame.core :as re-frame]))

(re-frame/reg-sub
 :district/db
 (fn [db [_ key]]
   (get db key)))
