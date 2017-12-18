# district-server-web3

[![Build Status](https://travis-ci.org/district0x/district-server-web3.svg?branch=master)](https://travis-ci.org/district0x/district-server-web3)

Clojurescript-node.js [mount](https://github.com/tolitius/mount) component for a district server, that takes care of 
setting up and providing [web3](https://github.com/ethereum/web3.js) instance. 

## Installation
Add `[district0x/district-server-web3 "1.0.0"]` into your project.clj  
Include `[district.server.web3]` in your CLJS file, where you use `mount/start`.

**Warning:** district0x components are still in early stages, therefore API can change in a future.

## Real-world example
To see how district server components play together in real-world app, you can take a look at [NameBazaar server folder](https://github.com/district0x/name-bazaar/tree/master/src/name_bazaar/server), 
where this is deployed in production.

## Usage
You can pass following args to web3 component:   
* `:port` Port of locally running [ganache](https://github.com/trufflesuite/ganache-cli) or real blockchain  
* `:url` Full url of blockchain to connect to
* `:start-ganache-server?` (Temporarily unavailable) Pass true if you want to start [ganache](https://github.com/trufflesuite/ganache-cli) server at `:port` automatically 
* `:use-ganache-provider?` (Temporarily unavailable) Pass true if want to use [ganache](https://github.com/trufflesuite/ganache-cli) as provider without starting a server

Last two options are currently unavailable, because of [ganache issue](https://github.com/trufflesuite/ganache-core/issues/15) that server will hang for synchronous requests. 
**For this reason, use this component only while you have [ganache](https://github.com/trufflesuite/ganache) or real blockchain running on your local machine**

```clojure
(ns my-district
  (:require [mount.core :as mount]
            [district.server.web3 :refer [web3]]
            [cljs-web3.eth :as web3-eth]))

(-> (mount/with-args
      {:web3 {:port 8545}})
  (mount/start))

(web3-eth/accounts @web3)
;; => ["0x184c2c67dec231c32856e13134670e44f636acc9"]
```
## Component dependencies

### [district-server-config](https://github.com/district0x/district-server-config)
`district-server-web3` gets initial args from config provided by `district-server-config/config` under the key `:web3`. These args are then merged together with ones passed to `mount/with-args`.

If you wish to use custom components instead of dependencies above while still using `district-server-web3`, you can easily do so by [mount's states swapping](https://github.com/tolitius/mount#swapping-states-with-states).

## Development
```bash
# To start REPL and run tests
lein deps
lein repl
(start-tests!)

# In other terminal
node tests-compiled/run-tests.js

# To run tests without REPL
lein doo node "tests" once
```