# district-ui-web3-account-balances

[![Build Status](https://travis-ci.org/district0x/district-ui-web3-account-balances.svg?branch=master)](https://travis-ci.org/district0x/district-ui-web3-account-balances)

Clojurescript [mount](https://github.com/tolitius/mount) + [re-frame](https://github.com/Day8/re-frame) module for a district UI, 
that combines together [district-ui-web3-accounts](https://github.com/district0x/district-ui-web3-accounts) and 
[district-ui-web3-balances](https://github.com/district0x/district-ui-web3-balances) to provide balances of user's accounts.  

## Installation
Add `[district0x/district-ui-web3-account-balances "1.0.0"]` into your project.clj  
Include `[district.ui.web3-account-balances]` in your CLJS file, where you use `mount/start`

**Warning:** district0x modules are still in early stages, therefore API can change in a future.

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

#### `::active-account-balance [& [contract]]`
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

## district.ui.web3-account-balances.events
re-frame events provided by this module:

#### `::start [opts]`
Event fired at mount start.

#### `::load-account-balances [opts]`
Loads balances of user's accounts

#### `::stop`
Cleanup event fired on mount stop.

## district.ui.web3-account-balances.queries
DB queries provided by this module:  
*You should use them in your events, instead of trying to get this module's 
data directly with `get-in` into re-frame db.*

#### `active-account-balance [db & [contract]]`
Works the same way as sub `::active-account-balance`

#### `account-balances [db & [contract]]`
Works the same way as sub `::account-balances`

## Dependency on other district UI modules
* [district-ui-web3-accounts](https://github.com/district0x/district-ui-web3-accounts)
* [district-ui-web3-balances](https://github.com/district0x/district-ui-web3-balances)

## Development
```bash
lein deps
# Start ganache blockchain
ganache-cli -p 8549
# To run tests and rerun on changes
lein doo chrome tests
```