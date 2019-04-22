(defproject district0x/district-server-web3-events "1.0.0"
  :description "district0x server module for handling web3 events"
  :url "https://github.com/district0x/district-server-web3-events"
  :license {:name "EPL-2.0 OR GPL-2.0-or-later WITH Classpath-exception-2.0"
            :url "https://www.eclipse.org/legal/epl-2.0/"}
  :dependencies [[cljs-web3 "0.19.0-0-11"]
                 [com.taoensso/encore "2.92.0"]
                 [com.taoensso/timbre "4.10.0"]
                 [district0x/district-server-config "1.0.1"]
                 [district0x/district-server-logging "1.0.5"]
                 [district0x/district-server-smart-contracts "1.0.14"]
                 [district0x/district-server-web3 "1.0.1"]
                 [medley "1.0.0"]
                 [mount "0.1.11"]
                 [org.clojure/clojurescript "1.10.439"]]

  :plugins [[lein-npm "0.6.2"]
            [lein-doo "0.1.8"]
            [lein-solc "1.0.11"]]

  :npm {:devDependencies [[ws "2.0.1"]
                          ["@sentry/node" "4.2.1"]
                          [chalk "2.3.0"]]}

  :solc {:src-path "resources/public/contracts/src"
         :build-path "resources/public/contracts/build"
         :abi? false
         :bin? false
         :truffle-artifacts? true
         :contracts :all}

  :source-paths ["src"]

  :clean-targets ^{:protect false} ["target" "tests-compiled"]

  :profiles {:dev {:dependencies [[com.cemerick/piggieback "0.2.2"]
                                  [figwheel-sidecar "0.5.16"]
                                  [lein-doo "0.1.8"]
                                  [org.clojure/clojure "1.9.0"]
                                  [org.clojure/tools.reader "1.3.0"]]
                   :source-paths ["dev" "src"]
                   :resource-paths ["resources"]}}

  :deploy-repositories [["snapshots" {:url "https://clojars.org/repo"
                                      :username :env/clojars_username
                                      :password :env/clojars_password
                                      :sign-releases false}]
                        ["releases"  {:url "https://clojars.org/repo"
                                      :username :env/clojars_username
                                      :password :env/clojars_password
                                      :sign-releases false}]]

  :release-tasks [["change" "version" "leiningen.release/bump-version" "release"]
                  ["deploy"]]

  :cljsbuild {:builds [{:id "tests"
                        :source-paths ["src" "test"]
                        :compiler {:main "tests.runner"
                                   :output-to "tests-compiled/run-tests.js"
                                   :output-dir "tests-compiled"
                                   :target :nodejs
                                   :optimizations :none
                                   :source-map true}}]})
