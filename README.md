# district-validation

[![CircleCI](https://circleci.com/gh/district0x/district-validation/tree/master.svg?style=svg)](https://circleci.com/gh/district0x/district-validation/tree/master)


Set of functions helpful for input validation. Validation functions always return boolean.  


## Installation
Add [![Clojars Project](https://img.shields.io/clojars/v/io.github.district0x/district-validation.svg?include_prereleases)](https://clojars.org/io.github.district0x/district-validation) into your project.clj  
Include `[district.validation]` in your CLJS file  

## API Overview
- [district.validation](#districtvalidation)
  - [js-date?](#js-date?)
  - [cljs-time?](#cljs-time?)
  - [length?](#length?)
  - [email?](#email?)
  - [web3-address?](#web3-address?)
  - [sha3?](#sha3?)
  - [not-neg?](#not-neg?)
  - [not-nil?](#not-nil?)
  - [http-url?](#http-url?)
  - [eth-value?](#eth-value?)
  - [not-neg-eth-value?](#not-neg-eth-value?)
  - [pos-eth-value?](#pos-eth-value?)
  

## district.validation
#### <a name="js-date?">`js-date? [x]`
True if passed instance of `js/Date`.
```clojure
(valid/js-date? (js/Date.))
;; => true
```

#### <a name="cljs-time?">`cljs-time? [x]`
True if passed instance of [cljs-time](https://github.com/andrewmcveigh/cljs-time)
```clojure
(valid/cljs-time? (t/now))
;; => true
```

#### <a name="length?">`length? [x max-length]`
#### `length? [x min-length max-length]`
True if given string is in a range of lengths.
```clojure
(valid/length? "a" 1)
;; => true
(valid/length? "aa" 1)
;; => false
(valid/length? "aaaa" 1 3)
;; => false
```

#### <a name="email?">`email? [x & [{:keys [:allow-empty?]}]]`
True for valid email. 
```clojure
(valid/email? "some@email.com")
;; => true
(valid/email? "" {:allow-empty? true})
;; => true
```

#### <a name="web3-address?">`web3-address? [x]`
True if given valid web3 address.
```clojure
(valid/web3-address? "0x48e69c07bc7b9b953b07c45dc8adbd78e12f10fa")
;; => true
```

#### <a name="sha3?">`sha3? [x]`
True if string is a sha3 hash.
```clojure
(valid/sha3? "0x10e176b8986f2cfd620a941952c6b3b245a5ae4b276552d6909a88c610eccd66")
;; => true
```

#### <a name="not-neg?">`not-neg? [x]`
True if number is not negative number.
```clojure
(valid/not-neg? 1)
;; => true
```

#### <a name="not-nil?">`not-nil? [x]`
True if parameter is not nil.
```clojure
(valid/not-nil? 1)
;; => true
```

#### <a name="http-url?">`http-url? [x & [{:keys [:allow-empty?]}]]`
True if given valid http url
```clojure
(valid/http-url? "https://district0x.io")
;; => true
(valid/http-url? "" {:allow-empty? true})
;; => true
```

#### <a name="eth-value?">`eth-value? [x & [{:keys [:allow-empty?]}]]`
True if given valid ether (as a unit, not currency) value, that is convertible to wei. 
```clojure
(valid/eth-value? "1.1")
;; => true
(valid/eth-value? "1a")
;; => false
(valid/eth-value? nil {:allow-empty? true})
;; => true
```

#### <a name="not-neg-eth-value?">`not-neg-eth-value? [x & [{:keys [:allow-empty?]}]]`
True if given valid non negative ether (as a unit, not currency) value, that is convertible to wei.
```clojure
(valid/not-neg-eth-value? "1,1")
;; => true
(valid/not-neg-eth-value? 0)
;; => true
(valid/not-neg-eth-value? "-1")
;; => false
```

#### <a name="pos-eth-value?">`pos-eth-value? [x & [{:keys [:allow-empty?]}]]`
True if given valid positive ether (as a unit, not currency) value, that is convertible to wei.
```clojure
(valid/pos-eth-value? "1,1")
;; => true
(valid/pos-eth-value? 0)
;; => false
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
