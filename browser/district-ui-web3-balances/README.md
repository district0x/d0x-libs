# district-ui-web3-balances

[![Build Status](https://travis-ci.org/district0x/district-ui-web3-balances.svg?branch=master)](https://travis-ci.org/district0x/district-ui-web3-balances)

Clojurescript [re-mount](https://github.com/district0x/d0x-INFRA/blob/master/re-mount.md) module, that handles [web3](https://github.com/ethereum/web3.js/) balances of Ether or other ERC20 tokens.

## Installation
Add `[district0x/district-ui-web3-balances "1.0.2"]` into your project.clj  
Include `[district.ui.web3-balances]` in your CLJS file, where you use `mount/start`

## API Overview

**Warning:** district0x modules are still in early stages, therefore API can change in a future.

- [district.ui.web3-balances](#districtuiweb3-balances)
- [district.ui.web3-balances.subs](#districtuiweb3-balancessubs)
  - [::balances](#balances-sub)
  - [::balance](#balance-sub)
  - [::contracts](#contracts-sub)
  - [::contract-address](#contract-address-sub)
- [district.ui.web3-balances.events](#districtuiweb3-balancesevents)
  - [::load-balances](#load-balances)
  - [::set-balance](#set-balance)
  - [::balance-load-failed](#balance-load-failed)
  - [::stop-watching](#stop-watching)
  - [::stop-watching-all](#stop-watching-all)
- [district.ui.web3-balances.queries](#districtuiweb3-balancesqueries)
  - [balances](#balances)
  - [balance](#balance)
  - [assoc-balance](#assoc-balance)
  - [contracts](#contracts)
  - [contract-address](#contract-address)
  - [merge-contracts](#merge-contracts)
  - [watch-ids](#watch-ids)
  - [concat-watch-ids](#concat-watch-ids)
  - [merge-web3-balances](#merge-web3-balances)

## district.ui.web3-balances
This namespace contains web3-balances [mount](https://github.com/tolitius/mount) module.

You can pass following args to initiate this module: 
* `:contracts` (optional) Map of contracts with their addresses, so you can later refer to them in subscriptions and queries by human-readable key


```clojure
  (ns my-district.core
    (:require [mount.core :as mount]
              [district.ui.web3-balances]))

  (-> (mount/with-args
        {:web3 {:url "https://mainnet.infura.io/"}
         :web3-balances {:contracts {:GNT {:address "0xa74476443119A942dE498590Fe1f2454d7D4aC0d"}
                                     :ICN {:address "0x888666CA69E0f178DED6D75b5726Cee99A87D698"}
                                     :DNT {:address "0x0abdace70d3790235af448c88547603b945604ea"}}}})
    (mount/start))
```
Notice format of `:contracts` map is the same as `:contracts` passed to [district-ui-smart-contracts](https://github.com/district0x/district-ui-smart-contracts),
so you can conveniently pass the same value to both, if you use both modules. 

## district.ui.web3-balances.subs
re-frame subscriptions provided by this module:

#### <a name="balances-sub">`::balances`
Returns all balances.

#### <a name="balance-sub">`::balance [address & [contract]]`
Returns balance of an address. Optionally, you can pass contract to get balance of an ERC20 token. Contract param can be in 3 different forms:
* Contract key as defined in `:contracts` you passed to the module
* Contract address
* Contract instance
* Additionally, if you use keyword `:ETH`, if will refer to Ether 

```clojure
(ns my-district.core
    (:require [mount.core :as mount]
              [district.ui.web3-balances.subs :as balances-subs]
              [cljs-web3.core :as web3]))
  
  (defn home-page []
    (let [addr "0x0000000000000000000000000000000000000000"
          balance-eth (subscribe [::balances-subs/balance addr])
          balance-gnt (subscribe [::balances-subs/balance addr :GNT])
          balance-icn (subscribe [::balances-subs/balance addr "0x888666CA69E0f178DED6D75b5726Cee99A87D698"])
          balance-dnt (subscribe [::balances-subs/balance addr DNTInstance])] ;; def of DNTInstance is skipped  
      (fn []
        [:div "Address " addr " has following balances:"]
        [:div (web3/from-wei @balance-eth :ether) " ETH"]
        [:div (web3/from-wei @balance-gnt :ether) " GNT"]
        [:div (web3/from-wei @balance-icn :ether) " ICN"]
        [:div (web3/from-wei @balance-dnt :ether) " DNT"])))
```

#### <a name="contracts-sub">`::contracts`
Returns map of contracts as passed into module at start

#### <a name="contract-address-sub">`::contract-address [contract-key]`
Returns address of a contract by its key

## district.ui.web3-balances.events
re-frame events provided by this module:

#### <a name="load-balances">`::load-balances [items]`
Loads collection of balances. To each item, you can pass `:watch? true` to keep watching balance for changes.
Also, you can optionally pass `:contract` in same 3 different forms as you'd pass into subscription `::balance`.

```clojure
(let [addr "0x0000000000000000000000000000000000000000"]
    (dispatch [::events/load-balances
               [{:address addr :watch? true}
                {:address addr :contract :GNT}
                {:address addr :contract "0x0abdace70d3790235af448c88547603b945604ea"}
                {:address addr :contract ICNInstance :watch? true}]]))
```

#### <a name="set-balance">`::set-balance [item]`
Sets a balance into re-frame db. Format of `item` is same as for `::load-balances`. You can use this event to hook into 
event flow, to be notified when a balance was loaded. 

#### <a name="balance-load-failed">`::balance-load-failed`
Fired when there was an error loading a balance.

#### <a name="stop-watching">`::stop-watching [items]`
Use to stop watching balances. Format of `items` is same as for `::load-balances`.

#### <a name="stop-watching-all">`::stop-watching-all`
Stops watching all balances watched by this module. 

## district.ui.web3-balances.queries
DB queries provided by this module:  
*You should use them in your events, instead of trying to get this module's 
data directly with `get-in` into re-frame db.*

#### <a name="balances">`balances [db]`
Works the same way as sub `::balances`

#### <a name="balances">`balances [db address & [contract]]`
Works the same way as sub `::balance`

```clojure
(ns my-district.events
    (:require [district.ui.web3-balances.queries :as balances-queries]
              [re-frame.core :refer [reg-event-fx]]))

(reg-event-fx
  ::my-event
  (fn [{:keys [:db]}]
    (if (.gt (balances-queries/balance db "0x0000000000000000000000000000000000000000" :DNT) 0)
      {:dispatch [::has-some-dnt]}
      {:dispatch [::has-no-dnt]})))
```

#### <a name="assoc-balance">`assoc-balance [db address balance]`
#### `assoc-balance [db address contract balance]`
Associates balance and returns new re-frame db.

#### <a name="merge-balances">`merge-balances [db balances]`
Merges balances into existing ones and returns new re-frame db.

#### <a name="contracts">`contracts [db]`
Returns all contracts

#### <a name="contract-address">`contract-address [db contract-key]`
Returns contract address by key.

#### <a name="merge-contracts">`merge-contracts [db contracts]`
Merges contracts into existing ones and returns new re-frame db.

#### <a name="watch-ids">`watch-ids [db]`
Returns ids of currently watched balances

#### <a name="concat-watch-ids">`concat-watch-ids [db watch-ids]`
Concats new watch-ids with existing ones and returns new re-frame db.

#### <a name="merge-web3-balances">`merge-web3-balances [db {:keys [:balances :contracts :watch-ids]}]`
Merges balances, contracts and watch-ids into this module. 

## Dependency on other district UI modules
* [district-ui-web3](https://github.com/district0x/district-ui-web3)

## Development
```bash
lein deps

# To run tests and rerun on changes
lein doo chrome tests
```