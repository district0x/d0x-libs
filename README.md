# district-server-web3

[![CircleCI](https://circleci.com/gh/district0x/district-server-web3.svg?style=svg)](https://circleci.com/gh/district0x/district-server-web3)

Clojurescript-node.js [mount](https://github.com/tolitius/mount) module for a district server, that takes care of setting up and providing [web3](https://github.com/ethereum/web3.js) instance.

## Installation
Add <br>
[![Clojars Project](https://img.shields.io/clojars/v/district0x/district-server-web3.svg)](https://clojars.org/district0x/district-server-web3) <br>
into your project.clj. <br>
Include `[district.server.web3]` in your CLJS file, where you use `mount/start`.

**Warning:** district0x modules are still in early stages, therefore API can change in a future.

## Usage
You can pass following args to web3 module:
* `:port` Port of locally running [ganache](https://github.com/trufflesuite/ganache-cli) or real ethereum client
* `:url` Full url of the client to connect to.
* `:ping-interval` How often to ping the node to keep the websocket connection alive (milliseconds)
* `:reset-connection-poll-interval` If websocket disconnects or detects an error module calls `on-offline` and starts polling with this interval (milliseconds) if the connection can be re-created.
* `:on-offline` Function to be called when websocket connection disconnects or throws an error.
* `:on-online` Function to be called when connection to the node comes back online.

```clojure
(ns my-district
  (:require [mount.core :as mount]
            [district.server.web3 :refer [web3]]
            [cljs-web3.eth :as web3-eth]))

(-> (mount/with-args
      {:web3 {:port 8545}})
  (mount/start))

(.then (web3-eth/accounts @web3) prn)
;; => ["0x184c2c67dec231c32856e13134670e44f636acc9"]
```
## Module dependencies

### [district-server-config](https://github.com/district0x/district-server-config)
`district-server-web3` gets initial args from config provided by `district-server-config/config` under the key `:web3`. These args are then merged together with ones passed to `mount/with-args`.

If you wish to use custom modules instead of dependencies above while still using `district-server-web3`, you can easily do so by [mount's states swapping](https://github.com/tolitius/mount#swapping-states-with-states).

## Development

```bash
lein npm install
lein doo node "nodejs-tests" auto
```
