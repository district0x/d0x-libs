# cljs-solidity-sha3

[![Build Status](https://travis-ci.org/district0x/cljs-solidity-sha3.svg?branch=master)](https://travis-ci.org/district0x/cljs-solidity-sha3)

Clojurescript implementation of [Solidity](https://solidity.readthedocs.io/en/develop/) sha3 function. [Web3.js](https://github.com/ethereum/web3.js/)
sha3 function works bit differently that one in Solidity, that's why this is needed.


## Installation
Add [![Clojars Project](https://img.shields.io/clojars/v/district0x/cljs-solidity-sha3.svg?include_prereleases)](https://clojars.org/district0x/cljs-solidity-sha3) into your project.clj  
Include `[cljs-solidity-sha3.core]` in your CLJS file

## Usage

```clojure
(ns my.namespace
  (:require
    [cljs-web3-next.core :as web3]
    [cljs-solidity-sha3.core :refer [solidity-sha3]]))

(def provider (web3/create-web3 "http://localhost:8549"))
    
(solidity-sha3 provider "0x7d10b16dd1f9e0df45976d402879fb496c114936" 6 "abc")
;; => "0x789357bc7419b62048fc1339ce448db0836603d3c0738082337b68e2b17d26a6"
```  

## Development

1. Setup local testnet

- spin up a testnet instance in a separate shell
    - `npx truffle develop`

2. Run test suite:
- Browser
    - `npx shadow-cljs watch test-browser`
    - open https://d0x-vm:6502
    - tests refresh automatically on code change
- CI (Headless Chrome, Karma)
    - `npx shadow-cljs compile test-ci`
    - ``CHROME_BIN=`which chromium-browser` npx karma start karma.conf.js --single-run``

3. Build
- on merging pull request to master on GitHub, CI builds & publishes new version automatically
- update version in `build.clj`
- to build: `clj -T:build jar`
- to release: `clj -T:build deploy` (needs `CLOJARS_USERNAME` and `CLOJARS_PASSWORD` env vars to be set)
