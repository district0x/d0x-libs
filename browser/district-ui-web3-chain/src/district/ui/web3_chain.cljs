(ns district.ui.web3-chain
  (:require
    [cljs.spec.alpha :as s]
    [district.ui.web3-chain.events :as events]
    [district.ui.web3]
    [district.ui.window-focus]
    [mount.core :as mount :refer [defstate]]
    [re-frame.core :as re-frame]))

(declare start)
(declare stop)

(defstate web3-chain
  :start (start (:web3-chain (mount/args)))
  :stop (stop))

(s/def ::disable-loading-at-start? boolean?)
(s/def ::disable-polling? boolean?)
(s/def ::polling-interval-ms number?)
(s/def ::load-injected-chain-only? boolean?)
(s/def ::opts (s/nilable (s/keys :opt-un [::disable-polling? ::polling-interval-ms ::load-injected-chain-only?
                                          ::disable-loading-at-start?])))

(defn start [opts]
  (s/assert ::opts opts)
  (re-frame/dispatch-sync [::events/start opts])
  opts)

(defn stop []
  (re-frame/dispatch-sync [::events/stop @web3-chain]))
