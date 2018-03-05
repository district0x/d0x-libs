(ns district.ui.reagent-render.spec
  (:require [cljs.spec.alpha :as s]))

(s/def ::container-id string?)

(s/def ::component-ref #(instance? cljs.core/Var %))

(s/def ::opts (s/nilable (s/keys :req-un [::component-ref ::container-id])))
