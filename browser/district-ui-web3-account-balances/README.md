# district-ui-web3-account-balances

[![Build Status](https://travis-ci.org/district0x/district-ui-web3-account-balances.svg?branch=master)](https://travis-ci.org/district0x/district-ui-web3-account-balances)

Clojurescript [re-mount](https://github.com/district0x/d0x-INFRA/blob/master/re-mount.md) module, 
that combines together [district-ui-web3-accounts](https://github.com/district0x/district-ui-web3-accounts) and 
[district-ui-web3-balances](https://github.com/district0x/district-ui-web3-balances) to provide balances of user's accounts.  

## Installation
Add [![Clojars Project](https://img.shields.io/clojars/v/district0x/district-ui-web3-account-balances.svg)](https://clojars.org/district0x/district-ui-web3-account-balances) into your project.clj  
Include `[district.ui.web3-account-balances]` in your CLJS file, where you use `mount/start`

## API Overview

**Warning:** district0x modules are still in early stages, therefore API can change in a future.

- [district.ui.web3-account-balances](#districtuiweb3-account-balances)
- [district.ui.web3-account-balances.subs](#districtuiweb3-account-balancessubs)
  - [::active-account-balance](#active-account-balance-sub)
  - [::accounts-balances](#accounts-balances-sub)
- [district.ui.web3-account-balances.events](#districtuiweb3-account-balancesevents)
  - [::load-account-balances](#load-account-balances)
- [district.ui.web3-account-balances.queries](#districtuiweb3-account-balancesqueries)
  - [active-account-balance](#active-account-balance)
  - [accounts-balances](#accounts-balances)

## district.ui.web3-account-balances
This namespace contains web3-account-balances [mount](https://github.com/tolitius/mount) module.

You can pass following args to initiate this module: 
* `:for-contracts` (optional) Collection of contract keys from `:contracts` map passed into `district-ui-web3-balances`,
to load balances of ERC20 tokens. 
If you don't use this param, Ether balances will be loaded by default. However, if you use `:for-contract`, then you must
include `:ETH` to load Ether balances.   
Item in `:for-contracts` collection can be either token contract key, address or an instance. Same way as in  
[district-ui-web3-balances](https://github.com/district0x/district-ui-web3-balances). 
* `:disable-loading-at-start?` (optional) Pass true if you don't want to load balances at mount start
* `:disable-watching?` (optional) Pass true if you don't want to watch balance changes


```clojure
  (ns my-district.core
    (:require [mount.core :as mount]
              [district.ui.web3-account-balances]))
              
  ;; This will load and watch ETH, DNT balances of user's accounts at mount start
  (-> (mount/with-args
        {:web3 {:url "https://mainnet.infura.io/"}
         :web3-balances {:contracts {:DNT {:address "0x0abdace70d3790235af448c88547603b945604ea"}}}
         :web3-account-balances {:for-contracts [:ETH :DNT]}})
    (mount/start))
```

## district.ui.web3-account-balances.subs
re-frame subscriptions provided by this module:

#### <a name="active-account-balance-sub">`::active-account-balance [& [contract]]`
Retunrns balance of an active account. Optionally, you can pass contract to get balance of an ERC20 token.
See [district-ui-web3-balances balance subscription](https://github.com/district0x/district-ui-web3-balances#balance-address--contract).

```clojure
(ns my-district.core
    (:require [mount.core :as mount]
              [district.ui.web3-account-balances :as subs]
              [cljs-web3.core :as web3]))
  
  (defn home-page []
    (let [balance-eth (subscribe [::subs/active-account-balance])
          balance-dnt (subscribe [::subs/active-account-balance :DNT])]  
      (fn []
        [:div "Active account has following balances:"]
        [:div (web3/from-wei @balance-eth :ether) " ETH"]
        [:div (web3/from-wei @balance-dnt :ether) " DNT"])))
```

#### <a name="accounts-balances-sub">`::accounts-balances [& [contract]]`
Returns balances of all accounts. 

## district.ui.web3-account-balances.events
re-frame events provided by this module:

#### <a name="load-account-balances">`::load-account-balances [opts]`
Loads balances of user's accounts

## district.ui.web3-account-balances.queries
DB queries provided by this module:  
*You should use them in your events, instead of trying to get this module's 
data directly with `get-in` into re-frame db.*

#### <a name="active-account-balance">`active-account-balance [db & [contract]]`
Works the same way as sub `::active-account-balance`

#### <a name="accounts-balances">`accounts-balances [db & [contract]]`
Works the same way as sub `::accounts-balances`

## Dependency on other district UI modules
* [district-ui-web3-accounts](https://github.com/district0x/district-ui-web3-accounts)
* [district-ui-web3-balances](https://github.com/district0x/district-ui-web3-balances)

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
