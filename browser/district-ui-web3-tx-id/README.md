# district-ui-web3-tx-id

[![Build Status](https://travis-ci.org/district0x/district-ui-web3-tx-id.svg?branch=master)](https://travis-ci.org/district0x/district-ui-web3-tx-id)

Clojurescript [mount](https://github.com/tolitius/mount) + [re-frame](https://github.com/Day8/re-frame) module for a district UI,
that extends [district-ui-web3-tx](https://github.com/district0x/district-ui-web3-tx) to provide easy way to associate arbitrary id with a transaction.
This is especially useful when in UI we need to display if certain transaction is pending without knowing its transaction hash. 

## Installation
Add `[district0x/district-ui-web3-tx-id "1.0.0"]` into your project.clj  
Include `[district.ui.web3-tx-id]` in your CLJS file, where you use `mount/start`

## API Overview

**Warning:** district0x modules are still in early stages, therefore API can change in a future.

- [district.ui.web3-tx-id](#districtuiweb3-tx-id)
- [district.ui.web3-tx-id.subs](#districtuiweb3-tx-idsubs)
  - [::tx-hash](#tx-hash-sub)
  - [::tx](#tx-sub)
  - [::tx-status](#tx-status-sub)
  - [::tx-pending?](#tx-pending?-sub)
  - [::tx-success?](#tx-success?-sub)
  - [::tx-error?](#tx-error?-sub)
- [district.ui.web3-tx-id.events](#districtuiweb3-tx-idevents)
  - [::add-tx-hash](#add-tx-hash)
  - [::remove-tx-id](#remove-tx-id)
  - [::clean-localstorage](#clean-localstorage)
- [district.ui.web3-tx-id.queries](#districtuiweb3-tx-idqueries)
  - [tx-hash](#tx-hash)
  - [tx](#tx)
  - [tx-status](#tx-status)
  - [tx-pending?](#tx-pending?)
  - [tx-success?](#tx-success?)
  - [tx-error?](#tx-error?)
  - [remove-tx-id](#remove-tx-id)


## district.ui.web3-tx-id
This namespace contains web3-tx-id [mount](https://github.com/tolitius/mount) module.
There are no configuration parameters for this module.

```clojure
  (ns my-district.core
    (:require [mount.core :as mount]
              [district.ui.web3-tx-id]))
              
  (-> (mount/with-args
        {:web3 {:url "https://mainnet.infura.io/"}})
    (mount/start))
```

#### send-tx extension
```clojure
(ns my-district.events
    (:require [district.ui.web3-tx.events :as tx-events]))
    
(dispatch [::tx-events/send-tx {:instance MyContract
                                :fn :my-function
                                :args [1]
                                :tx-opts {:from my-account :gas 4500000}
                                ;; You can pass anything as :tx-id
                                :tx-id {:my-id 1}}])
```

## district.ui.web3-tx-id.subs
re-frame subscriptions provided by this module:

#### <a name="tx-hash-sub">`::tx-hash [db tx-id & [opts]]`
Returns transaction hash of transaction with given `:tx-id`. For `opts` you can pass following keys:  
* `:from` Gets tx hash only if tx sender's address matches given address. Default is [district-ui-web3-accounts#active-account](https://github.com/district0x/district-ui-web3-accounts#active-account-db).
* `:fn` Gets tx hash only if called contract function matches given `:fn`. 

#### <a name="tx-sub">`::tx [db tx-id & [opts]]`
Returns transaction by `tx-id` as stored by [district-ui-web3-tx](https://github.com/district0x/district-ui-web3-tx). 

#### <a name="tx-status-sub">`::tx-status [db tx-id & [opts]]`
Returns transaction status by `tx-id` as stored by [district-ui-web3-tx](https://github.com/district0x/district-ui-web3-tx).

#### <a name="tx-pending?-sub">`::tx-pending? [db tx-id & [opts]]`
Returns true if transaction status is pending. 

In this example we assume transaction was sent as in [send-tx extension](#send-extension). 
```clojure
(ns my-district.core
    (:require [district.ui.web3-tx-id.subs :as subs]))
  
  (defn home-page []
    (let [tx-pending? (subscribe [::subs/tx-pending? {:my-id 1}])
          same-tx-pending? (subscribe [::subs/tx-pending? {:my-id 1} {:fn :my-function}])]  
      (fn []
        [:div "Transaction pending? " @tx-pending?]
        [:div "Transaction pending? " @same-tx-pending?])))
```

#### <a name="tx-success?-sub">`::tx-success? [db tx-id & [opts]]`
Returns true if transaction status was successfully processed.

#### <a name="tx-error?-sub">`::tx-error? [db tx-id & [opts]]`
Returns true if transaction had an error.

## district.ui.web3-tx-id.events
re-frame events provided by this module:

#### <a name="add-tx-hash">`::add-tx-hash [tx-id tx-hash opts]`
Associates new tx-id with a tx-hash. You don't need to use this unless doing something specific as this event is fired
automatically after [district-ui-web3-tx#send-tx](https://github.com/district0x/district-ui-web3-tx#send-tx). 

#### <a name="remove-tx-id">`::remove-tx-id [tx-hash]`
Removes tx-id association with given tx-hash. You don't need to use this unless doing something specific as this event is fired
automatically after [district-ui-web3-tx#remove-tx](https://github.com/district0x/district-ui-web3-tx#remove-tx).  

#### <a name="clean-localstorage">`::clean-localstorage`
Cleans all tx-id associations from localstorage. 

## district.ui.web3-tx-id.queries
DB queries provided by this module:  
*You should use them in your events, instead of trying to get this module's 
data directly with `get-in` into re-frame db.*

#### <a name="tx-hash">`tx-hash [db tx-id & [opts]]`
Works same was as sub `::tx-hash`.

#### <a name="tx">`tx [db tx-id & [opts]]`
Works same was as sub `::tx`.

#### <a name="tx-status">`tx-status [db tx-id & [opts]]`
Works same was as sub `::tx-status`.

#### <a name="tx-pending?">`tx-pending? [db tx-id & [opts]]`
Works same was as sub `::tx-pending?`.

#### <a name="tx-success?">`tx-success? [db tx-id & [opts]]`
Works same was as sub `::tx-success?`.

#### <a name="tx-error?">`tx-error? [db tx-id & [opts]]`
Works same was as sub `::tx-error?`.

#### <a name="remove-tx-id">`remove-tx-id [db tx-hash]`
Removes tx-id association for tx-hash and returns new re-frame db. 

## Dependency on other district UI modules
* [district-ui-web3-tx](https://github.com/district0x/district-ui-web3-tx)
* [district-ui-web3-accounts](https://github.com/district0x/district-ui-web3-accounts)

## Development
```bash
lein deps
# Start ganache blockchain with 1s block time
ganache-cli -p 8549 -b 1
# To run tests and rerun on changes
lein doo chrome tests
```