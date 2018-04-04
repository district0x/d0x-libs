# district-ui-smart-contracts

[![Build Status](https://travis-ci.org/district0x/district-ui-smart-contracts.svg?branch=master)](https://travis-ci.org/district0x/district-ui-smart-contracts)

Clojurescript [re-mount](https://github.com/district0x/d0x-INFRA/blob/master/re-mount.md) module, that takes care of loading Ethereum smart-contract files.

## Installation
Add `[district0x/district-ui-smart-contracts "1.0.4"]` into your project.clj  
Include `[district.ui.smart-contracts]` in your CLJS file, where you use `mount/start`

## API Overview

**Warning:** district0x modules are still in early stages, therefore API can change in a future.

- [district.ui.smart-contracts](#districtuismart-contracts)
- [district.ui.smart-contracts.subs](#districtuismart-contractssubs)
  - [::contracts](#contracts-sub)
  - [::contract](#contract-sub)
  - [::contract-address](#contract-address-sub)
  - [::contract-abi](#contract-abi-sub)
  - [::contract-bin](#contract-bin-sub)
  - [::contract-name](#contract-name-sub)
  - [::instance](#instance-sub)
- [district.ui.smart-contracts.events](#districtuismart-contractsevents)
  - [::load-contracts](#load-contracts)
  - [::contract-loaded](#contract-loaded)
  - [::contracts-loaded](#contracts-loaded)
  - [::set-contract](#set-contract)
  - [::contract-load-failed](#contract-load-failed)
- [district.ui.smart-contracts.deploy-events](#districtuismart-contractsdeploy-events)
  - [::deploy-contract](#deploy-contract)
  - [::contract-deploy-failed](#contract-deploy-failed)
- [district.ui.smart-contracts.queries](#districtuismart-contractsqueries)
  - [contracts](#contracts)
  - [contract](#contract)
  - [contract-address](#contract-address)
  - [contract-abi](#contract-abi)
  - [contract-bin](#contract-bin)
  - [contract-name](#contract-name)
  - [instance](#instance)
  - [merge-contracts](#merge-contracts)
  - [merge-contract](#merge-contract)
  - [assoc-contract-abi](#assoc-contract-abi)
  - [assoc-contract-bin](#assoc-contract-bin)

## district.ui.smart-contracts
This namespace contains smart-contracts [mount](https://github.com/tolitius/mount) module. Once you start mount it'll take care 
of loading smart contract files.

You can pass following args to initiate this module: 
* `:disable-loading-at-start?` Pass true if you don't want load ABIs or BINs at start
* `:contracts` A map of smart-contracts to load
* `:load-bin?` Pass true if you want to load BIN files as well
* `:contracts-path` Path where contracts should be loaded from. Default: `"./contracts/build/"`
* `:contracts-version` Pass some version for bypassing browser's cache after deploying new contracts to production. 
Pass `:no-cache` if you want to invalidate browser cache on every request (useful for development)
* `:request-timeout` Request timeout for loading files. Default: 10000 (10s)

Passed `:contracts` should have following format:
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

#### <a name="contracts-sub">`::contracts`
Returns all contracts.

#### <a name="contract-sub">`::contract [contract-key]`
Returns contract by `contract-key`

#### <a name="contract-address-sub">`::contract-address [contract-key]`
Returns address of a contract.

#### <a name="contract-abi-sub">`::contract-abi [contract-key]`
Returns ABI of a contract.

#### <a name="contract-bin-sub">`::contract-bin [contract-key]`
Returns BIN of a contract.

#### <a name="contract-name-sub">`::contract-name [contract-key]`
Returns name of a contract.

#### <a name="instance-sub">`::instance [contract-key]`
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

#### <a name="load-contracts">`::load-contracts [opts]`
Loads smart contracts. Pass same args as to mount start.

#### <a name="contract-loaded`">`::contract-loaded`
Event fired when a single file was loaded. Either ABI or BIN. 

#### <a name="contracts-loaded`">`::contracts-loaded`
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

#### <a name="set-contract">`::set-contract [contract-key contract]`
Sets new contract into re-frame db

#### <a name="contract-load-failed`">`::contract-load-failed`
Fired when there was an error loading contract file

## district.ui.smart-contracts.deploy-events
Events useful for deploying contracts. This namespace is meant to be used only in tests or very simple apps.
Any larger application should be doing smart-contract deployment on server-side via [district-server-smart-contracts](https://github.com/district0x/district-server-smart-contracts).

#### <a name="deploy-contract">`::deploy-contract [contract-key opts]`
Deploys a smart-contract of key `contract-key` and saves new address into re-frame db.

```clojure
  (ns my-district.core
    (:require [mount.core :as mount]
              [district.ui.smart-contracts]
              [district.ui.smart-contracts.deploy-events :as deploy-events]
              [district.ui.smart-contracts.queries :as queries]))

  (-> (mount/with-args
        {:web3 {:url "http://localhost:8549"}
         :smart-contracts 
          {:disable-loading-at-start? true
           :contracts {:deploy-test-contract {:name "DeployTestContract"
                                              :abi (js/JSON.parse "[{\"inputs\":[{\"name\":\"someNumber\",\"type\":\"uint256\"}],\"payable\":false,\"stateMutability\":\"nonpayable\",\"type\":\"constructor\"}]")
                                              :bin "0x60606040523415600e57600080fd5b604051602080607183398101604052808051915050801515602e57600080fd5b50603580603c6000396000f3006060604052600080fd00a165627a7a72305820f6c231e485f5b65831c99412cbcad5b4e41a4b69d40f3d4db8de3a38137701fb0029"}}}})
    (mount/start))
    
(dispatch [::deploy-events/deploy-contract :deploy-test-contract {:gas 4500000
                                                                  :arguments [1]
                                                                  :from "0xb2930b35844a230f00e51431acae96fe543a0347"
                                                                  :on-success [::optional-callback]
                                                                  :on-error [::optional-error-callback]}])
```
When successfully deployed, you'll be able to access contract instance and address same way as other contracts

```clojure
(queries/contract-address db :deploy-test-contract)
(queries/instance db :deploy-test-contract)
```

#### <a name="contract-deploy-failed`">`::contract-deploy-failed`
Event fired when deploying a contract failed. 

## district.ui.smart-contracts.queries
DB queries provided by this module:  
*You should use them in your events, instead of trying to get this module's 
data directly with `get-in` into re-frame db.*

#### <a name="contracts">`contracts [db]`
Works the same way as sub `::contracts`

#### <a name="contract">`contract [db contract-key]`
Works the same way as sub `::contract`

#### <a name="contract-address">`contract-address [db contract-key]`
Works the same way as sub `::contract-address`

#### <a name="contract-abi">`contract-abi [db contract-key]`
Works the same way as sub `::contract-abi`

#### <a name="contract-bin">`contract-bin [db contract-key]`
Works the same way as sub `::contract-bin`

#### <a name="contract-name">`contract-name [db contract-key]`
Works the same way as sub `::contract-name`

#### <a name="instance">`instance [db contract-key]`
Works the same way as sub `::instance`

#### <a name="merge-contracts">`merge-contracts [db contracts]`
Merges contracts and returns new re-frame db

#### <a name="merge-contract">`merge-contract [db contract-key]`
Merges a contract and returns new re-frame db

#### <a name="assoc-contract-abi">`assoc-contract-abi [db contract-key abi]`
Associates ABI to contract and returns new re-frame db

#### <a name="assoc-contract-bin">`assoc-contract-bin [db contract-key bin]`
Associates BIN to contract and returns new re-frame db

## Dependency on other district UI modules
* [district-ui-web3](https://github.com/district0x/district-ui-web3)

## Development
```bash
lein deps

# To run tests and rerun on changes
lein doo chrome tests
```