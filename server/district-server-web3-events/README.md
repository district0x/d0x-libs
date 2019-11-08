# district-server-web3-events

[![CircleCI](https://circleci.com/gh/district0x/district-server-web3-events.svg?style=svg)](https://circleci.com/gh/district0x/district-server-web3-events)

Clojurescript-node.js [mount](https://github.com/tolitius/mount) module for a district server, that enables easier setting up and handling web3 smart-contract events.
This module provides ordered dispatching across different smart-contract events. Other server modules can simply plug in into
dispatching cycle by registering callback(s) for any event, being free from handling troublesome web3 events directly.
It also provides ability to cache events into a local file, for later replaying of events from the file. This is especially
useful when debugging QA or production and not having locally synced blockchain.

## Installation
Latest released version of this library: <br>
[![Clojars Project](https://img.shields.io/clojars/v/district0x/district-server-web3-events.svg)](https://clojars.org/district0x/district-server-web3-events)

Include `[district.server.web3-events]` in your CLJS file, where you use `mount/start`

## API Overview

**Warning:** district0x modules are still in early stages, therefore API can change in a future.

- [district-server-web3-events](#districtserverweb3-events)
  - [register-callback!](#register-callback)
  - [register-after-past-events-dispatched-callback!](#register-after-past-events-dispatched-callback)
  - [unregister-callbacks!](#unregister-callbacks)

## Usage
You can pass following args to web3-events module:
* `:events` Collection of smart-contract events the module will start listening to. Pass definition for each event in format
`[contract-key event-key event-opts block-opts]`
* `:write-events-into-file?` Pass true if you want to write events into file. Serves as a cache, for example when you're connecting to a remote Ethereum node.
* `:read-past-events-from-file?` Pass true if you want to read events from a file. The module will read past events from file and then start listening for a new events on connected Ethereum node.
* `:file-path` Path to a file you want to read/write
* `:dispatch-on-error?` Pass true if you want to dispatch an event despite being an error there.
* `:disable-dispatch-logging?` Normally each dispatched event is logged. Pass true to disable.
* `:on-error` Function that will be fired, when there's error in any event callback

Let's see example of using this module:
```clojure
(ns my-district.smart-contracts)

(def smart-contracts
  {:my-contract
   {:name "MyContract"
    :address "0x0000000000000000000000000000000000000000"}})
```

If you're unfamiliar with smart-contracts definition above, please check [district-server-smart-contracts](https://github.com/district0x/district-server-smart-contracts).

Let's take for example simple module, which is supposed to perform some actions on smart-contract events:

```clojure
(ns my-district.my-module
  (:require
    [mount.core :refer [defstate]]
    [district.server.web3-events :refer [register-callback! unregister-callbacks!]]))

(defn handle-some-event []
  (println "Handling some event"))

(defn handle-some-other-event []
  (println "Handling some other event"))

(defn start []
  (register-callback! :my-contract/some-event handle-some-event ::some-event)
  (register-callback! :my-contract/some-other-event handle-some-other-event ::some-other-event)
  opts)

(defn stop []
  (unregister-callbacks! [::some-event ::some-other-event]))

(defstate my-module
  :start (start)
  :stop (stop))
```

Now all we need to do is to include our module and configure web3-events:

```clojure
(ns my-district
  (:require [mount.core :as mount]
            [district.server.smart-contracts]
            [district.server.web3-events]
            [my-district.smart-contracts]
            [my-district.my-module]))


(-> (mount/with-args
      {:web3 {:port 8545}
       :smart-contracts {:contracts-var #'my-district.smart-contracts/smart-contracts
                         :print-gas-usage? true}
       :web3-events {:events {:my-contract/some-event [:my-contract :SomeEvent {} {:from-block 0 :to-block "latest"}]
                              :my-contract/some-other-event [:my-contract :SomeOtherEvent {} {:from-block 0 :to-block "latest"}]}
                     :write-events-into-file? true
                     :file-path "my-events.log"}})
    (mount/start))

```

And that's all! Now handlers in my-module will be fired in exact order as they went through the blockchain.

## module dependencies

### [district-server-config](https://github.com/district0x/district-server-config)
`district-server-web3-events` gets initial args from config provided by `district-server-config/config` under the key `:smart-contracts`. These args are then merged together with ones passed to `mount/with-args`.

### [district-server-web3](https://github.com/district0x/district-server-web3)
`district-server-web3-events` relies on getting [web3](https://github.com/ethereum/web3.js) instance from `district-server-web3/web3`. That's why, in example, you need to set up `:web3` in `mount/with-args` as well.

### [district-server-smart-contracts](https://github.com/district0x/district-server-smart-contracts)
`district-server-web3-events` relies on accessing smart-contract instances via this module. That's why, in example, you need to set up `:smart-contracts` in `mount/with-args` as well.

If you wish to use custom modules instead of dependencies above while still using `district-server-web3-events`, you can easily do so by [mount's states swapping](https://github.com/tolitius/mount#swapping-states-with-states).

## district.server.web3-events
Namespace contains following functions for working with web3 events:
#### <a name="register-callback">`register-callback! [event-key callback & [callback-id]]`
Registers a callback by the key you've passed into configuration of this module. Callback will receive contract and event
as well, so it can be identified backwards. Optionally, you can pass `callback-id` by which you can unregister callback,
if not supplied random will be generated and returned from the function call.

#### <a name="register-after-past-events-dispatched-callback">`register-after-past-events-dispatched-callback! [callback]`
Registers a callback that will be fired once, after all past events were replayed and before listening on latest events starts.
No need to unregister this callback, since it's fired only once, it's automatically unregistered.

#### <a name="unregister-callback">`unregister-callbacks! [callback-ids]`
Unregisters collection of callbacks by their ids.

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

# To run tests without REPL and rerun on file changes
lein doo node "tests" auto
```
