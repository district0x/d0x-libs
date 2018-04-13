(ns district.ui.core
  (:require [cljs.spec.alpha :as s]
            [district.ui.component.main :refer [main-panel]]
            [district.ui.reagent-render] ;; mount (module)
            [district.ui.events]
            [district.ui.subs]
            [mount.core :as mount]
            [reagent.core :as r]
            [re-frame.core :as re-frame]
            [re-frisk.core :as re-frisk]

            [district.ui.now]
            [district.ui.web3]
            [district.ui.web3-sync-now]

            ))

(def debug?
  ^boolean js/goog.DEBUG)

(defn dev-setup []
  (when debug?
    (enable-console-print!)
    (re-frisk/enable-re-frisk!)
    (s/check-asserts true)
    (println "dev mode")))

(defn ^:export init []
  (dev-setup)
  (re-frame/dispatch-sync [:initialize-db])
  (-> (mount/with-args {:reagent-render {:id "app"
                                         :component-var #'main-panel}
                        :web3 {:url "http://127.0.0.1:8549"
                               #_"https://mainnet.infura.io/"}})
      (mount/start)))
