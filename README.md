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

The babashka tasks (implemented in `monorepo-tools` and made available via `bb.edn`):
```
bb migrate         Import existing CLJS (using shadow-cljs, deps.edn) library with history from git repo
bb run-tests       Generates config for CirlceCi dynamic config continuation steps
bb update-versions Take changed library and bump versions of all affected by it through dependency
bb mt-test         Run monorepo-tools tests
```

## Getting started

1. Clone the repository `git clone git@github.com:district0x/d0x-libs.git`
  - set up `monorepo-tools` git submodule: `git submodule update --init`
2. Make sure you have [Babashka](https://github.com/babashka/babashka#installation) installed
  - it's enough to download the release and put `bb` executable on your PATH
3. Check and use the tasks provided `bb tasks`

## Updating the `monorepo-tools`

There are various ways:
1. Updating the submodule directory
  - first add and commit and push the changes *being inside the monorepo-tools* folder
  - then at the top level add and commit that the submodule now refers to
2. Inside a separately cloned `monorepo-tools` folder
  - add, commit and push like normal
  - once inside `d0x-libs` update the reference that submodule refers using: `git submodule update --remote`
  - then add, commit & push the change (so that others updating their `d0x-libs`) also start using new `monorepo-tools`
3. If the changes are in separate branch of `monorepo-tools` (pushed to remote)
  - `git submodule set-branch --branch fix-version-tracking monorepo-tools` (changing _fix-version-tracking_ for the branch name)
  - `git submodule update --init --recursive --remote` to pull in the new code
  - then add & commit changes on repo root level, in case you want to share the `d0x-libs` working against a branch of `monorepo-tools`

Or if you or someone else made changes to `monorepo-tools` (and you don't have any local changes) the easiest way is to run the following from your monorepo root
```
git submodule update --init --recursive --remote
```
