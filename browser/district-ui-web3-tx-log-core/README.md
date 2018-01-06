# district-ui-web3-tx-log-core

[![Build Status](https://travis-ci.org/district0x/district-ui-web3-tx-log-core.svg?branch=master)](https://travis-ci.org/district0x/district-ui-web3-tx-log-core)

Clojurescript [mount](https://github.com/tolitius/mount) + [re-frame](https://github.com/Day8/re-frame) module for a district UI,
that provides core logic for transaction log components. This module does not provide [reagent](https://github.com/reagent-project/reagent) component for a transaction log, only
logic to build the component upon. This way many different reagent components can be build on top of this module.

This module automatically listens to [district-ui-web3-tx](https://github.com/district0x/district-ui-web3-tx) events to
build up chronological list of transactions. It means you don't need to add/remove transactions manually to this module
unless doing something very specific. It also uses localstorage to store data between sessions, but only if [district-ui-web3-tx](https://github.com/district0x/district-ui-web3-tx)
uses it as well.  

## Installation
Add `[district0x/district-ui-web3-tx-log-core "1.0.0"]` into your project.clj  
Include `[district.ui.web3-tx-log-core]` in your CLJS file, where you use `mount/start`

**Warning:** district0x modules are still in early stages, therefore API can change in a future.

## district.ui.web3-tx-log-core
This namespace contains web3-tx-log-core [mount](https://github.com/tolitius/mount) module.

This module does not have any configuration options.

```clojure
  (ns my-district.core
    (:require [mount.core :as mount]
              [district.ui.web3-tx-log-core]))
              
  (-> (mount/with-args
        {:web3 {:url "https://mainnet.infura.io/"}})
    (mount/start))
```

## district.ui.web3-tx-log-core.subs
re-frame subscriptions provided by this module:

#### `::txs [filter-opts]`
Returns list of transactions as stored by [district-ui-web3-tx](https://github.com/district0x/district-ui-web3-tx), ordered chronologically with newest being first. Optionally, you can pass filter opts same
way as you'd pass to [district-ui-web3-tx ::txs](https://github.com/district0x/district-ui-web3-tx#txs-filter-opts)

```clojure
(ns my-district.core
    (:require [mount.core :as mount]
              [district.ui.web3-tx-log-core :as subs]))
  
  (defn transaction-log []
    (let [txs (subscribe [::subs/txs])]  
      (fn []
        [:div "Transaction Log: "]
        (for [{:keys [:transaction-hash :created-on :gas-used]} @txs]
          [:div 
            {:key transaction-hash}
            transaction-hash " - " created-on " - " gas-used]))))
```

#### `::tx-hashes`
Returns only list of transaction hashes ordered chronologically with newest transactions being first.

## district.ui.web3-tx-log-core.events
re-frame events provided by this module:

#### `::start [opts]`
Event fired at mount start.

#### `::add-tx-hash [tx-hash]`
Adds transaction hash into transaction log list.

#### `::remove-tx-hash [tx-hash]`
Removes transaction hash from transaction log list.  

#### `::stop`
Cleanup event fired on mount stop.

## district.ui.web3-tx-log-core.queries
DB queries provided by this module:  
*You should use them in your events, instead of trying to get this module's 
data directly with `get-in` into re-frame db.*

#### `txs [db]`
Works the same way as sub `::txs`

#### `tx-hashes [db]`
Works the same way as sub `::tx-hashes`

#### `add-tx-hash [db tx-hash]`
Adds transaction hash into transaction log list and returns new re-frame db.

#### `remove-tx-hash [db tx-hash]`
Removes transaction hash from transaction log list and returns new re-frame db.

#### `assoc-tx-hashes [db tx-hashes]`
Associates list of tx-hashes into this module's state.

#### `dissoc-web3-tx-log-core [db]`
Cleans up this module from re-frame db. 

## Dependency on other district UI modules
* [district-ui-web3-tx](https://github.com/district0x/district-ui-web3-tx)

## Development
```bash
lein deps
# To run tests and rerun on changes
lein doo chrome tests
```