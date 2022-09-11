#!/usr/bin/env bb
;; Inspired from https://book.babashka.org/#_running_tests

(require '[clojure.test :as t]
         '[clojure.string :as string]
         '[babashka.classpath :as cp]
         '[babashka.fs :as fs])

(cp/add-classpath "bin:bin_test")

(defn test-file->test-ns
  [file]
  (as-> file $
        (fs/components $)
        (drop 1 $)
        (mapv str $)
        (string/join "." $)
        (string/replace $ #"_" "-")
        (string/replace $ #".clj$" "")
        (symbol $)))

(def test-namespaces
  (->> (fs/glob "./" "**/*_tests.clj")
       (mapv test-file->test-ns)))

(apply require test-namespaces)

(def test-results
  (apply t/run-tests test-namespaces))

(def failures-and-errors
  (let [{:keys [:fail :error]} test-results]
    (+ fail error)))

(System/exit failures-and-errors)
