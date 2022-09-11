(ns migrate-library-tests
  (:require [migrate-library :as ml]
            [clojure.test :refer [deftest testing is]]
            [babashka.fs :as fs]
            [clojure.java.shell :refer [sh]]))

; Run tests in VIM
; :nmap <leader>tr :VtrSendCommandToRunner ./bin_test/test_runner.clj<CR>

(defn count-commits [path & {:keys [branch]}]
  (-> (sh "git" "rev-list" (or branch "HEAD") "--count" :dir path)
      :out
      clojure.string/trim
      Integer/parseInt))

(defn split-by-space [string]
  (clojure.string/split string #"\s"))

(defn get-newest-file-in-path [path]
  (->> path
       str
       (sh "ls" "-t" ,,,)
       :out
       split-by-space
       first))

(defn file-set-in [path]
  (->> path
      fs/list-dir
      (map str)
      (map fs/file-name)
      (remove #{".git"})
      (into #{})))

(defn set-up-git-repo-in-tmp
  "Takes absolute path of .tar.gz archive that contains a Git repository.
   Returns absolute path of that archive extracted into system temp folder"
  [archive-path]
  (let [temp-dir (fs/create-temp-dir {:prefix "migrate-library-tests"})
        _extraction-result (sh "tar" "-xf" (str archive-path) "-C" (str temp-dir))
        newest-file (get-newest-file-in-path temp-dir)]
    (str temp-dir "/" newest-file)
    ))

(deftest migrate-library-tests
  (testing "checks library structure (deps.edn, shadow-cljs)"
    (is (false? (ml/library-structure-as-expected? "bin_test/fixtures/lib-no-deps")))
    (is (true? (ml/library-structure-as-expected? "bin_test/fixtures/lib-ok"))))

  (testing "moves selected library to subfolder with git history"
    (let [test-repo-path (set-up-git-repo-in-tmp "bin_test/fixtures/test-repo.tar.gz")
          container-repo-path (set-up-git-repo-in-tmp "bin_test/fixtures/container-repo.tar.gz")]
      (ml/move-merging-git-histories test-repo-path container-repo-path)
      (is (= 5 (count-commits container-repo-path))) ; 2 commits from container + 2 from test-repo + 1 merge
      (is (= (file-set-in (str container-repo-path "/test-repo")) (file-set-in test-repo-path)))))

  (testing "adding aliases to deps.edn"
    ()))
