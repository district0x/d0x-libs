(ns update-library-versions-test
  (:require [update-library-versions :as ulv]
            [clojure.test :refer [deftest testing is]]
            [babashka.fs :as fs]
            [clojure.java.shell :refer [sh]]
            [script-helpers :as helpers]))

(def temp-prefix "d0x-libs-test")

(defn add-deps-to-path
  "Helper to make folder path & create deps.edn in it (creating folders as
  necessary) with deps map"
  [root deps lib-name]
  (let [lib-path (str root "/" lib-name)
        deps-path (str lib-path "/deps.edn")]
    (fs/create-dir lib-path)
    (helpers/write-edn deps deps-path)
    deps-path))

(deftest update-dependency-versions-tests
  (let [candidate-deps [{:library 'is.mad/shared-low-level-a :path "shared/low-a" :edn {:paths ["src"] :deps {'is.mad/first-to-update {:mvn/version "2022.09.20"}}}}
                        {:library 'is.mad/server-low-level-b :path "server/low-b" :edn {:paths ["src"] :deps {'is.mad/shared-low-level-a {:mvn/version "2022.09.21"}}}}
                        {:library 'is.mad/shared-low-unchanged :path "shared/low-u" :edn {:paths ["src"] :deps {'is.mad/unrelated {:mvn/version "0.1"}}}}
                        {:library 'is.mad/server-mid-level-a :path "server/mid-a" :edn {:paths [] :deps {'is.mad/server-low-level-b {:mvn/version "2022.09.21"}
                                                                                                         'is.mad/first-to-update {:mvn/version "2022.01.20"}}}}]
        updated-library-name "is.mad/first-to-update"
        updated-library-version "2022.09.22"
        expected-updated-deps [{:library 'is.mad/shared-low-level-a :path "shared/low-a" :edn {:paths ["src"] :deps {'is.mad/first-to-update {:mvn/version "2022.09.22"}}}}
                      {:library 'is.mad/server-low-level-b :path "server/low-b" :edn {:paths ["src"] :deps {'is.mad/shared-low-level-a {:mvn/version "2022.09.22"}}}}
                      {:library 'is.mad/server-mid-level-a :path "server/mid-a" :edn {:paths [] :deps {'is.mad/server-low-level-b {:mvn/version "2022.09.22"}
                                                                                                       'is.mad/first-to-update {:mvn/version "2022.09.22"}}}}]
        actual-updated-deps (ulv/updated-deps updated-library-version updated-library-name candidate-deps)]
    (is (= expected-updated-deps actual-updated-deps))))

(deftest update-deps-at-path-tests
  (let [library "is.mad/cljs-web3-next"
        new-version "2022.09.28"
        libs-root (str (fs/create-temp-dir {:prefix temp-prefix}))
        deps-map {"first-lib" {:deps {(symbol library) {:mvn/version "2021.01.01"}}}
                  "second-depends-on-first" {:deps {'is.mad/first-lib {:mvn/version "2021.01.01"}}}}
        created-deps-paths (doall (map (fn [[lib-name deps]] (add-deps-to-path libs-root deps lib-name)) deps-map))
        result (doall (ulv/update-deps-at-path library new-version libs-root :source-group-id (fn [x] "is.mad")))]
    (is (= (helpers/read-edn (first result)) {:deps {'is.mad/first-lib {:mvn/version new-version}}}))))

(deftest version-number-support-tests
  (testing "semversion variants"
    (is (= (ulv/version->numeric "1.2.3") [1 2 3]))
    (is (= (ulv/version->numeric "1.2.3-SNAPSHOT") [1 2 3]))
    (is (= (ulv/version->numeric "1.2") [0 1 2]))
    (is (= (ulv/version->numeric "1.2-TOWELDAY") [0 1 2]))))
