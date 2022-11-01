(ns tests.all
  (:require [cljs.test :refer [deftest is testing run-tests async use-fixtures]]
            [cljs-react-test.simulate :as simulate]
            [cljs-react-test.utils :as test-utils]
            [hickory.core :as hickory]
            [day8.re-frame.test :refer [run-test-async run-test-sync wait-for]]
            [district.ui.reagent-render]
            [mount.core :as mount :refer [defstate]]
            [re-frame.core :as re-frame]
            [district.ui.web3]
            [district.ui.web3-accounts]
            [district.ui.component.tx-button :as tx-button]))

(def container (atom nil))

(defn mock-html []
  [:div#app [tx-button/tx-button {:disabled false
                                  :pending-text "Transacting..."
                                  :on-click #(prn "click!")}
             "TX BUTTON"]])

(use-fixtures :each
  {:before #(do (reset! container (test-utils/new-container!))
                (-> (mount/with-args {:reagent-render {:target @container
                                                       :component-var #'mock-html}
                                      :web3 {:url "http://localhost:8549"}
                                      :web3-accounts {:polling-interval-ms 5000}})
                    (mount/start)))
   :after #(do (test-utils/unmount! @container)
               (mount/stop))})

(deftest tests
  (testing "test meta tags"
    (async done
           (.setTimeout js/window
                        (fn []
                          ;; TODO: test actions
                          (set! (.. js/window -zeContainer) @container)
                          (is (-> @container
                                  .-innerHTML
                                  hickory/parse
                                  hickory/as-hiccup
                                  first
                                  (nth 3)
                                  (nth 2)
                                  (nth 2)
                                  first
                                  (= :button)))
                          (done))
                        1000))))
