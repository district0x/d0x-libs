(ns tests.all
  (:require
    [cljs-time.coerce :as time-coerce]
    [cljs-time.core :as t]
    [cljs-web3.core :as web3]
    [cljs.test :refer [deftest is testing run-tests async use-fixtures]]
    [cljsjs.web3]
    [district.web3-utils :as web3-utils]
    [bignumber.core :as bn]))

(deftest tests
  (is (= "1" (web3-utils/wei->eth 1000000000000000000)))
  (is (bn/= (web3/to-big-number 1) (web3-utils/wei->eth (web3/to-big-number 1000000000000000000))))
  (is (= nil (web3-utils/wei->eth "abc")))

  (is (= 1 (web3-utils/wei->eth-number 1000000000000000000)))

  (is (= "1100000000000000000" (web3-utils/eth->wei 1.1)))
  (is (= "1100000000000000000" (web3-utils/eth->wei "1,1")))
  (is (= 1000000000000000000 (web3-utils/eth->wei-number 1)))
  (is (= nil (web3-utils/eth->wei-number "abc")))

  (is (true? (web3-utils/zero-address? "0x")))
  (is (true? (web3-utils/zero-address? "0x0000000000000000000000000000000000000000")))

  (is (true? (web3-utils/empty-address? "0x")))
  (is (true? (web3-utils/empty-address? "")))
  (is (true? (web3-utils/empty-address? nil)))

  (is (= (web3-utils/remove-0x "0x0000000000000000000000000000000000000000")
         "0000000000000000000000000000000000000000"))

  (let [date (t/date-time 2017 3 4 10 10)]
    (is (t/equal? date
                  (web3-utils/web3-time->date-time (web3/to-big-number (time-coerce/to-epoch date))))))

  (let [date (t/local-date-time 2017 3 4 10 10)]
    (is (t/equal? date
                  (web3-utils/web3-time->local-date-time (time-coerce/to-epoch date)))))

  (is (= "0x0000000000000000000000000000000000000123" (web3-utils/prepend-address-zeros "0x123")))

  (is (= (web3-utils/bytes32->str "0x636f6e7374727563746564000000000000000000000000000000000000000000")
         "constructed")))

