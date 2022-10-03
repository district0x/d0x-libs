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

(defn load-all-libs-in-subfolders
  "Takes path and returns list of paths to deps.edn files in its direct subfolders"
  [path]
  (let [library-paths (->> (fs/list-dir path)
                           (filter fs/directory?)
                           (filter contains-deps-edn?))]
    (map #(str % "/deps.edn") library-paths)))

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
  "Takes one or more modified libraries and a single version (newest). Returns
  deps-details map with :deps (contents of deps.edn) updated for each library
  that depends on an entry in `modified-libs` directly or transitively."
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
  (let [updated-deps-details (calculate-deps-updates version [library] deps-details)]
    (filter-changed-deps-details deps-details updated-deps-details)))

(defn guess-group-id [library-path]
  ; Later it could try to read it from lib_version.clj or from a configuration file
  "is.mad")

(defn lib-name-from-path
  "Takes deps.edn path and returs penultimate component of the path.
  E.g. /one/two/three-is-lib/deps.edn => three-is-lib"
  [library-path]
  (-> library-path
      fs/components
      reverse
      second
      str))

(defn collect-deps-details
  ([deps-edn-path] (collect-deps-details deps-edn-path guess-group-id))
  ([deps-edn-path group-id-fn]
   {:library (symbol (str (guess-group-id deps-edn-path) "/" (lib-name-from-path deps-edn-path)))
    :path (clojure.string/replace deps-edn-path #"/deps.edn$" "")
    :edn (helpers/read-edn deps-edn-path)}))

(defn write-deps-detail [detail]
  (let [library (:library detail)
        target-path (str (:path detail) "/deps.edn")
        deps-edn (:edn detail)]
    (println "WRITING UPDATES" library target-path deps-edn)
    (helpers/write-edn deps-edn target-path)
    target-path))

(defn update-deps-at-path [updated-library new-version updatable-libraries-path]
  (let [deps-details (map collect-deps-details (load-all-libs-in-subfolders updatable-libraries-path))
        change-details (updated-deps new-version updated-library deps-details)]
    (map write-deps-detail change-details)))

(defn -main [& args]
  (let [[library version updated-path] *command-line-args*]
    (log "Updating dependencies. Starting from:" library version updated-path)
    (update-deps-at-path library version updated-path)))

; To allow running as commandn line util but also required & used in other programs or REPL
; https://book.babashka.org/#main_file
(when (= *file* (System/getProperty "babashka.file"))
  (apply -main *command-line-args*))
