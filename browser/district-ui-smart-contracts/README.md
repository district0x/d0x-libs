# district-ui-smart-contracts

[![Build Status](https://travis-ci.org/district0x/district-ui-smart-contracts.svg?branch=master)](https://travis-ci.org/district0x/district-ui-smart-contracts)

Clojurescript [mount](https://github.com/tolitius/mount) + [re-frame](https://github.com/Day8/re-frame) module for a district UI, that takes care of loading Ethereum smart-contract files.

## Installation
Add `[district0x/district-ui-smart-contracts "1.0.0"]` into your project.clj  
Include `[district.ui.smart-contracts]` in your CLJS file, where you use `mount/start`

**Warning:** district0x modules are still in early stages, therefore API can change in a future.

## district.ui.smart-contracts
This namespace contains smart-contracts [mount](https://github.com/tolitius/mount) module. Once you start mount it'll take care 
of loading smart contract files.

You can pass following args to initiate this module: 
* `:disable-loading-at-start?` Pass true if you don't want load ABIs or BINs at start
* `:smart-contracts` A map of smart-contracts to load
* `:load-bin?` Pass true if you want to load BIN files as well
* `:contracts-path` Path where contracts should be loaded from. Default: `"./contracts/build/"`
* `:contracts-version` Pass some version for bypassing browser's cache, after deploying new contracts to production. 
Pass `:no-cache` if you want to invalidate browser cache on every request (useful for development)
* `:request-timeout` Request timeout for loading files. Default: 10000 (10s)

Passed `smart-contracts` should have following format:
```clojure
(ns my-district.smart-contracts)

(def smart-contracts
  {:my-contract {:name "MyContract"                         ;; ABI and BIN is loaded by this name 
                 :address "0xfbb1b73c4f0bda4f67dca266ce6ef42f520fbb98"
                 ;; optional, if not provided, will try to load 
                 :abi nil
                 ;; optional, if not provided, will try to load
                 :bin nil
                 ;; optional, path would overwrite generic :contracts-path
                 :path nil
                 ;; optional, path would overwrite generic :contracts-version
                 :version nil}})
```

Starting the module may look like this:

```clojure
  (ns my-district.core
    (:require [mount.core :as mount]
              [district.ui.smart-contracts]
              [my-district.smart-contracts]))

  (-> (mount/with-args
        {:web3 {:url "https://mainnet.infura.io/"}
         :smart-contracts {:contracts my-district.smart-contracts/smart-contracts
                           :contracts-path "./"}})
    (mount/start))
```

## district.ui.smart-contracts.subs
re-frame subscriptions provided by this module:

#### `::contracts`
Returns all contracts.

#### `::contract [contract-key]`
Returns contract by `contract-key`

#### `::contract-address [contract-key]`
Returns address of a contract.

#### `::contract-abi [contract-key]`
Returns ABI of a contract.

#### `::contract-bin [contract-key]`
Returns BIN of a contract.

#### `::contract-name [contract-key]`
Returns name of a contract.

#### `::instance [contract-key]`
Returns web3 instance of a contract.

```clojure
(ns my-district.home-page
  (:require [district.ui.smart-contracts.subs :as contracts-subs]
            [re-frame.core :refer [subscribe]]))

(defn home-page []
  (let [contract-abi (subscribe [::contracts-subs/contract-abi :my-contract])]
    (fn []
      [:div "MyContract ABI is: " @contract-abi])))
```

## district.ui.smart-contracts.events
re-frame events provided by this module:

#### `::start [opts]`
Event fired at mount start.

#### `::load-contracts [opts]`
Loads smart contracts. Pass same args as to mount start.

#### `::contract-loaded`
Event fired when a single file was loaded. Either ABI or BIN. 

#### `::contracts-loaded`
Event fired when all smart contract files have been loaded. Use this event to hook into event flow from your modules.
One example using [re-frame-forward-events-fx](https://github.com/Day8/re-frame-forward-events-fx) may look like this:

```clojure
(ns my-district.events
    (:require [district.ui.smart-contracts.events :as contracts-events]
              [re-frame.core :refer [reg-event-fx]]
              [day8.re-frame.forward-events-fx]))
              
(reg-event-fx
  ::my-event
  (fn []
    {:register :my-forwarder
     :events #{::contracts-events/contracts-loaded}
     :dispatch-to [::do-something]}))
```

#### `::set-contract [contract-key contract]`
Sets new contract into re-frame db

#### `::contract-load-failed`
Fired when there was an error loading contract file

#### `::stop`
Cleanup event fired on mount stop.

## district.ui.smart-contracts.queries
DB queries provided by this module:  
*You should use them in your events, instead of trying to get this module's 
data directly with `get-in` into re-frame db.*

#### `contracts [db]`
Works the same way as sub `::contracts`

#### `contract [db contract-key]`
Works the same way as sub `::contract`

#### `contract-address [db contract-key]`
Works the same way as sub `::contract-address`

#### `contract-abi [db contract-key]`
Works the same way as sub `::contract-abi`

#### `contract-bin [db contract-key]`
Works the same way as sub `::contract-bin`

#### `instance [db contract-key]`
Works the same way as sub `::instance`

#### `merge-contracts [db contracts]`
Merges contracts and returns new re-frame db

#### `merge-contract [db contract-key]`
Merges a contract and returns new re-frame db

#### `assoc-contract-abi [db contract-key abi]`
Associates ABI to contract and returns new re-frame db

#### `assoc-contract-bin [db contract-key bin]`
Associates BIN to contract and returns new re-frame db

#### `dissoc-smart-contracts [db]`
Cleans up this module from re-frame db. 

## Dependency on other district UI modules
* [district-ui-web3](https://github.com/district0x/district-ui-web3)

## Development
```bash
lein deps

# To run tests and rerun on changes
lein doo chrome tests
```