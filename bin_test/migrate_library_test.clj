(ns migrate-library-test
  (:require [migrate-library :as ml]
            [clojure.test :refer [deftest testing is]]
            [babashka.fs :as fs]
            [clojure.java.shell :refer [sh]]
            [script-helpers :as helpers]))

; Run tests in VIM
; :nmap <leader>tr :VtrSendCommandToRunner ./bin_test/test_runner.clj<CR>

(def temp-prefix "d0x-libs-test")

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
    (let [temp-path (fs/create-temp-dir {:prefix temp-prefix})
          test-repo-path (set-up-git-repo "bin_test/fixtures/test-repo.tar.gz" temp-path)
          container-repo-path (set-up-git-repo "bin_test/fixtures/container-repo.tar.gz" temp-path)]
      (ml/move-merging-git-histories test-repo-path container-repo-path "shared" :create-new-branch? true)
      (is (= 5 (count-commits container-repo-path))) ; 2 commits from container + 2 from test-repo + 1 merge
      (is (= (file-set-in (str container-repo-path "/shared/test-repo")) (file-set-in test-repo-path))))))
