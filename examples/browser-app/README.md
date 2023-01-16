# Example Browser app

## Usage

1. Start watching the source: `clojure -A:dev:shadow-cljs watch browser-app`
2. Open browser
  - check output in JS console if there's no DOM changes on your examples
3. Connect to REPL:
  - start REPL client: `lein repl :connect 30333`
  - switch to the runtime: `(shadow/repl :browser-app)`
