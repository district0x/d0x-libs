(ns tests.runner
  (:require
    [doo.runner :refer-macros [doo-tests]]
    [tests.all]
    [cljs.spec.alpha :as s]))

(s/check-asserts true)

(enable-console-print!)

(doo-tests 'tests.all)

