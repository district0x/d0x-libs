# Example node.js app

## Usage

1. Start watching the source: `clojure -A:dev:shadow-cljs watch node-app`
2. Start the app: `node out/node-app.js`
3. Connect to REPL:
  - start REPL client: `lein repl :connect 30333`
  - switch to the runtime: `(shadow/repl :node-app)`

When running with _watch_, the code is automatically re-compiled on save and the namespaces re-loaded in runtime. That means no need to re-start the app (step 2.). Although note that any state you defined manually (in the REPL) will be lost.
