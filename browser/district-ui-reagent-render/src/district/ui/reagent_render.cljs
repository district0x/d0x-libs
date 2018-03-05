(ns district.ui.reagent-render
  (:require [cljs.pprint :as pprint]
            [cljs.spec.alpha :as s]
            [district.ui.reagent-render.events :as events]
            [district.ui.reagent-render.spec :as spec]
            [mount.core :as mount :refer [defstate]]
            [reagent.core :as r]
            [re-frame.core :as re-frame]))

(defn start [opts]
  {:pre [(s/assert ::spec/opts opts)]}
  (re-frame/dispatch-sync [::events/start opts]))

(defstate district-ui-notification
  :start (start (:reagent-render (mount/args)))
  :stop :stopped)
