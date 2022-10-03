(ns script-helpers
  (:require [clojure.java.shell :refer [sh with-sh-dir]]
            [babashka.fs :as fs]
            [clojure.edn :as edn]
            [clojure.pprint :as pp]))

(defn log [& args]
  (apply println args))

(defn read-edn [deps-edn-path]
  (edn/read-string (slurp deps-edn-path)))

(defn read-clj-wrapped [deps-edn-path]
  (edn/read-string (str "[" (slurp deps-edn-path) "]")))

(defn write-edn [deps-map deps-edn-path]
  (binding [*print-readably* true] ; necessary to get quotes around strings in the written EDN
    (spit deps-edn-path (with-out-str (pp/pprint deps-map)))))
