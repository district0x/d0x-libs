# district-ui-web3-sync-now

[![Build Status](https://travis-ci.org/district0x/district-ui-web3-sync-now.svg?branch=master)](https://travis-ci.org/district0x/district-ui-web3-sync-now)

Clojurescript [re-mount](https://github.com/district0x/d0x-INFRA/blob/master/re-mount.md) module, that synchronises UI time with the blockchain time.

## Installation
Add `[district0x/district-ui-web3-sync-now "1.0.0"]` into your project.clj.
Include `[district.ui.web3-sync-now]` in your CLJS file, where you use `mount/start`.

## API Overview

**Warning:** district0x modules are still in early stages, therefore API can change in a future.

- [district.ui.web3-sync-now](#module)
- [district.ui.web3-sync-now.events](#events)
  - [::increment-now](#increment-now-event)
  - [::set-now](#set-now-event)
  - [::increase-evm-time](#set-now-event)

## <a name="module"> district.ui.web3-sync-now
This namespace contains now [mount](https://github.com/tolitius/mount) module.
This module has no configuration parameters.

```clojure
(ns my-district.core
  (:require [mount.core :as mount]
            [district.ui.web3-sync-now]))

(mount/start)
```

After the `:start` lifecycle method gets called this module waits for the [`:district.ui.web3.events/web3-created`](https://github.com/district0x/district-ui-web3#web3-created) event and sets [`:district.ui.now.subs/now`](https://github.com/district0x/district-ui-now#now-sub) time to the last block time on the blockchain.

## district.ui.web3-sync-now.events
re-frame events provided by this module:

#### <a name="increment-now-event">`::increment-now`
Event to increment now time in a re-frame db and the (testrpc) blockchain time by a number of milliseconds.

```clojure
(ns my-district.core
  (:require [district.ui.web3-sync-now.events :as sync-now-events]
            [re-frame.core :as re-frame]))

(re-frame/dispatch [::sync-now-events/increment-now 8.64e+7])
```

#### <a name="set-now-event">`::set-now`
This is an utility event which sets the  [`:district.ui.now.subs/now`](https://github.com/district0x/district-ui-now#now-sub) time and is called by the `:start` lifecycle method of the [module](#module).
In a typical application you will never need to call this event yourself.

#### <a name="increase-evm-time">`::increase-evm-time`
This is an utility side-effect which sets the (testrpc) blockchain time.
In a typical application you will never need to call this effect yourself.

## Development

Run test suite:

```bash
lein deps
# To run tests and rerun on changes
lein doo chrome tests
```
Install into local repo:

```bash
lein install
```
