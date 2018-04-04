# district-ui-web3-tx-log-core

[![Build Status](https://travis-ci.org/district0x/district-ui-web3-tx-log-core.svg?branch=master)](https://travis-ci.org/district0x/district-ui-web3-tx-log-core)

Clojurescript [re-mount](https://github.com/district0x/d0x-INFRA/blob/master/re-mount.md) module,
that provides core logic for transaction log components. This module does not provide [reagent](https://github.com/reagent-project/reagent) UI component for a transaction log, only
logic to build the component upon. This way many different reagent components can be build on top of this module.

This module automatically listens to [district-ui-web3-tx](https://github.com/district0x/district-ui-web3-tx) events and
based on that performs 2 things:
1. Builds up chronological list of transactions. Also stores it in localstorage, but only if
[district-ui-web3-tx](https://github.com/district0x/district-ui-web3-tx) uses localstorage as well.

2. Extends [send-tx](https://github.com/district0x/district-ui-web3-tx#send-tx), so you can pass it key `:tx-log` with
any data you want associate together with a transaction. Then these data can be displayed in transaction log UI component. 

## Installation
Add `[district0x/district-ui-web3-tx-log-core "1.0.3"]` into your project.clj  
Include `[district.ui.web3-tx-log-core]` in your CLJS file, where you use `mount/start`

## API Overview

**Warning:** district0x modules are still in early stages, therefore API can change in a future.

- [district.ui.web3-tx-log-core](#districtuiweb3-tx-log-core)
- [district.ui.web3-tx-log-core.subs](#districtuiweb3-tx-log-coresubs)
  - [::txs](#txs-sub)
  - [::tx-hashes](#tx-hashes-sub)
- [district.ui.web3-tx-log-core.events](#districtuiweb3-tx-log-coreevents)
  - [::add-tx-hash](#add-tx-hash-evt)
  - [::remove-tx-hash](#remove-tx-hash-evt)
- [district.ui.web3-tx-log-core.queries](#districtuiweb3-tx-log-corequeries)
  - [txs](#txs)
  - [tx-hashes](#tx-hashes)
  - [add-tx-hash](#add-tx-hash)
  - [remove-tx-hash](#remove-tx-hash)
  - [assoc-tx-hashes](#assoc-tx-hashes)

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

#### Using send-tx extension
```clojure
(ns my-district.events
    (:require [district.ui.web3-tx.events :as tx-events]))
(dispatch [::tx-events/send-tx {:instance MyContract
                                :fn :my-function
                                :args [1]
                                :tx-opts {:from my-account :gas 4500000}
                                ;; Any data can be passed to :tx-log, depending on what your tx-log component
                                ;; is displaying for each transaction
                                :tx-log {:name "Nice Transaction"}}])
```

## district.ui.web3-tx-log-core.subs
re-frame subscriptions provided by this module:

#### <a name="txs-sub">`::txs [filter-opts]`
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

#### <a name="tx-hashes-sub">`::tx-hashes`
Returns only list of transaction hashes ordered chronologically with newest transactions being first.

## district.ui.web3-tx-log-core.events
re-frame events provided by this module:

#### <a name="add-tx-hash-evt">`::add-tx-hash [tx-hash]`
Adds transaction hash into transaction log list.

#### <a name="remove-tx-hash-evt">`::remove-tx-hash [tx-hash]`
Removes transaction hash from transaction log list.  

## district.ui.web3-tx-log-core.queries
DB queries provided by this module:  
*You should use them in your events, instead of trying to get this module's 
data directly with `get-in` into re-frame db.*

#### <a name="txs">`txs [db]`
Works the same way as sub `::txs`

#### <a name="tx-hashes">`tx-hashes [db]`
Works the same way as sub `::tx-hashes`

#### <a name="add-tx-hash">`add-tx-hash [db tx-hash]`
Adds transaction hash into transaction log list and returns new re-frame db.

#### <a name="remove-tx-hash">`remove-tx-hash [db tx-hash]`
Removes transaction hash from transaction log list and returns new re-frame db.

#### <a name="assoc-tx-hashes">`assoc-tx-hashes [db tx-hashes]`
Associates list of tx-hashes into this module's state.

## Dependency on other district UI modules
* [district-ui-web3-tx](https://github.com/district0x/district-ui-web3-tx)

## Development
```bash
lein deps
# To run tests and rerun on changes
lein doo chrome tests
```