# district-ui-web3-tx-log

[![Build Status](https://travis-ci.org/district0x/district-ui-web3-tx-log.svg?branch=master)](https://travis-ci.org/district0x/district-ui-web3-tx-log)

Clojurescript [re-mount](https://github.com/district0x/d0x-INFRA/blob/master/re-mount.md) module,
that provides [reagent](https://github.com/reagent-project/reagent) UI component for transaction log, as well as related
re-frame events and subscriptions.

## Installation
Add `[district0x/district-ui-web3-tx-log "1.0.3-SNAPSHOT"]` into your project.clj  
Include `[district.ui.web3-tx-log]` in your CLJS file, where you use `mount/start`

## API Overview

**Warning:** district0x modules are still in early stages, therefore API can change in a future.

- [district.ui.web3-tx-log](#districtuiweb3-tx-log)
- [district.ui.component.tx-log](#districtuicomponenttx-log)
  - [tx-log](#tx-log)
  - [header](#header)
  - [settings](#settings)
  - [from-active-address-only-toggle](#from-active-address-only-toggle)
  - [transactions](#transactions)
  - [transaction](#transaction)
  - [no-transactions](#no-transactions)
  - [tx-name](#tx-name)
  - [tx-created-on](#tx-created-on)
  - [tx-gas](#tx-gas)
  - [tx-from](#tx-from)
  - [tx-id](#tx-id)
  - [tx-status](#tx-status)
  - [tx-value](#tx-value)
  - [tx-remove](#tx-remove)
- [district.ui.web3-tx-log.subs](#districtuiweb3-tx-logsubs)
  - [::txs](#txs-sub)
  - [::settings](#settings-sub)
  - [::open?](#open?-sub)
- [district.ui.web3-tx-log.events](#districtuiweb3-tx-logevents)
  - [::set-open](#set-open)
  - [::set-settings](#set-settings)
  - [::clear-localstorage](#clear-localstorage)
- [district.ui.web3-tx-log.queries](#districtuiweb3-tx-logqueries)
  - [settings](#settings-q)
  - [from-active-address-only?](#from-active-address-only?)
  - [localstorage-disabled?](#localstorage-disabled?)
  - [txs](#txs)
  - [merge-settings](#merge-settings)

## district.ui.web3-tx-log
This namespace contains web3-tx-log [mount](https://github.com/tolitius/mount) module.

You can pass following args to initiate this module: 
* `:open-on-tx-hash?` Pass true for opening transaction log automatically when user sends a new tx to the network
* `:tx-costs-currencies` List of currency rates to load, so transaction cost can be displayed in those currencies
* `:disable-using-localstorage?` Pass true if you don't want to store transaction log settings in browser's localstorage


```clojure
  (ns my-district.core
    (:require [mount.core :as mount]
              [district.ui.web3-tx-log]))
              
  (-> (mount/with-args
        {:web3 {:url "https://mainnet.infura.io/"}
         :web3-tx-log {:open-on-tx-hash? true
                       :tx-costs-currencies [:USD]}})
    (mount/start))
```

## district.ui.component.tx-log
Namespace with [reagent](https://github.com/reagent-project/reagent) component for transaction log and its subcomponents.
Components don't come with styling, you need to provide own CSS. Currently, there can be only 1 transaction log per app. 

#### `tx-log`
Main transaction log UI component. It cointains all subcomponents below.
  
Component props (besides standard reagent props):
* `:tx-cost-currency` - Currency to display transaction cost in
* `:header-props` - Props passed to [header](#header) component
* `:header-el` - Pass reagent component if you want to replace [header](#header) component with your own
* `:settings-props` - Props passed to [settings](#settings) component
* `:settings-el` - Pass reagent component if you want to replace [settings](#settings) component with your own
* `:transactions-props` - Props passed to [transactions](#transactions) component
* `:transactions-el` - Pass reagent component if you want to replace [transactions](#transactions) component with your own 

```clojure
(ns my-district.core
    (:require [district.ui.component.tx-log :refer [tx-log]]))

  (defn home-page []
    [tx-log 
     {:tx-cost-currency :USD
      :header-props {:text "My Transaction Log"}
      :class "my-tx-log-class"}])
```

In order for transaction log to work properly, you need to pass following data about a transaction under the key
`:tx-log` when sending a transaction with [district-ui-web3-tx](https://github.com/district0x/district-ui-web3-tx) module:
* `:name` Human readable name for a transaction. Will be displayed by [tx-name](#tx-name)
* `:related-href` Href to go to, after user clicks on a transaction in transaction log. 

```clojure
(ns my-district.events
    (:require [district.ui.web3-tx.events :as tx-events]))
    
(dispatch [::tx-events/send-tx {:instance MyContract
                                :fn :my-function
                                :args [1]
                                :tx-opts {:from my-account :gas 4500000}
                                :tx-log {:name "Called My Function"
                                         :related-href "/some-page"}}])
```

An example of transaction log UI component might look like this:

![tx-log](https://cdn-images-1.medium.com/max/1600/1*LFKH-q4YTVv_qduka4A5fw.png)

#### `header`
Header component of transaction log. 

Component props:
* `:text` - Text displayed in the header

#### `settings`
Settings component of transaction log.

Component props:
* `:from-active-address-only-toggle-props` - Props passed to [from-active-address-only-toggle](#from-active-address-only-toggle) component
* `:from-active-address-only-toggle-el` - Pass reagent component if you want to replace [from-active-address-only-toggle](#from-active-address-only-toggle) component with your own

#### `transactions`
List of transactions component of transaction log.

Component props:
* `:transaction-props` - Props passed to [transaction](#transaction) component
* `:transaction-el` - Pass reagent component if you want to replace [transaction](#transaction) component with your own
* `:no-transactions-props` - Props passed to [no-transactions](#no-transactions) component
* `:no-transactions-el` - Pass reagent component if you want to replace [no-transactions](#no-transactions) component with your own

#### `no-transactions`
Component displayed when there are no transactions, instead of list of transactions

Component props:
* `:text` Displayed text

#### `transaction`
Component of each transaction in transaction log

Component props:
* `:tx` - Transaction data
* `:tx-name-props` - Props passed to [tx-name](#tx-name) component
* `:tx-name-el` - Pass reagent component if you want to replace [tx-name](#tx-name) component with your own
* `:tx-created-on-props` - Props passed to [tx-created-on](#tx-created-on) component
* `:tx-created-on-el` - Pass reagent component if you want to replace [tx-created-on](#tx-created-on) component with your own
* `:tx-gas-props` - Props passed to [tx-gas](#tx-gas) component
* `:tx-gas-el` - Pass reagent component if you want to replace [tx-gas](#tx-gas) component with your own
* `:tx-from-props` - Props passed to [tx-from](#tx-from) component
* `:tx-from-el` - Pass reagent component if you want to replace [tx-from](#tx-from) component with your own
* `:tx-id-props` - Props passed to [tx-id](#tx-id) component
* `:tx-id-el` - Pass reagent component if you want to replace [tx-id](#tx-id) component with your own
* `:tx-status-props` - Props passed to [tx-status](#tx-status) component
* `:tx-status-el` - Pass reagent component if you want to replace [tx-status](#tx-status) component with your own
* `:tx-value-props` - Props passed to [tx-value](#tx-value) component
* `:tx-value-el` - Pass reagent component if you want to replace [tx-value](#tx-value) component with your own
* `:tx-remove-props` - Props passed to [tx-remove](#tx-remove) component
* `:tx-remove-el` - Pass reagent component if you want to replace [tx-remove](#tx-remove) component with your own

#### `tx-name`
Component that displays name of a transaction. 

Component props:
* `:tx` - Transaction data

#### `tx-created-on`
Component that displays creation time of a transaction. 

Component props:
* `:tx` - Transaction data
* `:label` - Label text

#### `tx-gas`
Component that displays gas usage with a gas cost. 

Component props:
* `:tx` - Transaction data
* `:tx-cost-currency` - Currency to display transaction cost in
* `:label` - Label text

#### `tx-from`
Component that displays address from which a transaction was sent.

Component props:
* `:tx` - Transaction data
* `:label` - Label text

#### `tx-id`
Component that displays transaction id with link to the etherscan. 

Component props:
* `:tx` - Transaction data
* `:label` - Label text next to the time

#### `tx-status`
Component that displays transaction status

Component props:
* `:tx` - Transaction data

#### `tx-value`
Component that displays transaction value in Ether

Component props:
* `:tx` - Transaction data

#### `tx-remove`
Component that serves as a button to remove a transaction.

Component props:
* `:tx` - Transaction data

## district.ui.web3-tx-log.subs
re-frame subscriptions provided by this module:

#### <a name="txs-sub">`::txs`
Returns list of transactions in for transaction log.

#### <a name="settings-sub">`::settings`
Returns current transaction log settings.

#### <a name="open?-sub">`::open?`
True if transaction log is currently open

## district.ui.web3-tx-log.events
re-frame events provided by this module:

#### <a name="set-open">`::set-open [open?]`
Sets whether transaction log should open or not. 

#### <a name="set-settings">`::set-settings [settings]`
Sets settings for transaction log.

#### <a name="clear-localstorage">`::clear-localstorage`
Clears transaction log settings from localstorage.

## district.ui.web3-tx-log.queries
DB queries provided by this module:  
*You should use them in your events, instead of trying to get this module's 
data directly with `get-in` into re-frame db.*

#### <a name="settings-q">`settings [db]`
Works the same way as sub `::settings`

#### <a name="from-active-address-only?">`from-active-address-only? [db]`
True if setting `:from-active-address-only?` is on, to show transactions only from active address. 

#### <a name="open?">`open? [db]`
Works the same way as sub `::open?`

#### <a name="txs">`txs [db]`
Works the same way as sub `::txs`

#### <a name="merge-settings">`merge-settings [db settings]`
Merges new settings in returns new re-frame db

#### <a name="localstorage-disabled?">`localstorage-disabled? [db]`
True if localstorage is disabled for this module

## Dependency on other district UI modules
* [district-ui-web3-tx-log-core](https://github.com/district0x/district-ui-web3-tx-log-core)
* [district-ui-web3-tx-costs](https://github.com/district0x/district-ui-web3-tx-costs)
* [district-ui-web3-accounts](https://github.com/district0x/district-ui-web3-accounts)

## Development
```bash
lein deps
# Start ganache blockchain with 1s block time
ganache-cli -p 8549 -b 1 --noVMErrorsOnRPCResponse
# To run tests and rerun on changes
lein doo chrome tests
```
