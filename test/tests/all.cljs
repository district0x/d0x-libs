(ns tests.all
  (:require
    [cljs-time.core :as t]
    [cljs.test :refer [deftest is testing run-tests async use-fixtures]]
    [cljsjs.web3]
    [district.validation :as valid]))

(deftest tests
  (is (false? (valid/js-date? (t/now))))
  (is (true? (valid/js-date? (js/Date.))))

  (is (true? (valid/cljs-time? (t/now))))
  (is (false? (valid/cljs-time? (js/Date.))))

  (is (true? (valid/length? "a" 1)))
  (is (false? (valid/length? "aa" 1)))
  (is (true? (valid/length? "aa" 1 3)))
  (is (false? (valid/length? "" 1 3)))
  (is (false? (valid/length? "aaaa" 1 3)))

  (is (true? (valid/email? "some@email.com")))
  (is (true? (valid/email? "" {:allow-empty? true})))

  (is (true? (valid/sha3? "0x10e176b8986f2cfd620a941952c6b3b245a5ae4b276552d6909a88c610eccd66")))
  (is (false? (valid/sha3? "0x48e69c07bc7b9b953b07c45dc8adbd78e12f10fa")))

  (is (true? (valid/not-neg? 1)))

  (is (true? (valid/not-nil? 1)))

  (is (true? (valid/http-url? "https://district0x.io")))
  (is (false? (valid/http-url? "https://")))
  (is (true? (valid/http-url? "" {:allow-empty? true})))


  (is (true? (valid/eth-value? "1.1")))
  (is (false? (valid/eth-value? "1a")))
  (is (true? (valid/eth-value? nil {:allow-empty? true})))
  (is (true? (valid/eth-value? "-1" {:allow-empty? true})))

  (is (true? (valid/not-neg-eth-value? "1,1")))
  (is (true? (valid/not-neg-eth-value? "" {:allow-empty? true})))
  (is (true? (valid/not-neg-eth-value? 0)))
  (is (false? (valid/not-neg-eth-value? "-1")))

  (is (true? (valid/pos-eth-value? "1,1")))
  (is (true? (valid/pos-eth-value? "" {:allow-empty? true})))
  (is (false? (valid/pos-eth-value? 0)))
  (is (false? (valid/pos-eth-value? "-1"))))




