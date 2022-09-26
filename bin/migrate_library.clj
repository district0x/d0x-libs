#!/usr/bin/env bb

(ns migrate-library
  (:require [clojure.java.shell :refer [sh with-sh-dir]]
            [clojure.string :refer [trim]]
            [babashka.fs :as fs]
            [clojure.edn :as edn]
            [clojure.pprint :as pp]
            [script-helpers :refer [log read-edn write-edn] :as helpers]))

(defn absolutize-path [path]
  (let [absolute-path? (clojure.string/starts-with? path "/")]
    (if absolute-path? path (str (fs/cwd) "/" path))))

(defn library-structure-as-expected? [library-path]
  (let [absolute-path (absolutize-path library-path)
        using-deps? (fs/exists? (str absolute-path "/deps.edn"))]
    using-deps?))

(defn add-aliases
  "Adds aliases to deps.edn map to allow building, loading and releasing subsets of
  the libraries under this monorepo. Adds under:
    1) {:aliases {:<server/browser/shared>-all / {...}}
    2) {:aliases {:<library-name> {...}}"
  [deps-map library-path library-name group]
  (let [deps-edn-template {:paths [] :deps {} :aliases {}}
        library-relative-path library-path ; FIXME: parse or pass in library relative path e.g. server/...
        library-alias-key (keyword library-name)
        group-id "is.mad"
        artefact-id library-name
        new-extra-deps {(symbol (str group-id "/" artefact-id)) {:local/root library-relative-path}}
        new-extra-paths [(str library-path "/test")]
        library-alias-value {:extra-deps new-extra-deps
                             :extra-paths new-extra-paths}
        ; The group deps are useful to be able to build or load all libraries
        ; in the group at once E.g. to load all `server` libraries (under alias
        ; :server-all (in REPL) or to build `browser` bundle with all libraries
        ; (under alias :browser-all)
        group-alias (keyword (str group "-all"))
        old-group-deps (or (get-in deps-map [:aliases group-alias :extra-deps]) {})
        old-group-paths (or (get-in deps-map [:aliases group-alias :extra-paths]) [])
        new-group-deps (merge old-group-deps new-extra-deps)
        new-group-paths (->> old-group-paths
                             (into new-extra-paths)
                             (distinct)
                             (into []))
        updated-group {:extra-deps new-group-deps :extra-paths new-group-paths}]
    (-> deps-map
        (assoc-in [:aliases library-alias-key] library-alias-value)
        (assoc-in [:aliases group-alias] updated-group))))

(defn move-merging-git-histories
  "Creates branch and merges repository at source-path (with its commit
  history) to repository at `target-path` using last component (folder) name as
  the subfolder (or prefix) in the target. Optionally the destination can be
  specified with 3rd argument, e.g. \"server/smart-contracts\". "
  [source-path target-path group]
  (let [source-name (str (last (fs/components source-path))) ; the-thing from /this/is/the-thing
        prefix (str group "/" source-name) ; e.g. browser/district-ui-web3
        merge-result (atom {})
        deps-edn-path (str target-path "/deps.edn")
        has-changes? (= 1 (:exit (sh "git" "diff-index" "HEAD" "--exit-code" "--quiet" :dir target-path)))]
    (with-sh-dir target-path
      (if has-changes?
        (throw (ex-info "Can't continue because repo has changes. Stash or reset them before trying again" {})))
      (sh "git" "checkout" "-b" source-name) ; Create new branch where to put the history
      (log "Migrating" source-path "to" target-path "under" prefix)
      (reset! merge-result (sh "git" "subtree" "add" (str "--prefix=" prefix) source-path "master")))
    (if (= 1 (:exit @merge-result))
      (throw (ex-info (str "Failed running `git subtree`: " (:err @merge-result)) @merge-result)))
    (-> (read-edn deps-edn-path)
        (add-aliases ,,, prefix source-name group)
        (write-edn ,,, deps-edn-path))
    (log "Done updating deps.edn at" deps-edn-path)))

(defn -main [& args]
  (let [[library-path target-path group] *command-line-args*]
    (cond
      (library-structure-as-expected? library-path) (move-merging-git-histories library-path target-path group)
      :else (log "Library doesn't have the expected structure (needs deps.edn)"))))

; To allow running as commandn line util but also required & used in other programs or REPL
; https://book.babashka.org/#main_file
(when (= *file* (System/getProperty "babashka.file"))
  (apply -main *command-line-args*))
