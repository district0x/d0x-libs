(ns tests.runner
  (:require [cljs.nodejs :as nodejs]
            [doo.runner :refer-macros [doo-tests]]
            [tests.all]))

(nodejs/enable-util-print!)

(doo-tests 'tests.all)
