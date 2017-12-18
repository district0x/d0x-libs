# district-server-web3-watcher

Clojurescript-node.js [mount](https://github.com/tolitius/mount) component for a district server, that watches server connection to a blockchain. 

## Installation
Add `[district0x/district-server-web3-watcher "1.0.0"]` into your project.clj  
Include `[district.server.web3-watcher]` in your CLJS file, where you use `mount/start`

**Warning:** district0x components are still in early stages, therefore API can change in a future.

## Real-world example
To see how district server components play together in real-world app, you can take a look at [NameBazaar server folder](https://github.com/district0x/name-bazaar/tree/master/src/name_bazaar/server), 
where this is deployed in production.

## Usage
You can pass following args to web3-watcher component: 
* `:interval` Interval at which it should check the connection. Default: 3s
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

## Component dependencies

### [district-server-config](https://github.com/district0x/district-server-config)
`district-server-web3-watcher` gets initial args from config provided by `district-server-config/config` under the key `:web3-watcher`. These args are then merged together with ones passed to `mount/with-args`.

### [district-server-web3](https://github.com/district0x/district-server-web3)
`district-server-web3-watcher` relies on getting [web3](https://github.com/ethereum/web3.js) instance from `district-server-web3/web3`. That's why, in example, you need to set up `:web3` in `mount/with-args` as well.

If you wish to use custom components instead of dependencies above while still using `district-server-web3-watcher`, you can easily do so by [mount's states swapping](https://github.com/tolitius/mount#swapping-states-with-states).