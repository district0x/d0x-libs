# district-ui-web3-sync-now

[![CircleCI](https://circleci.com/gh/district0x/district-ui-web3-sync-now/tree/master.svg?style=svg)](https://circleci.com/gh/district0x/district-ui-web3-sync-now/tree/master)

Clojurescript [re-mount](https://github.com/district0x/d0x-INFRA/blob/master/re-mount.md) module, that synchronises UI time with the blockchain time.

## Installation
Add [![Clojars Project](https://img.shields.io/clojars/v/io.github.district0x/district-ui-web3-sync-now?include_prereleases)](https://clojars.org/io.github.district0x/district-ui-web3-sync-now)
Include `[district.ui.web3-sync-now]` in your CLJS file, where you use `mount/start`.

## API Overview

**Warning:** district0x modules are still in early stages, therefore API can change in a future.

- [district.ui.web3-sync-now](#module)
- [district.ui.web3-sync-now.events](#events)
  - [::increment-now](#increment-now-event)
  - [::block-number](#block-number-event)
  - [::get-block](#get-block-event)
  - [::set-now](#set-now-event)

## <a name="module"> district.ui.web3-sync-now
This namespace contains now [mount](https://github.com/tolitius/mount) module.
This module has no configuration parameters.

```clojure
(ns my-district.core
  (:require [mount.core :as mount]
            [district.ui.now]
            [district.ui.web3]
            [district.ui.web3-sync-now]
            [district.ui.logging]))

(-> (mount/with-args {:logging {:level :info}
                      :web3 {:url "http://127.0.0.1:8549"}})
    (mount/start))
```

After the `:start` lifecycle method gets called this module waits for the [`:district.ui.web3.events/web3-created`](https://github.com/district0x/district-ui-web3#web3-created) event and sets [`:district.ui.now.subs/now`](https://github.com/district0x/district-ui-now#now-sub) time to the last block time on the blockchain.
Set the logging level to get notified of errors and/or successfull events.

## <a name="events"> district.ui.web3-sync-now.events
re-frame events provided by this module:

#### <a name="increment-now-event">`::increment-now`
Event to increment now time in a re-frame db and the (testrpc) blockchain time by a number of seconds.

```clojure
(ns my-district.core
  (:require [district.ui.web3-sync-now.events :as sync-now-events]
            [re-frame.core :as re-frame]))

(re-frame/dispatch [::sync-now-events/increment-now 300])
```

Errors and successfully handled events will be logged to the JS console.

#### <a name="block-number-event">`::block-number`
This is an utility event called by the `:start` lifecycle method of the [module](#module).
It wraps the [re-frame-web3-fx](https://github.com/district0x/re-frame-web3-fx) `web3/call` effect and chains the returned last block number to the [`::get-block`](#get-block-event) event.
In a typical application you will never need to call this event yourself.

#### <a name="get-block-event">`::get-block`
This is an utility event which wraps the [re-frame-web3-fx](https://github.com/district0x/re-frame-web3-fx) `web3/call` effect and chains the returned last block object to the [`::set-now`](#set-now-event).
In a typical application you will never need to call this event yourself.

#### <a name="set-now-event">`::set-now`
This is an utility event which sets the [`:district.ui.now.subs/now`](https://github.com/district0x/district-ui-now#now-sub) time from the last block time.
Upon success it will log to the JS console.
In a typical application you will never need to call this event yourself.

## Development

1. Setup local testnet

- spin up a testnet instance in a separate shell
  - `npx truffle develop`

2. Run test suite:
- Browser
  - `npx shadow-cljs watch test-browser`
  - open https://d0x-vm:6502
  - tests refresh automatically on code change
- CI (Headless Chrome, Karma)
  - `npx shadow-cljs compile test-ci`
  - ``CHROME_BIN=`which chromium-browser` npx karma start karma.conf.js --single-run``

3. Build
- on merging pull request to master on GitHub, CI builds & publishes new version automatically
- update version in `build.clj`
- to build: `clj -T:build jar`
- to release: `clj -T:build deploy` (needs `CLOJARS_USERNAME` and `CLOJARS_PASSWORD` env vars to be set)