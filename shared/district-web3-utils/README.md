# district-web3-utils

[![CircleCI](https://circleci.com/gh/district0x/district-web3-utils/tree/master.svg?style=svg)](https://circleci.com/gh/district0x/district-web3-utils/tree/master)


Set of extra functions helpful for working with [web3.js](https://github.com/ethereum/web3.js/). 


## Installation
Add [![Clojars Project](https://img.shields.io/clojars/v/io.github.district0x/district-web3-utils.svg?include_prereleases)](https://clojars.org/io.github.district0x/district-web3-utils) into your project.clj  
Include `[district.web3-utils]` in your CLJS file  

## API Overview
- [district.web3-utils](#districtweb3-utils)
  - [wei->eth](#wei-eth)
  - [wei->eth-number](#wei-eth-number)
  - [eth->wei](#eth-wei)
  - [eth->wei-number](#eth-wei-number)
  - [zero-address?](#zero-address?)
  - [empty-address?](#empty-address?)
  - [remove-0x](#remove-0x)
  - [web3-time->date-time](#web3-time-date-time)
  - [web3-time->local-date-time](#web3-time-local-date-time)
  - [prepend-address-zeros](#prepend-address-zeros)
  - [bytes32->str](#bytes32-str)
  - [uint->address](#uint-address)
  

## district.web3-utils

#### <a name="wei-eth">`wei->eth [x]`
Safely Converts wei into ether.
```clojure
(web3-utils/wei->eth "1000000000000000000")
;; => "1"

(web3-utils/wei->eth (web3/to-big-number 1000000000000000000))
;; => #object[BigNumber 1]

(web3-utils/wei->eth "abc")
;; => nil
```

#### <a name="wei-eth-number">`wei->eth-number [x]`
Safely Converts wei into ether and coerces into number. 
```clojure
(web3-utils/wei->eth-number "1000000000000000000")
;; => 1

(web3-utils/wei->eth-number (web3/to-big-number 1000000000000000000))
;; => 1
```

#### <a name="eth-wei">`eth->wei [x]`
Safely Converts ether into wei.
```clojure
(web3-utils/eth->wei "1.1")
;; => "1100000000000000000"

;; handles comma as fraction decimals separator as well
(web3-utils/eth->wei "1,1")
;; => "1100000000000000000"
```

#### <a name="eth-wei-number">`eth->wei-number [x]`
Safely Converts ether into wei and coerces into number. 
```clojure
(web3-utils/eth->wei-number "1.1")
;; => 1100000000000000000
```

#### <a name="zero-address?">`zero-address? [address]`
True if address is zero address
```clojure
(web3-utils/zero-address? "0x")
;; => true

(web3-utils/zero-address? "0x0000000000000000000000000000000000000000")
;; => true
```

#### <a name="empty-address?">`empty-address? [address]`
True if address is zero address, empty string or nil
```clojure
(web3-utils/empty-address? "0x")
;; => true

(web3-utils/empty-address? nil)
;; => true
```

#### <a name="remove-0x">`remove-0x [address]`
Removes initial "0x" from an address
```clojure
(web3-utils/remove-0x "0x0000000000000000000000000000000000000000")
;; => "0000000000000000000000000000000000000000"
```

#### <a name="web3-time-date-time">`web3-time->date-time [x]`
Converts time returned by smart-contracts (usually as BigNumber UNIX epoch) into cljs-time. 
```clojure
(web3-utils/web3-time->date-time (web3/to-big-number 1516223428))
;; => #object[Object 20180117T211028]

(web3-utils/web3-time->date-time 1516223428)
;; => #object[Object 20180117T211028]

(web3-utils/web3-time->date-time (web3/to-big-number 0))
;; => nil

```

#### <a name="web3-time-local-date-time">`web3-time->local-date-time [x]`
Converts time returned by smart-contracts (usually as BigNumber UNIX epoch) into local cljs-time. 
```clojure
(web3-utils/web3-time->local-date-time (web3/to-big-number 1516223428))
;; => #object[Object 20180117T211028]

(web3-utils/web3-time->local-date-time 1516223428)
;; => #object[Object 20180117T211028]
```

#### <a name="prepend-address-zeros">`prepend-address-zeros [address]`
If given address is shorter than it should be, it prepends missing places with zeros.
```clojure
(web3-utils/prepend-address-zeros "0x123")
;; => "0x0000000000000000000000000000000000000123"
```

#### <a name="bytes32-str">`bytes32->str [x]`
Converts web3 bytes32 encoded string into normal string. 
```clojure
(web3-utils/bytes32->str "0x636f6e7374727563746564000000000000000000000000000000000000000000")
;; => "constructed"
```

#### <a name="uint-address">`uint->address [x]`
Converts Solidity's uint into web3 address 
```clojure
(web3-utils/uint->address (web3/to-big-number 1234))
;; => "0x00000000000000000000000000000000000004d2"
```

## Development

1. Run test suite:
- Browser
  - `npx shadow-cljs watch test-browser`
  - open https://d0x-vm:6502
  - tests refresh automatically on code change
- CI (Headless Chrome, Karma)
  - `npx shadow-cljs compile test-ci`
  - ``CHROME_BIN=`which chromium-browser` npx karma start karma.conf.js --single-run``

2. Build
- on merging pull request to master on GitHub, CI builds & publishes new version automatically
- update version in `build.clj`
- to build: `clj -T:build jar`
- to release: `clj -T:build deploy` (needs `CLOJARS_USERNAME` and `CLOJARS_PASSWORD` env vars to be set)