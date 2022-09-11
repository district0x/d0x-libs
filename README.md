# d0x-libs: district0x libraries

This is a monorepo holding most of district0x ClojureScript libraries - for browser, server and shared (work with browser and server).

The goals that guided this approach were:
1. Make it easier for developers use d0x libraries.
  - by having them all in one place, repository-wide search helps to find the right code
2. Simplify development
  - refactoring becomes easier, as some libraries depend on others and renaming & checking that things work is easier with one codebase
  - being able to run REPL and have all libraries (or a subset of them) available, allows faster prototyping and brings out the best parts of Clojure
3. Simplify releases
  - having this monorepo structure allows via one pull-request run tests for various libraries
  - it also allows release them in bulk (e.g. `is.d0x/district-server`) or individually (e.g. `is.d0x/district-ui-web3`)

The repository also contains some helpful tools, namely:
1. `bin/migrate_library.clj` script to migrate existing library (along with its git commit history) to this monorepo
2. `bin/start-repl` script to start repl making one or more libraries available for trying out
3. `bin/run-tests` script to run tests for one or more libraries
4. `bin/make-release` script to publish one or more libraries to Clojars

> More documentation coming up as the libraries and tools get built

## Tools used and included

Some scripts included (e.g. `bin/migrate_library.clj`) use [babashka](https://github.com/babashka/babashka), which is _a native Clojure interpreter for scripting with fast startup_.
To run the tests for these scripts (i.e. not tests for the libraries in the monorepo but tests for the helper tool scripts under `bin`) use:
```
./bin_test/test_runner.clj
```
