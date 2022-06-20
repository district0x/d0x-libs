(ns tests.all
  (:require [cljs.spec.alpha :as s]
            [cljs.test :refer [deftest is testing run-tests async use-fixtures]]
            [day8.re-frame.test :refer [run-test-async run-test-sync wait-for]]
            ["react-dom" :as ReactDOM]
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
  [:div#app @(re-frame/subscribe [:mock/content])])

; Copied from https://github.com/bensu/cljs-react-test/blob/1322372ff80119d9cf79867073263be9c1086cc5/src/cljs_react_test/utils.cljs
;   - because these were the only ones used and the library isn't maintained (new React has breaking changes)
;   - that way we can continue using these tests with minimum changes
(defn unmount!
  "Unmounts the React Component at a node"
  [n]
  (.unmountComponentAtNode ReactDOM n))

(defn- container-div []
  (let [id (str "container-" (gensym))
        node (.createElement js/document "div")]
    (set! (.-id node) id)
    [node id]))

(defn insert-container! [container]
  (.appendChild (.-body js/document) container))

(defn new-container! []
  (let [[n s] (container-div)]
    (insert-container! n)
    (.getElementById js/document s)))

; ---- END of copy

(use-fixtures :each
  {:before #(do
              (reset! container (new-container!))
              (-> (mount/with-args {:reagent-render {:target @container
                                                     :component-var #'mock-html}
                                    :mock {:content "MOCK"}})
                  (mount/start)))
   :after #(do
             (unmount! @container)
             (mount/stop))})

(deftest rendering-test
  (testing "render-fx dispatches after sync events"
    (async done
           (.setTimeout js/window
                        (fn []
                          (is (re-find #"MOCK" (.-innerHTML @container)))
                          (done))
                        1000))))
