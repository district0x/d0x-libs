#!/usr/bin/env bb

(ns migrate-library
  (:require [clojure.java.shell :refer [sh with-sh-dir]]
            [clojure.string :refer [trim]]
            [babashka.fs :as fs]))

(defn log [& args]
  (apply println args))

(defn absolutize-path [path]
  (let [absolute-path? (clojure.string/starts-with? "/" path)]
  (if absolute-path? path (str (fs/cwd) "/" path))))

(defn library-structure-as-expected? [library-path]
  (let [absolute-path (absolutize-path library-path)
        using-deps? (fs/exists? (str absolute-path "/deps.edn"))]
    (and using-deps?)))

(defn move-merging-git-histories
  "Creates branch and merges repository at source-path (with its commit
  history) to repository at `target-path` using last component (folder) name as
  the subfolder (or prefix) in the target. Optionally the destination can be
  specified with 3rd argument, e.g. \"server/smart-contracts\". "
  [source-path target-path & [subfolder-path]]
  (let [source-name (str (last (fs/components source-path)))
        prefix (or subfolder-path source-name)
        ; original-target-branch (:out (sh "git" "branch" "--show-current" :dir target-path))
        original-target-branch (clojure.string/trim (:out (sh "git" "branch" "--show-current" :dir target-path)))
        merge-result (atom {})]
    (with-sh-dir target-path
      (sh "git" "stash") ; FIXME: Shouldn't be necessary. Remembers any changes in the working tree
      (sh "git" "checkout" "-b" source-name)
      (reset! merge-result (sh "git" "subtree" "add" (str "--prefix=" source-name) source-path "master"))
      (sh "git" "checkout" original-target-branch)
      (sh "git" "stash" "pop") ; FIXME: Shouldn't be necessary
      (sh "git" "checkout" source-name))
    (if (= 1 (:exit @merge-result))
      (throw (ex-info (str "Failed running `git subtree`: " (:err @merge-result)) @merge-result)))))

(defn -main [& args]
  (println "WITH ARGS: " args)
  (let [[library-path target-path] *command-line-args*]
    (log "Migrating" library-path "to" target-path)
    (cond
      (library-structure-as-expected? library-path) (move-merging-git-histories library-path target-path)
      :else (log "Library doesn't have the expected structure (needs deps.edn)"))
    (move-merging-git-histories library-path target-path)))

; To allow running as commandn line util but also required & used in other programs or REPL
; https://book.babashka.org/#main_file
(when (= *file* (System/getProperty "babashka.file"))
  (apply -main *command-line-args*))
