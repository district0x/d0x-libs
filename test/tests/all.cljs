(ns tests.all
  (:require
    [cljs.test :refer [deftest is testing run-tests async use-fixtures]]
    [cljsjs.web3]
    [district.parsers :as parsers]))

(deftest tests
  (is (= 1 (parsers/parse-int "1")))
  (is (nil? (parsers/parse-int "abc")))
  (is (= 1 (parsers/parse-int "a" 1)))
  (is (nil? (parsers/parse-int [1])))

  (is (= 1 (parsers/parse-float "1")))
  (is (= 1.1 (parsers/parse-float "1.1")))
  (is (= 1.1 (parsers/parse-float "1,1")))
  (is (= 1 (parsers/parse-float "a" 1)))
  (is (nil? (parsers/parse-float [1])))

  (is (= :a (parsers/parse-keyword ":a")))
  (is (= :a/b (parsers/parse-keyword ":a/b")))
  (is (= :a (parsers/parse-keyword "a")))
  (is (= :a/b (parsers/parse-keyword "a/b")))
  (is (= :b (parsers/parse-keyword true :b)))
  (is (nil? (parsers/parse-keyword [1])))

  (is (true? (parsers/parse-boolean "true")))
  (is (false? (parsers/parse-boolean "FALSE")))
  (is (true? (parsers/parse-boolean "a" true)))
  (is (nil? (parsers/parse-boolean [1])))

  (is (nil? (parsers/parse-non-empty-str "")))
  (is (nil? (parsers/parse-non-empty-str nil)))
  (is (= "a" (parsers/parse-non-empty-str "a")))
  (is (= "1" (parsers/parse-non-empty-str 1)))
  (is (= "a" (parsers/parse-non-empty-str [1] "a")))

  (is (= (parsers/parse-web3-address "0x0B73EEc3b1C5A2c555799B7FfC500082606DFf15")
         "0x0b73eec3b1c5a2c555799b7ffc500082606dff15"))
  (is (nil? (parsers/parse-web3-address "0x0")))
  (is (= :a (parsers/parse-web3-address "0x0" :a)))

  (is (= [1 8] (parsers/parse-int-seq ["1.1" "8"])))
  (is (= [1 10] (parsers/parse-int-seq ["1,1" "a"] 10)))
  (is (= [1] (parsers/parse-int-seq "1")))

  (is (= [1.1 1.1] (parsers/parse-float-seq ["1.1" "1,1"])))
  (is (= [1.1 8] (parsers/parse-float-seq ["1.1" "a"] 8)))

  (is (= [:a :a] (parsers/parse-keyword-seq ["a" ":a"])))
  (is (= [:a :b] (parsers/parse-keyword-seq ["a" (js/Date.)] :b)))

  (is (= [true false] (parsers/parse-boolean-seq ["true" "false"])))
  (is (= [true true] (parsers/parse-boolean-seq ["true" "a"] true)))

  (is (= ["0x0b73eec3b1c5a2c555799b7ffc500082606dff15" "0x0"]
         (parsers/parse-web3-address-seq ["0x0B73EEc3b1C5A2c555799B7FfC500082606DFf15" "a"] "0x0")))

  (is (= [nil "a"] (parsers/parse-non-empty-str-seq ["" "a"])))
  (is (= ["a"] (parsers/parse-non-empty-str-seq "" "a"))))


