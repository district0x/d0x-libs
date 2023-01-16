## Monorepo example apps

This folder contains minimal example apps to try out and demonstrate the use of these apps.
It can be very useful to play around with the apps - in the browser or node.js

What makes it useful and handy is that with `shadow-cljs` and `deps.edn` these demo apps can depend on the local dependencies.
It allows you to make changes in the code and verify the changes immediately in a real application context.

### Usage

Build & run (while watching for changes) the **browser** app:
```bash
clojure -A:dev:shadow-cljs watch browser-app
```


Build & run (while watching for changes) the **node.js** app:
```bash
clojure -A:dev:shadow-cljs watch node-app
```
