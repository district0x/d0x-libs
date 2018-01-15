# district-ui-web3-tx-costs

[![Build Status](https://travis-ci.org/district0x/district-ui-web3-tx-costs.svg?branch=master)](https://travis-ci.org/district0x/district-ui-web3-tx-costs)

Clojurescript [mount](https://github.com/tolitius/mount) + [re-frame](https://github.com/Day8/re-frame) module for a district UI.

This module automatically listens to [district-ui-web3-tx](https://github.com/district0x/district-ui-web3-tx) events and
when a transaction is processed, it calculates and saves transaction costs in chosen currencies.
It uses [district-ui-conversion-rates](https://github.com/district0x/district-ui-conversion-rates) to obtain conversion rates.  

## Installation
Add `[district0x/district-ui-web3-tx-costs "1.0.0"]` into your project.clj  
Include `[district.ui.web3-tx-costs]` in your CLJS file, where you use `mount/start`

**Warning:** district0x modules are still in early stages, therefore API can change in a future.

## district.ui.web3-tx-costs
This namespace contains web3-tx-costs [mount](https://github.com/tolitius/mount) module.

You can pass following args to initiate this module (optional): 
* `:currencies` Currencies you want to be calculating costs to. `:ETH` is always added, you don't need to pass that. 
* `:request-interval-ms` How often rates should be reloaded. Default as in [district-ui-conversion-rates](https://github.com/district0x/district-ui-conversion-rates).

```clojure
  (ns my-district.core
    (:require [mount.core :as mount]
              [district.ui.web3-tx-costs]))
              
  (-> (mount/with-args
        {:web3 {:url "https://mainnet.infura.io/"}
         :web3-tx-costs {:currencies [:USD :EUR]}})
    (mount/start))
```

## subscriptions
This module doesn't have its own subscriptions. Instead you can use [district-ui-web3-tx subscriptions](https://github.com/district0x/district-ui-web3-tx#districtuiweb3-txsubs)
and transactions will automatically contain entry `:tx-costs` with transaction costs.

```clojure
(ns my-district.core
    (:require [mount.core :as mount]
              [district.ui.web3-tx :as subs]))
  
  (defn home-page []
    (let [txs (subscribe [::subs/txs])]  
      (fn []
        [:div "Your transactions: "]
        (for [[tx-hash {:keys [:tx-costs]}] @txs]
          [:div 
            {:key tx-hash}
            "Transaction hash: " tx-hash
            "Transaction cost USD: " (:USD tx-costs)
            "Transaction cost ETH: " (:ETH tx-costs)]))))
```

## district.ui.web3-tx-costs.events
re-frame events provided by this module:

#### `::start [opts]`
Event fired at mount start.

#### `::add-currencies [currencies opts]`
Adds currencies, that costs will be converted to. Does effectively same thing as if you pass initial `:currencies` on
mount start. Use this from inside other modules, which build on top of this module.  

```clojure
(dispatch [::events/add-currencies [:USD :EUR]])
```

#### `::tx-loaded`
Event fired when [district-ui-web3-tx](https://github.com/district0x/district-ui-web3-tx) fires its event. Performs conversion. 

#### `::stop`
Cleanup event fired on mount stop.

## district.ui.web3-tx-costs.queries
DB queries provided by this module:  
*You should use them in your events, instead of trying to get this module's 
data directly with `get-in` into re-frame db.*

#### `currencies [db]`
Returns currencies the module is converting into. 

#### `set-currencies [db currencies]`
Sets currencies and returns and returns new re-frame db.

#### `add-currencies [db currencies]`
Adds currencies and returns and returns new re-frame db.

#### `dissoc-web3-tx-costs [db]`
Cleans up this module from re-frame db. 

## Dependency on other district UI modules
* [district-ui-web3-tx](https://github.com/district0x/district-ui-web3-tx)
* [district-ui-conversion-rates](https://github.com/district0x/district-ui-conversion-rates)

## Development
```bash
lein deps
# To run tests and rerun on changes
lein doo chrome tests
```