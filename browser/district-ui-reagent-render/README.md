# district-ui-reagent-render

Clojurescript [mount](https://github.com/tolitius/mount) + [re-frame](https://github.com/Day8/re-frame) component for a district UI, that facilitates root [reagent](https://github.com/reagent-project/reagent) UI component mounting.

## Installation
Add `[district0x/district-ui-reagent-render "1.0.0"]` into your project.clj.<br/>
Include `[district.ui.reagent-render]` in your CLJS file, where you use `mount/start`.

## Usage

**Warning:** district0x modules are still in early stages of development, therefore API can change in the future.

- [district.ui.reagent-render](#district.ui.reagent-render)
- [district.ui.reagent-render.events](#district.ui.reagent-render.events)
  - [::start](#start)
  - [::render](#render)
  - [::render-fx](#render-fx)
- [district.ui.reagent-render.spec](#district.ui.reagent-render.spec)
  - [::opts](#opts-spec)

## <a name="district.ui.reagent-render"> district.ui.reagent-render

This namespace contains district-ui-reagent-render [mount](https://github.com/tolitius/mount) module.x
It takes a map of [opts](#opts-spec) as an argument:
* `:container-id` The id of the html element where the root component of you app will be injected.
* `:component-ref` The reference (a [Var](https://clojuredocs.org/clojure.core/var)) to the function which returns the root component.

The validity of the args passed to the module will be checked at runtime if you have set the `clojure.spec.check-asserts` system property to `true`.

```clojure
(ns my-district
  (:require [cljs.spec.alpha :as s]
            [district.ui.other-component]
            [district.ui.reagent-render]
            [district.ui.reagent-render.events :as events]
            [mount.core :as mount]))

(def main-panel []
  [:div "HOME"])

(defn ^:export init []
  (s/check-asserts true)
  (-> (mount/with-args {:reagent-render {:container-id "app"
                                         :component-ref #'main-panel}
                        :other-component {:fu "bar"}})
      (mount/start)))
```

If you're using [figwheel](https://github.com/bhauman/lein-figwheel) for development, ensure hot-reloading by passing the function which starts all the mount component to the figwheel config:

```clojure
:figwheel {:on-jsload "my-district/init"}
```

Call the resulting JS function in your `index.html` file to bootstrap the application::

```html
<!doctype html>
<html lang="en">
  <head>
    <meta charset='utf-8'>
  </head>
  <body>
    <div id="app"></div>
    <script src="js/compiled/app.js"></script>
    <script>my_district.init();</script>
  </body>
</html>
```

## <a name="district.ui.reagent-render.events"> district.ui.reagent-render.events

This namespace contains re-frame events provided by this module.

**Note:** In typical applications you won't be needing to use the provided events, it is enough to just call `mount/start` on the underlying mount module provided by the [district.ui.reagent-render](#district.ui.reagent-render) namespace.
None of the events below do any sort of input checking.

#### <a name="start"> start

This is the handler for the underlying event dispatched synchronously when `mount/start` is invoked. It takes the same arguments as the [district.ui.reagent-render](#district.ui.reagent-render) mount module and dispatches a [`:render`](#render) event.

#### <a name="render"> render

This is just a book-keeping event to make sure that effectful event handler [`render-fx`](#render-fx) is called only once all other synchronously dispatched events from other modules have already happened.
It takes the same arguments as the [district.ui.reagent-render](#district.ui.reagent-render) mount module.

#### <a name="render-fx"> render-fx

This event handler performs the actual work of rendering the root component.
It takes the same arguments as the [district.ui.reagent-render](#district.ui.reagent-render) mount module.

## <a name="district.ui.reagent-render.spec"> district.ui.reagent-render.spec

specs provided by this module:

#### <a name="opts-spec"> opts

Spec for the options passed to the module. You can toggle whether this spec is checked at runtime, see [district.ui.reagent-render](#district.ui.reagent-render).

## Development

Run test suite:

```bash
lein deps
# To run tests and rerun on changes
lein doo chrome tests
```
Install into local repo:

```bash
lein cljsbuild test
lein install
```
