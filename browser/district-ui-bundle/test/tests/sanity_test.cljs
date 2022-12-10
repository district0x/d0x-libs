(ns tests.sanity-test
  (:require
    [cljs.test :refer [deftest is testing run-tests async use-fixtures]]))

(deftest tests
  (is (= true true)))
