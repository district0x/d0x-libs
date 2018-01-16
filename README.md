# cljs-solidity-sha3

[![Build Status](https://travis-ci.org/district0x/cljs-solidity-sha3.svg?branch=master)](https://travis-ci.org/district0x/cljs-solidity-sha3)

Clojurescript implementation of [Solidity](https://solidity.readthedocs.io/en/develop/) sha3 function. [Web3.js](https://github.com/ethereum/web3.js/)
sha3 function works bit differently that one in Solidity, that's why this is needed. This library is not 
including [web3.js](https://github.com/ethereum/web3.js/) on purpose.


## Installation
Add `[district0x/cljs-solidity-sha3 "1.0.0"]` into your project.clj  
Include `[cljs-solidity-sha3.core]` in your CLJS file

```clojure
(ns my.namespace
  (:require
    [cljsjs.web3]
    [cljs-solidity-sha3.core :refer [solidity-sha3]]))
    
(solidity-sha3 "0x7d10b16dd1f9e0df45976d402879fb496c114936" 6 "abc")
;; => "0x789357bc7419b62048fc1339ce448db0836603d3c0738082337b68e2b17d26a6"
```  

## Development
```bash
lein deps
# To run tests and rerun on changes
lein doo chrome tests
```