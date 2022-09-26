#!/usr/bin/env bb

(ns update-library-versions
  (:require [clojure.java.shell :refer [sh with-sh-dir]]
            [clojure.string :refer [trim]]
            [babashka.fs :as fs]
            [clojure.edn :as edn]
            [clojure.pprint :as pp]

            [script-helpers :refer [log read-edn write-edn] :as helpers]))

(defn contains-deps-edn? [path]
  (fs/exists? (str path "/deps.edn")))

(defn version->numeric [version]
  (map #(Integer/parseInt %) (clojure.string/split version #"\.")))

(defn version< [version-a version-b]
  (let [a-parts (into [] (version->numeric version-a))
        b-parts (into [] (version->numeric version-b))]
    (if (or (nil? version-a) (nil? version-b))
      false
      (= -1 (compare a-parts b-parts)))))

(defn load-deps [path]
  (let [library-paths (->> (fs/list-dir path)
                          (filter fs/directory?)
                          (filter contains-deps-edn?))
        deps-paths (map #(str % "/deps.edn") library-paths)]))

(defn update-deps-if-outdated [library-version library-name deps-details]
  (let [deps-map (:edn deps-details)
        library-sym (symbol library-name)
        used-version (get-in deps-map [:deps library-sym :mvn/version])] ; io.github.district0x/cljs-web3-next {:mvn/version "0.2.0-SNAPSHOT"}
    (cond
      (nil? used-version) nil
      (nil? library-version) nil
      (version< used-version library-version) (assoc-in deps-details [:edn :deps library-sym :mvn/version] library-version))))

(defn find-first-dep
 "Given library as symbol finds the entry in vector or list of maps that has it
 under {:library <...>}"
 [library deps-details-to-search]
  (first (filter #(= (:library %) library) deps-details-to-search)))

(defn given-or-newer-deps [new-deps-details old-single-detail]
  (or (find-first-dep (:library old-single-detail) new-deps-details) old-single-detail))

(defn calculate-deps-updates
  [version modified-libs deps-details]
  (if (empty? modified-libs)
    deps-details
    (let [first-lib (first modified-libs)
          rest-libs (rest modified-libs)
          new-deps-details (map #(update-deps-if-outdated version first-lib %) deps-details)
          compacted (remove nil? new-deps-details)
          changed-lib-names (map :library compacted)
          updated-deps-details (doall (map #(given-or-newer-deps compacted %) deps-details))
          modified-libs-remaining (into rest-libs changed-lib-names)]
      (calculate-deps-updates version modified-libs-remaining updated-deps-details))))

(defn filter-changed-deps-details
  "Takes 2 vectors of dep details and returns only the entries that have changed in *newer*"
  [older newer]
  (filter (fn [dep-detail]
            (not (let [library (:library dep-detail)
                       old-first (find-first-dep library older)
                       new-first (find-first-dep library newer)]
                   (= old-first new-first)))) newer))

(defn updated-deps
  "Takes 3 params:
    1) version (calendar versioning) e.g. 2022.09.22
    2) library symbol e.g. is.mad/some-library
    3) vector of deps.edn details like {:library <symbol> :path <string path filesystem>
                                        :deps <contents of deps.edn>}
  And finds dependencies and transitive dependencies (recursively) that need to
  be updated directly or as a result of another library getting updated.

  Returns vector described in 3) with ONLY the updated deps.edn details (i.e.
  if it stayed the same, gets filtered)
  "
  [version library deps-details]
  (filter-changed-deps-details deps-details (calculate-deps-updates version [library] deps-details)))

(defn -main [& args]
  (let [[library-name version updated-path] *command-line-args*]
    (log "Updating dependencies. Starting from:" library-name version updated-path)))

; To allow running as commandn line util but also required & used in other programs or REPL
; https://book.babashka.org/#main_file
(when (= *file* (System/getProperty "babashka.file"))
  (apply -main *command-line-args*))
