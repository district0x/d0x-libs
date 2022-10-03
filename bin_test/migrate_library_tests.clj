(ns migrate-library-tests
  (:require [migrate-library :as ml]
            [update-library-versions :as ulv]
            [clojure.test :refer [deftest testing is]]
            [babashka.fs :as fs]
            [clojure.java.shell :refer [sh]]
            [script-helpers :as helpers]))

; Run tests in VIM
; :nmap <leader>tr :VtrSendCommandToRunner ./bin_test/test_runner.clj<CR>

(defn count-commits [path & {:keys [branch]}]
  (-> (sh "git" "rev-list" (or branch "HEAD") "--count" :dir path)
      :out
      clojure.string/trim
      Integer/parseInt))

(defn split-by-space [string]
  (clojure.string/split string #"\s"))

(defn file-set-in [path]
  (->> path
      fs/list-dir
      (map str)
      (map fs/file-name)
      (remove #{".git"})
      (into #{})))

(defn get-extracted-folder-name [tar-output]
  (-> tar-output
      :out
      clojure.string/split-lines
      last
      clojure.string/trim
      fs/components
      first
      str))

(defn set-up-git-repo
  "Takes absolute path of .tar.gz archive that contains a Git repository.
   Returns absolute path of that archive extracted into system temp folder"
  [archive-path temp-path]
  (let [extraction-result (sh "tar" "-xvf" (str archive-path) "-C" (str temp-path))
        repo-name (get-extracted-folder-name extraction-result)
        extracted-repo-path (str temp-path "/" repo-name)]
    ; For some reason there are changes that git status doesn't show but git diff-index does
    ; Due to these git subtree gives an error
    ; These changes can be detected with
    ;   git diff-index HEAD --exit-code --quiet <-- exits with code 1 when changes present
    ;   git diff-index HEAD                     <-- produces visual output with changes
    ; Because this is a test repo and no changes are expected, hard reset
    ; can be done to avoid the issue
    (sh "git" "reset" "--hard" "HEAD" :dir extracted-repo-path)
    extracted-repo-path))

(deftest migrate-library-tests
  (testing "checks library structure (deps.edn, shadow-cljs)"
    (is (false? (ml/library-structure-as-expected? "bin_test/fixtures/lib-no-deps")))
    (is (true? (ml/library-structure-as-expected? "bin_test/fixtures/lib-ok"))))

  (testing "moves selected library to subfolder with git history"
    (let [temp-path (fs/create-temp-dir {:prefix "migrate-library-tests"})
          test-repo-path (set-up-git-repo "bin_test/fixtures/test-repo.tar.gz" temp-path)
          container-repo-path (set-up-git-repo "bin_test/fixtures/container-repo.tar.gz" temp-path)]
      (ml/move-merging-git-histories test-repo-path container-repo-path "shared")
      (is (= 5 (count-commits container-repo-path))) ; 2 commits from container + 2 from test-repo + 1 merge
      (is (= (file-set-in (str container-repo-path "/shared/test-repo")) (file-set-in test-repo-path)))))

  (testing "adding aliases to deps.edn"))

(deftest update-dependency-versions-tests
  (let [candidate-deps [{:library 'is.mad/shared-low-level-a :path "shared/low-a" :edn {:paths ["src"] :deps {'is.mad/first-to-update {:mvn/version "2022.09.20"}}}}
                        {:library 'is.mad/server-low-level-b :path "server/low-b" :edn {:paths ["src"] :deps {'is.mad/shared-low-level-a {:mvn/version "2022.09.21"}}}}
                        {:library 'is.mad/shared-low-unchanged :path "shared/low-u" :edn {:paths ["src"] :deps {'is.mad/unrelated {:mvn/version "0.1"}}}}
                        {:library 'is.mad/server-mid-level-a :path "server/mid-a" :edn {:paths [] :deps {'is.mad/server-low-level-b {:mvn/version "2022.09.21"}
                                                                                                         'is.mad/first-to-update {:mvn/version "2022.01.20"}}}}]
        updated-library-name "is.mad/first-to-update"
        updated-library-version "2022.09.22"
        updated-deps [{:library 'is.mad/shared-low-level-a :path "shared/low-a" :edn {:paths ["src"] :deps {'is.mad/first-to-update {:mvn/version "2022.09.22"}}}}
                      {:library 'is.mad/server-low-level-b :path "server/low-b" :edn {:paths ["src"] :deps {'is.mad/shared-low-level-a {:mvn/version "2022.09.22"}}}}
                      {:library 'is.mad/server-mid-level-a :path "server/mid-a" :edn {:paths [] :deps {'is.mad/server-low-level-b {:mvn/version "2022.09.22"}
                                                                                                       'is.mad/first-to-update {:mvn/version "2022.09.22"}}}}]
        result (ulv/updated-deps updated-library-version updated-library-name candidate-deps)]
    (clojure.pprint/pprint result)
    (is (= updated-deps result))))

(defn add-deps-to-path
  "Helper to make folder path & create deps.edn in it (creating folders as
  necessary) with deps map"
  [root deps lib-name]
  (let [lib-path (str root "/" lib-name)
        deps-path (str lib-path "/deps.edn")]
    (fs/create-dir lib-path)
    (helpers/write-edn deps deps-path)
    deps-path))

(deftest update-deps-at-path-tests
  (let [library "is.mad/cljs-web3-next"
        new-version "2022.09.28"
        libs-root (str (fs/create-temp-dir {:prefix "update-deps-at-path-tests"}))
        deps-map {"first-lib" {:deps {(symbol library) {:mvn/version "2021.01.01"}}}
                  "second-depends-on-first" {:deps {'is.mad/first-lib {:mvn/version "2021.01.01"}}}}
        created-deps-paths (doall (map (fn [[lib-name deps]] (add-deps-to-path libs-root deps lib-name)) deps-map))
        result (doall (ulv/update-deps-at-path library new-version libs-root))]
    (is (= (helpers/read-edn (first result)) {:deps {'is.mad/first-lib {:mvn/version new-version}}}))))
