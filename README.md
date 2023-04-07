# d0x-libs: district0x libraries

This is a monorepo holding most of district0x ClojureScript libraries - for browser, server and shared (work with browser and server).
It relies on [monorepo-tools](https://github.com/district0x/monorepo-tools) made accessible by git submodule `monorepo-tools` and bb tasks using it via `bb.edn`

## Initial setup

1. Clone the repository `git clone git@github.com:district0x/d0x-libs.git`
  - set up `monorepo-tools` git submodule: `git submodule update --init`
2. Make sure you have [Babashka](https://github.com/babashka/babashka#installation) installed
  - it's enough to download the release and put `bb` executable on your PATH
3. Check and use the tasks provided by running `bb tasks`

## Main workflows

### Migrating existing libraries to the monorepo

`bb migrate` is the task that brings commit history (with some path re-writing to keep `git blame` and similar tools working) to (this) monorepo. After migrating the library will be placed in one of the group sub-folders - `browser`, `server` or `shared` and it will look as if the history had been started there (originl commit times, authors and changesets).

Example command:
```shell
bb migrate ../district-server-config . server
```

> Sidenote: before pushing & releasing for the first time a manual release is needed so that the dependencies on CI build & release can be resolved. This release can have any version number (smaller/older than the "real" one to be made by CI)
> Command to use (change library): `bb release 0.0.1 server/district-server-config`

### Working with libraries already in the monorepo

One of the motivations to group all the libraries under a monorepo was to simplify code changes, simplify & dry up build process and make it easier to discover what's available.
When working with libraries in the monorepo, normally it goes like this
1. Make changes on one or more libraries (by editing their source code)
2. Release these libraries (and the other affected by them through a direct or transient dependency)

To facilitate these some useful _babashka_ tasks are avilable:
1. Manually include one or more libraries for the next release
```bash
bb mark-for-release        # help will be printed out about the usage
bb mark-for-release server # all libraries under server/ get included in version-tracking.edn
bb mark-for-release server/district-server-web3 # only one particular library gets released
```
2. After making changes to a library you want to release it AND also all the other libraries affected via dependency relationship
  - the following example says _"is.mad/cljs-web-next got changed so release it with *22.12.13-SNAPSHOT* version on next merge and also all libraries under browser/ that got affected"_
```bash
bb update-versions is.mad/cljs-web3-next 22.12.13-SNAPSHOT /home/madis/code/district0x/d0x-libs/browser
```

## Updating the `monorepo-tools`

Because `monorepo-tools` folder is a git submodule, all the techniques for working with git submodules apply here too.
*TL;DR* `git submodule update --init --recursive --remote`

There are various ways (all standard for working with _git submodules_):
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

## Rationale

The goals that guided this approach were:
1. Make it easier for developers use d0x libraries.
  - by having them all in one place, repository-wide search helps to find the right code
2. Simplify development
  - refactoring becomes easier, as some libraries depend on others and renaming & checking that things work is easier with one codebase
  - being able to run REPL and have all libraries (or a subset of them) available, allows faster prototyping and brings out the best parts of Clojure
3. Simplify releases
  - having this monorepo structure allows via one pull-request run tests for various libraries
  - it also allows release them in bulk (e.g. `is.d0x/district-server`) or individually (e.g. `is.d0x/district-ui-web3`)
