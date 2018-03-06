(ns tests.all
  (:require [cljs.spec.alpha :as s]
            [cljs.test :refer [deftest is testing run-tests async use-fixtures]]
            [cljs-react-test.simulate :as simulate]
            [cljs-react-test.utils :as test-utils]
            [day8.re-frame.test :refer [run-test-async run-test-sync wait-for]]
            [district.ui.reagent-render]
            [district.ui.reagent-render.events :as events]
            [mount.core :as mount :refer [defstate]]
            [re-frame.core :as re-frame]))

(s/check-asserts true)

(def container (atom nil))

(re-frame/reg-event-db
 :mock/start
 (fn [db [_ content]]
   (assoc-in db [:mock] content)))

(re-frame/reg-event-db
 :mock/stop
 (fn [db _]
   (dissoc db :mock)))

(re-frame/reg-sub
 :mock/content
 (fn [db]
   (get-in db [:mock :content])))

(defstate mock-component
  :start (re-frame/dispatch-sync [:mock/start (:mock (mount/args))])
  :stop (re-frame/dispatch-sync [:mock/stop]))

(defn mock-html []
  (fn []
    (let [content @(re-frame/subscribe [:mock/content])]
      [:div#app content])))

(use-fixtures :each
  {:before #(do
              (reset! container (test-utils/new-container!))
              (-> (mount/with-args {:reagent-render {:target @container
                                                     :component-ref #'mock-html}
                                    :mock {:content "MOCK"}})
                  (mount/start)))
   :after #(do
             (test-utils/unmount! @container)
             (mount/stop))})

(deftest rendering-test
  (testing "render-fx dispatches after sync events"
    (async done
           (.setTimeout js/window
                        (fn []
                          (is (re-find #"MOCK" (.-innerHTML @container)))
                          (done))
                        1000))))
