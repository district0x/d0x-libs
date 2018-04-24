# district-ui-tx-button

[![Build Status](https://travis-ci.org/district0x/district-ui-tx-button.svg?branch=master)](https://travis-ci.org/district0x/district-ui-tx-button)

Clojurescript [re-mount](https://github.com/district0x/d0x-INFRA/blob/master/re-mount.md) module which provides a button for submitting blockchain transactions.

## Installation
Add `[district0x/district-ui-tx-button "1.0.0"]` into your project.clj
Include `[district.ui.component.tx-button]` in your CLJS file.

## Module dependencies
This UI component assumes you have following UI modules installed in your app:
* [district-ui-web3-accounts](https://github.com/district0x/district-ui-web3-accounts)

## API Overview

**Warning:** district0x modules and components are still in early stages, therefore API can change in a future.

- [district.ui.component.tx-button](#component)

## <a name="component">`district.ui.component.tx-button`
This namespace contains the reagent UI component with a button for sending transactions. <br>

You can pass following args to this component:
* `:pending?` Takes a boolean value on `true` displays the value passed with the `:pending-text` key.
* `:pending-text` Takes a string to display.
* `:raised-button?` Takes a boolean value and styles the button appropriately.

Basic example:

```clojure
(ns my-district.core
  (:require [mount.core :as mount]
            [district.ui.component.tx-button :as tx-button]
            [district.ui.web3-accounts]
            [district.ui.web3]
            [reagent.core :as r]))

(defn main-panel []co
  [:div.app
   [tx-button/tx-button {:disabled false
                         :pending-text "Sending..."
                         :on-click #(prn "click!")}
    "BUTTON"]])

(defn ^:export init []
  (-> (mount/with-args {:web3 {:url "http://localhost:8549"}
                        :web3-accounts {:polling-interval-ms 5000}})
      (mount/start))
  (r/render [main-panel] (.getElementById js/document "app")))
```

## Development
```bash
lein deps
# To run tests and rerun on changes
lein doo chrome tests
```
