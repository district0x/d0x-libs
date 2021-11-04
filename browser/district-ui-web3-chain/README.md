# district-ui-web3-chain

[![CircleCI](https://circleci.com/gh/district0x/district-ui-web3-chain.svg?style=svg)](https://circleci.com/gh/district0x/district-ui-web3-chain)

Clojurescript [re-mount](https://github.com/district0x/d0x-INFRA/blob/master/re-mount.md) module, that handles the [web3](https://github.com/ethereum/web3.js/) chain the user wallet is connected to.

## Installation
Add
[![Clojars Project](https://img.shields.io/clojars/v/district0x/district-ui-web3-chain.svg)](https://clojars.org/district0x/district-ui-web3-chain)
into your project.clj
Include `[district.ui.web3-chain]` in your CLJS file, where you use `mount/start`

## API Overview

**Warning:** district0x modules are still in early stages, therefore API can change in a future.

- [district.ui.web3-chain](#districtuiweb3-chain)
- [district.ui.web3-chain.subs](#districtuiweb3-chainsubs)
  - [::chain](#chain-sub)
  - [::has-chain?](#has-chain?-sub)
- [district.ui.web3-chain.events](#districtuiweb3-chainevents)
  - [::load-chain](#load-chain)
  - [::set-chain](#set-chain)
  - [::poll-chain](#poll-chain)
  - [::load-chain](#load-chain)
  - [::chain-changed](#chain-changed)
- [district.ui.web3-chain.queries](#districtuiweb3-chainqueries)
  - [chain](#chain)
  - [has-chain?](#has-chain?)
  - [assoc-chain](#assoc-chain)

## district.ui.web3-chain
This namespace contains web3-chain [mount](https://github.com/tolitius/mount) module. Once you start mount it'll take care
of loading web3 chain.

You can pass following args to initiate this module:
* `:disable-loading-at-start?` Pass true if you don't want load chain at start
* `:disable-polling?` Pass true if you want to disable polling for chain changes (needed for [MetaMask](https://metamask.io/) account switching)
* `:polling-interval-ms` How often should poll for chain changes. Default: 4000
* `:load-injected-chain-only?` Pass true if you want to load chain only when web3 is injected into a browser

```clojure
  (ns my-district.core
    (:require [mount.core :as mount]
              [district.ui.web3-chain]))

  (-> (mount/with-args
        {:web3 {:url "https://mainnet.infura.io/"}
         :web3-chain {:polling-interval-ms 5000}})
    (mount/start))
```

## district.ui.web3-chain.subs
re-frame subscriptions provided by this module:

#### <a name="chain-sub">`::chain`
Returns chain.

#### <a name="has-chain?-sub">`::has-chain?`
Returns true if user is connected to a chain.

```clojure
(ns my-district.home-page
  (:require [district.ui.web3-chain.subs :as chain-subs]
            [re-frame.core :refer [subscribe]]))

(defn home-page []
  (let [chain (subscribe [::chain-subs/chain])]
    (fn []
      (if @chain
        [:div "Your active chain is " @chain]
        [:div "You don't have any active chain"]))))
```

## district.ui.web3-chain.events
re-frame events provided by this module:

#### <a name="load-chain">`::load-chain [opts]`
Loads web3 chain

#### <a name="set-chain">`::set-chain [chain-id]`
Sets chain into db

#### <a name="poll-chain">`::poll-chain [opts]`
Event fired when polling for chain changes in an interval. Note, polling is used only as fallback
option, since MetaMask provides callback registration for [chain changed event](https://docs.metamask.io/guide/ethereum-provider.html#chainchanged)).

#### <a name="chain-changed">`::chain-changed`
Fired when chain have been changed. Use this event to hook into event flow from your modules.
One example using [re-frame-forward-events-fx](https://github.com/Day8/re-frame-forward-events-fx) may look like this:

```clojure
(ns my-district.events
    (:require [district.ui.web3-chain.events :as chain-events]
              [re-frame.core :refer [reg-event-fx]]
              [day8.re-frame.forward-events-fx]))

(reg-event-fx
  ::my-event
  (fn []
    {:register :my-forwarder
     :events #{::chain-events/chain-changed}
     :dispatch-to [::do-something]}))
```

#### <a name="request-switch-chain">`::request-switch-chain [chain-id & chain-info]`
Makes a RPC request to trigger a chain switch in the connected wallet. If chain id is unknown and chain-info
is provided, it will trigger ::request-add-chain event

#### <a name="request-add-chain">`::request-add-chain [chain-info]`
Makes a RPC request to trigger adding a new chain to the connected wallet.


## district.ui.web3-chain.queries
DB queries provided by this module:
*You should use them in your events, instead of trying to get this module's
data directly with `get-in` into re-frame db.*

#### <a name="chain">`chain [db]`
Returns chain

```clojure
(ns my-district.events
    (:require [district.ui.web3-chain.queries :as chain-queries]
              [re-frame.core :refer [reg-event-fx]]))

(reg-event-fx
  ::my-event
  (fn [{:keys [:db]}]
    (if (empty? (chain-queries/chain db))
      {:dispatch [::do-something]}
      {:dispatch [::do-other-thing]})))
```

#### <a name="has-chain?">`has-chain? [db]`
Returns true if user is connected to chain.

#### <a name="assoc-chain">`assoc-chain [db chain]`
Associates chain and returns new re-frame db.

## Dependency on other district UI modules
* [district-ui-web3](https://github.com/district0x/district-ui-web3)
* [district-ui-window-focus](https://github.com/district0x/district-ui-window-focus)

## Development
```bash
lein deps

# To run tests and rerun on changes
lein doo chrome tests
```
