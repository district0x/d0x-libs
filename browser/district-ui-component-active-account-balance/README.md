# district-ui-component-active-account-balance

Clojurescript [reagent](https://github.com/reagent-project/reagent) UI component for displaying ETH or token balance of an active web3 account.

## Installation
Add `[district0x/district-ui-component-active-account-balance "1.0.0"]` into your project.clj  
Include `[district.ui.component.active-account-balance]` in your CLJS file

## Module dependencies
This UI component assumes you have following UI modules installed in your app: 
* [district-ui-web3-account-balances](https://github.com/district0x/district-ui-web3-account-balances)

## district.ui.component.active-account-balance
This namespace contains reagent UI component to display active account token balance. 

You can pass following props to the `active-account-balance` component:
* `:contract` Contract to obtain balance from, as you'd pass it into [district-ui-web3-account-balances#active-account-balance](https://github.com/district0x/district-ui-web3-account-balances#active-account-balance-sub).
Default is `:ETH`. 
* `:token-code` Code of a token to be displayed after the balance. 
* `:locale` Locale for number formatting. Default as configured in [district-format](https://github.com/district0x/district-format)
* `:min-fraction-digits` Min. fraction digits to display. Default 0
* `:max-fraction-digits` Max. fraction digits to display. Default 2

```clojure
(ns my-district.core
  (:require [district.ui.component.active-account-balance :refer [active-account-balance]]))

(defn home-page []
  [:div.accounts
   [active-account-balance
    {:token-code :DNT
     :contract :DNT
     :class :dank
     :locale "en-US"
     :max-fraction-digits 3
     :min-fraction-digits 2}]
   [active-account-balance
    {:token-code :ETH
     :locale "en-US"
     :max-fraction-digits 3
     :min-fraction-digits 2}]])
```
