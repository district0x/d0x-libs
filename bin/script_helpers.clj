(ns script-helpers
  (:require [clojure.java.shell :refer [sh with-sh-dir]]
            [clojure.string :refer [trim]]
            [babashka.fs :as fs]
            [clojure.edn :as edn]
            [clojure.pprint :as pp]))

(defn log [& args]
  (apply println args))

(defn read-edn [deps-edn-path]
  (edn/read-string (slurp deps-edn-path)))

(defn write-edn [deps-map deps-edn-path]
  (pp/pprint deps-map (clojure.java.io/writer deps-edn-path)))
