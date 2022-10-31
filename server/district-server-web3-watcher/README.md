# district-server-web3-watcher

[![CircleCI](https://dl.circleci.com/status-badge/img/gh/district0x/district-server-web3-watcher/tree/master.svg?style=svg)](https://dl.circleci.com/status-badge/redirect/gh/district0x/district-server-web3-watcher/tree/master)

Clojurescript-node.js [mount](https://github.com/tolitius/mount) module for a district server, that watches server connection to a blockchain.

## Installation
Add `[district0x/district-server-web3-watcher "1.0.3"]` into your project.clj
Include `[district.server.web3-watcher]` in your CLJS file, where you use `mount/start`

**Warning:** district0x modules are still in early stages, therefore API can change in a future.

## Real-world example
To see how district server modules play together in real-world app, you can take a look at [NameBazaar server folder](https://github.com/district0x/name-bazaar/tree/master/src/name_bazaar/server),
where this is deployed in production.

## Usage
You can pass following args to web3-watcher module:
* `:interval` Interval at which it should check the connection. Default: 3s
* `:confirmations` How many offline responses module needs to get before triggers `:on-offline`. Default: 3
* `:on-online` Callback that is run when connection goes from offline to online
* `:on-offline` Callback that is run when connection goes from online to offline

```clojure
(ns my-district
    (:require [mount.core :as mount]
              [district.server.web3-watcher]))

  (-> (mount/with-args
        {:web3 {:port 8545}
         :web3-watcher {:on-offline #(println "Node went offline")
                        :on-online #(println "Node is online again")}})
    (mount/start))
```

## Module dependencies

### [district-server-config](https://github.com/district0x/district-server-config)
`district-server-web3-watcher` gets initial args from config provided by `district-server-config/config` under the key `:web3-watcher`. These args are then merged together with ones passed to `mount/with-args`.

### [district-server-web3](https://github.com/district0x/district-server-web3)
`district-server-web3-watcher` relies on getting [web3](https://github.com/ethereum/web3.js) instance from `district-server-web3/web3`. That's why, in example, you need to set up `:web3` in `mount/with-args` as well.

If you wish to use custom modules instead of dependencies above while still using `district-server-web3-watcher`, you can easily do so by [mount's states swapping](https://github.com/tolitius/mount#swapping-states-with-states).

# Development (build, test)

To try things in REPL:
1. Watch `node-repl` target: `npx shadow-cljs watch node-repl`
2. Run the compiled file: `node out/node-repl.js`
3. Connect REPL client: `lein repl :connect 30333`
4. To use the build, check `src/repl_helper.cljs`

## Node.js

1. Build: `npx shadow-cljs compile test-node`
2. Tests: `node out/node-tests.js`

## Build & release with `deps.edn` and `tools.build`

1. Build: `clj -T:build jar`
2. Release: `clj -T:build deploy`
