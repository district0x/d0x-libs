(ns tests.all
  (:require
    [district.sendgrid :as sg]
    [cljs.test :refer-macros [deftest use-fixtures is testing async]]))

(deftest test-smart-contracts
  (async done
         (is (fn? sg/send-email))
         (done)))

