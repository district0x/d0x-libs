(ns tests.all
  (:require
    [cljs.test :refer [deftest is testing run-tests async use-fixtures]]
    [day8.re-frame.test :refer [run-test-async run-test-sync wait-for]]
    [district.ui.router.events :as events]
    [district.ui.router.subs :as subs]
    [district.ui.router.utils :as utils]
    [district.ui.router.effects :as effects]
    [district.ui.router]
    [mount.core :as mount]
    [re-frame.core :refer [reg-event-fx dispatch-sync subscribe reg-cofx reg-sub dispatch trim-v]]))

(use-fixtures
  :each
  {:after
   (fn []
     (mount/stop))})

(deftest tests
  (run-test-async
    (let [active-page-name (subscribe [::subs/active-page-name])
          active-page-params (subscribe [::subs/active-page-params])
          active-page-query (subscribe [::subs/active-page-query])
          html5? (subscribe [::subs/html5?])]
      (-> (mount/with-args {:router {:routes [["/a" :route/a]
                                              ["/b/:b" :route/b]]
                                     :default-route :route/a}})
        (mount/start))

      (is (not @html5?))
      (is (= "/a" (utils/resolve :route/a)))
      (is (= "/b/abc?c=xyz" (utils/resolve :route/b {:b "abc"} {:c "xyz"})))
      (is (= [:route/b {:b "abc"} {:c "xyz"}] (utils/match "/b/abc?c=xyz")))

      (wait-for [::events/active-page-changed]
        (is (= @active-page-name :route/a))
        (dispatch [::events/navigate :route/b {:b "abc"} {:c "xyz"}])
        (wait-for [::events/active-page-changed]
          (is (= @active-page-name :route/b))
          (is (= @active-page-params {:b "abc"}))
          (is (= @active-page-query {:c "xyz"}))

          (dispatch [::events/replace :route/a])
          (wait-for [::events/active-page-changed]
            (is (= @active-page-name :route/a))))))))


(deftest tests-html5
  (run-test-async
    (let [active-page-name (subscribe [::subs/active-page-name])
          active-page-params (subscribe [::subs/active-page-params])
          active-page-query (subscribe [::subs/active-page-query])
          html5? (subscribe [::subs/html5?])]
      (-> (mount/with-args {:router {:routes [["/a" :route/a]
                                              ["/b/:b" :route/b]]
                                     :default-route :route/a
                                     :html5-hosts "localhost"}})
        (mount/start))


      (is (true? @html5?))
      (is (= "/a" (utils/resolve :route/a)))
      (is (= "/b/abc?c=xyz" (utils/resolve :route/b {:b "abc"} {:c "xyz"})))
      (is (= [:route/b {:b "abc"} {:c "xyz"}] (utils/match "/b/abc?c=xyz")))

      (wait-for [::events/active-page-changed]
        (is (= @active-page-name :route/a))
        (dispatch [::events/navigate :route/b {:b "abc"} {:c "xyz"}])
        (wait-for [::events/active-page-changed]
          (is (= @active-page-name :route/b))
          (is (= @active-page-params {:b "abc"}))
          (is (= @active-page-query {:c "xyz"}))

          (dispatch [::events/replace :route/a])
          (wait-for [::events/active-page-changed]
            (is (= @active-page-name :route/a))))))))


(reg-event-fx
  ::event1
  (constantly nil))


(reg-event-fx
  ::event2
  (constantly nil))


(reg-event-fx
  ::event3
  (constantly nil))


(deftest tests-effects
  (run-test-async
    (-> (mount/with-args {:router {:routes [["/a" :route/a]
                                            ["/b/:b" :route/b]]
                                   :default-route :route/a
                                   :html5-hosts ["localhost"]}})
      (mount/start))

    (testing "Watching based on route name"
      (dispatch [::events/watch-active-page [{:id :watcher1
                                              :name :route/b
                                              :dispatch [::event1]}]])

      (wait-for [::events/active-page-changed]
        (dispatch [::events/navigate :route/b {:b "abc"}])
        (wait-for [::event1]
          (testing "Watching based on params"
            (dispatch [::events/watch-active-page [{:id :watcher2
                                                    :params {:b "efg"}
                                                    :dispatch [::event2]}]])
            (dispatch [::events/navigate :route/b {:b "efg"}])
            (wait-for [::event2]
              (testing "Watching based on query"
                (dispatch [::events/watch-active-page [{:id :watcher3
                                                        :query {:c "xxx"}
                                                        :dispatch-n [[::event3]]}]])
                (dispatch [::events/navigate :route/b {:b "efg"} {:c "xxx"}])
                (wait-for [::event3]
                  (testing "Watching based on multiple routes"
                    (dispatch [::events/watch-active-page [{:id :watcher4
                                                            :name [:route/a :route/b]
                                                            :dispatch [::event1]}]])
                    (dispatch [::events/navigate :route/a])
                    (wait-for [::event1]
                      (dispatch [::events/unwatch-active-page [{:id :watcher1}
                                                               {:id :watcher2}
                                                               {:id :watcher3}
                                                               {:id :watcher4}]]))))))))))))