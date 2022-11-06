(ns tests.all
  (:require-macros [cljs.test :refer [deftest testing is async]])
  (:require [cljs.test :as t]))

(deftest add-test []
  (async done
         (is (= 1 1))
         (done)))
