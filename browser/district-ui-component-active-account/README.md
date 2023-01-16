# district-ui-component-active-account

Clojurescript [reagent](https://github.com/reagent-project/reagent) UI component for displaying user's active web3
account. If user has multiple accounts, select field will be presented, otherwise just plain element with the address.
This component uses [React Semantic UI](https://react.semantic-ui.com). 

## Installation
Add [![Clojars Project](https://img.shields.io/clojars/v/district0x/district-ui-component-active-account.svg)](https://clojars.org/district0x/district-ui-component-active-account) into your project.clj  
Include `[district.ui.component.active-account]` in your CLJS file

## Module dependencies
This UI component assumes you have following UI modules installed in your app: 
* [district-ui-web3-accounts](https://github.com/district0x/district-ui-web3-accounts)

## district.ui.component.active-account
You can pass following props to the `active-account` component:
* `:select-props` Props passed into [Select](https://react.semantic-ui.com/addons/select) with multiple accounts.
* `:single-account-props` Props passed into single account element

```clojure
  (ns my-district.core
    (:require [district.ui.component.active-account :refer [active-account]]))
              
  (defn home-page []
      [active-account])
```