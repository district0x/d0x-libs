# district-ui-router

[![Build Status](https://travis-ci.org/district0x/district-ui-router.svg?branch=master)](https://travis-ci.org/district0x/district-ui-router)

Clojurescript [mount](https://github.com/tolitius/mount) + [re-frame](https://github.com/Day8/re-frame) module for a district UI,
that provides routing functionality for UI. This module currently utilises [bide](https://github.com/funcool/bide) routing library. 
It also provides 2 [reagent](https://github.com/reagent-project/reagent) components helpful for switching pages in UI
based on currently active page.

## Installation
Add `[district0x/district-ui-router "1.0.0"]` into your project.clj  
Include `[district.ui.router]` in your CLJS file, where you use `mount/start`

## API Overview

**Warning:** district0x modules are still in early stages, therefore API can change in a future.

- [district.ui.router](#districtuirouter)
- [district.ui.router.subs](#districtuiroutersubs)
  - [::active-page](#active-page-sub)
  - [::active-page-name](#active-page-name-sub)
  - [::active-page-params](#active-page-params-sub)
  - [::active-page-query](#active-page-params-query)
  - [::resolve](#resolve-sub)
  - [::match](#match-sub)
  - [::bide-router](#bide-router-sub)
  - [::html5?](#html5?-sub)
- [district.ui.router.events](#districtuirouterevents)
  - [::active-page-changed](#active-page-changed-evt)
  - [::watch-active-page](#watch-active-page-evt)
  - [::navigate](#navigate-evt)
  - [::replace](#replace-evt)
- [district.ui.router.effects](#districtuiroutereffects)
  - [::watch-active-page](#watch-active-page-fx)
  - [::navigate](#navigate-fx)
  - [::replace](#replace-fx)
- [district.ui.router.queries](#districtuirouterqueries)
  - [active-page](#active-page)
  - [active-page-name](#active-page-name)
  - [active-page-params](#active-page-params)
  - [active-page-query](#active-page-query)
  - [assoc-active-page](#assoc-active-page)
  - [resolve](#resolve)
  - [match](#match)
  - [bide-router](#bide-router)
  - [html5?](#html5?)
  - [assoc-bide-router](#assoc-bide-router)
  - [assoc-html5](#assoc-html5)
- [district.ui.router.utils](#districtuirouterutils)
  - [resolve](#resolve-util)
  - [match](#match-util)
- [district.ui.component.page](#districtuicomponentpage)
- [district.ui.component.router](#districtuicomponentrouter)
  

## district.ui.router
This namespace contains router [mount](https://github.com/tolitius/mount) module.

You can pass following args to initiate this module: 
* `:routes` Routes as you'd pass them into bide library
* `:default-route` Default route, passed as `:default` into bide library.
* `:html5?` Pass true if you want to use HTML5 history. This option overrides `:html5-hosts`.
* `:html5-hosts` Collection of hostnames for which HTML5 history should be used. You can also pass string of comma separated
hostnames instead of collection. This is useful for defining hostnames in project.clj via `:closure-defines`. 


```clojure
  (ns my-district.core
    (:require [mount.core :as mount]
              [district.ui.router]))
              
  (-> (mount/with-args
        {:router {:routes [["/a" :route/a]
                           ["/b/:b" :route/b]]
                  :default-route :route/a
                  :html5-hosts ["localhost" "mydomain.com"]}})
    (mount/start))
```

## district.ui.router.subs
re-frame subscriptions provided by this module:

#### <a name="active-page-sub">`::active-page`
Returns active page. A map containing `:name` `:params` `:query`.

#### <a name="active-page-name-sub">`::active-page-name`
Returns route name of active page. 

#### <a name="active-page-params-sub">`::active-page-params`
Returns params of active page.

#### <a name="active-page-query-sub">`::active-page-query`
Returns query of active page. 

#### <a name="resolve-sub">`::resolve`
Works as bide's `resolve`, but you don't need to pass router. 

#### <a name="match-sub">`::match`
Works as bide's `match`, but you don't need to pass router.

#### <a name="bide-router-sub">`::bide-router`
Return's bide's router.

#### <a name="html5?-sub">`::html5?`
True if using HTML5 History. 

## district.ui.router.events
re-frame events provided by this module:

#### <a name="active-page-changed-evt">`::active-page-changed`
Event fired when active page has been changed. 

#### <a name="watch-active-page-evt">`::watch-active-page`
Event to call [::watch-active-page](watch-active-page-fx) effect.

#### <a name="unwatch-active-page-evt">`::unwatch-active-page`
Event to call [::unwatch-active-page](unwatch-active-page-fx) effect.

#### <a name="navigate-evt">`::navigate`
Event to call [::navigate](#navigate-fx) effect.

#### <a name="replace-evt">`::replace`
Event to call [::replace](#replace-fx) effect.

## district.ui.router.effects
re-frame effects provided by this module

#### <a name="watch-active-page-fx">`::watch-active-page`
This is special type of effect useful for hooking into [active-page-changed](#active-page-changed) event. Works similarly as
[re-frame-forward-events-fx](https://github.com/Day8/re-frame-forward-events-fx), but instead of dispatching being based on events, it
is based on route name, params and query. This is useful when, for example, your module needs to load data when user visits certain page.

As a param you pass collection of maps containing following keys: 
* `:id` ID of watcher, so you can later unwatch using this ID
* `:name` Route name dispatching will be based on. You can pass also collection of routes or a predicate function.
* `:params` Route params dispatching will be based on. You can also pass predicate function.
* `:query` Route query dispatching will be based on. You can also pass predicate function.
* `:dispatch` Dispatch fired when certain name/params/query is hit. Fired event will get name, params, query as last 3 args.  
* `:dispatch-n` Dispatches fired when certain name/params/query is hit.

You can do dispatching based on either name, params or query, or any combination of two or three of them.  

```clojure
(ns my.district
  (:require
    [district.ui.router.effects :as router-effects]
    [re-frame.core :refer [reg-event-fx]]))


(reg-event-fx
  ::my-event
  (fn []
    ;; When :route/b page is visited ::some-event will be fired
    {::router-effects/watch-active-page [{:id :watcher1
                                          :name :route/b
                                          :dispatch [::some-event]}]}))
                                   
(reg-event-fx
  ::my-event
  (fn []
    ;; When :route/c page with {:a 1} params is visited ::some-event will be fired
    {::router-effects/watch-active-page [{:id :watcher1
                                          :name :route/c
                                          :params {:a 1}
                                          :dispatch [::some-event]}]}))
                                   
(reg-event-fx
  ::my-event
  (fn []
    ;; When any page with {:some-param "abc"} query is visited ::some-event will be fired
    {::router-effects/watch-active-page [{:id :watcher1
                                          :query {:some-param "abc"}
                                          :dispatch [::some-event]}]}))                                                                      
```

#### <a name="unwatch-active-page-fx">`::unwatch-active-page`
Unwatches previously set watcher based on `:id`. 

```clojure
(reg-event-fx
  ::my-event
  (fn []
    {::router-effects/unwatch-active-page [{:id :watcher1}]}))
```

#### <a name="navigate-fx">`::navigate`
Reframe effect to call bide's `navigate!` function.

#### <a name="replace-fx">`::replace`
Reframe effect to call bide's `replace!` function.  


## district.ui.router.queries
DB queries provided by this module:   
*You should use them in your events, instead of trying to get this module's 
data directly with `get-in` into re-frame db.*

#### <a name="active-page">`active-page [db]`
Works the same way as sub `::active-page`

#### <a name="active-page-name">`active-page-name [db]`
Works the same way as sub `::active-page-name`

#### <a name="active-page-params">`active-page-params [db]`
Works the same way as sub `::active-page-params`

#### <a name="active-page-query">`active-page-query [db]`
Works the same way as sub `::active-page-query`

#### <a name="assoc-active-page">`assoc-active-page [db active-page]`
Associates new active-page and returns new re-frame db.

#### <a name="resolve">`resolve [db & args]`
Works the same way as sub `::resolve`

#### <a name="match">`match [db & args]`
Works the same way as sub `::match`

#### <a name="bide-router">`bide-router [db]`
Works the same way as sub `::bide-router`

#### <a name="html5?">`html5? [db]`
Works the same way as sub `::html5?`

#### <a name="assoc-bide-router">`assoc-bide-router [db bide-router]`
Associates new bide router and returns new re-frame db. 

#### <a name="assoc-html5">`assoc-html5 [db html5?]`
Associates whether the module is using html5 history or not. 

## district.ui.router.utils
Util functions provided by this module:

#### <a name="resolve-util">`resolve [name & [params query]]`
Serves as a wrapper for instantly derefed sub `::resolve`.

#### <a name="match-util">`match [path]`
Serves as a wrapper for instantly derefed sub `::match`. 

```clojure
(ns my-district.core
    (:require [mount.core :as mount]
              [district.ui.router]
              [district.ui.router.utils :as utils]))
              
  (-> (mount/with-args
        {:router {:routes [["/a" :route/a]
                           ["/b/:b" :route/b]]
                  :default-route :route/a}})
    (mount/start))
    
(utils/resolve :route/a)
;; => "/a"

(utils/resolve :route/b {:b "abc"} {:c "xyz"})
;; => "/b/abc?c=xyz"

(utils/match "/b/abc?c=xyz")
;; => [:route/b {:b "abc"} {:c "xyz"}]
```

## district.ui.component.page
Multimethod to define pages upon. `district.ui.component.router` component will then route those pages according to active-page.

```clojure
(ns my-district.core
  (:require 
    [district.ui.component.page :refer [page]]))

(defmethod page :route/home []
  [:div "Welcome to Home Page"])
```

## district.ui.component.router
Components that switches pages (as defined via `district.ui.component.page`) based on current active-page.

```clojure
(ns my-district.core
  (:require
    [reagent.core :as r]
    [mount.core :as mount]
    [district.ui.router] 
    [district.ui.component.page :refer [page]]
    [district.ui.component.router :refer [router]]))

  (-> (mount/with-args
        {:router {:routes [["/" :route/home]
                           ["/user" :route/user]]
                  :default-route :route/home}})
    (mount/start))

(defmethod page :route/home []
  [:div "Welcome to Home Page"])
  
(defmethod page :route/user []
  [:div "Welcome to User Page"])

(r/render [router] (.getElementById js/document "app"))
```

## Development
```bash
lein deps
# To run tests and rerun on changes
lein doo chrome tests
```