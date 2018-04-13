(ns district.ui.events
  (:require [district.ui.db :as db]
            [re-frame.core :as re-frame]))

(def interceptors [re-frame/trim-v])

(re-frame/reg-event-db
 :initialize-db
 (fn [_ _]
   db/default-db))
