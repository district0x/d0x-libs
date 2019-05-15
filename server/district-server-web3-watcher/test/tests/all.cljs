(ns tests.all
  (:require
    [cljs.test :refer-macros [deftest is testing use-fixtures async]]
    [district.server.web3 :refer [web3]]
    [district.server.web3-watcher :as web3-watcher]
    [mount.core :as mount]))

(use-fixtures
  :each
  {:after
   (fn []
     (mount/stop))})

(def *connected?* (atom true))

(defn set-connected! [connected?]
  (reset! *connected?* connected?))

(deftest test-web3-watcher
  (let [went-offline? (atom false)
        went-online-again? (atom false)]
    (-> (mount/with-args {:web3 {:port 8549}
                          :web3-watcher {:interval 1000
                                         :confirmations 4
                                         :on-online #(reset! went-online-again? true)
                                         :on-offline #(reset! went-offline? true)}})
      (mount/start-without #'web3-watcher/web3-watcher))

    (aset @web3 "isConnected" (fn [] @*connected?*))

    (mount/start #'web3-watcher/web3-watcher)

    (is (false? @went-offline?))
    (is (false? @went-online-again?))

    (set-connected! false)

    (async done
      (js/setTimeout
        (fn []
          (is (false? @went-offline?))
          (is (false? @went-online-again?))
          (js/setTimeout
            (fn []
              (is (true? @went-offline?))
              (is (false? @went-online-again?))
              (set-connected! true)
              (js/setTimeout
                (fn []
                  (is (true? @went-online-again?))
                  (is (= 4 @(:confirmations-left @web3-watcher/web3-watcher)))
                  (done))
                1100))
            1100))
        4000))))

