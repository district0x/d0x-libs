(ns tests.all
  (:require
    [cljsjs.web3]
    [cljs-solidity-sha3.core :refer [solidity-sha3]]
    [cljs.test :refer [deftest is testing run-tests async use-fixtures]]))


(deftest tests
  (is (= "0xf652222313e28459528d920b65115c16c04f3efc82aaedc97be59f3f377c0d3f"
         (solidity-sha3 6)))

  (is (= "0x34ee2a785aa8c43ab6ddf4bd8cd55e2cf7ee009305e966472ee22a637a2bb71f"
         (solidity-sha3 "0x7d10b16dd1f9e0df45976d402879fb496c114936")))

  (is (= "0x4e03657aea45a94fc7d47ba826c8d667c0d1e6e33a64a036ec44f58fa12d6c45"
         (solidity-sha3 "abc")))

  (is (= "0x789357bc7419b62048fc1339ce448db0836603d3c0738082337b68e2b17d26a6"
         (solidity-sha3 "0x7d10b16dd1f9e0df45976d402879fb496c114936" 6 "abc"))))