(defproject district0x/district-ui-web3-tx-log "1.0.9"
  :description "district UI module providing web3 transaction log"
  :url "https://github.com/district0x/district-ui-web3-tx-log"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}

  :dependencies [[akiroz.re-frame/storage "0.1.2"]
                 [day8.re-frame/forward-events-fx "0.0.5"]
                 [district0x/district-format "1.0.5"]
                 [district0x/district-ui-router "1.0.5"]
                 [district0x/district-ui-web3-accounts "1.0.5"]
                 [district0x/district-ui-web3-tx-costs "1.0.3"]
                 [district0x/district-ui-web3-tx-log-core "1.0.3"]
                 [district0x/district-web3-utils "1.0.0"]
                 [district0x/re-frame-spec-interceptors "1.0.1"]
                 [madvas/cemerick-url "0.1.2"]
                 [mount "0.1.11"]
                 [org.clojure/clojurescript "1.9.946"]
                 [re-frame "0.10.2"]
                 [soda-ash "0.76.0"]]

  :doo {:paths {:karma "./node_modules/karma/bin/karma"}}

  :npm {:devDependencies [[karma "1.7.1"]
                          [karma-chrome-launcher "2.2.0"]
                          [karma-cli "1.0.1"]
                          [karma-cljs-test "0.1.0"]]}

  :profiles {:dev {:dependencies [[cljsjs/react "15.6.1-2"]
                                  [cljsjs/react-dom "15.6.1-2"]
                                  [day8.re-frame/test "0.1.5"]
                                  [district0x/district-ui-smart-contracts "1.0.5"]
                                  [district0x/district-ui-web3-tx "1.0.8"]
                                  [org.clojure/clojure "1.8.0"]
                                  [org.clojure/tools.nrepl "0.2.13"]]
                   :plugins [[lein-cljsbuild "1.1.7"]
                             [lein-doo "0.1.8"]
                             [lein-npm "0.6.2"]]}}

  :cljsbuild {:builds [{:id "tests"
                        :source-paths ["src" "test"]
                        :compiler {:output-to "tests-output/tests.js"
                                   :output-dir "tests-output"
                                   :main "tests.runner"
                                   :optimizations :none}}]}

  :deploy-repositories [["snapshots" {:url "https://clojars.org/repo"
                                      :username :env/clojars_username
                                      :password :env/clojars_password
                                      :sign-releases false}]
                        ["releases"  {:url "https://clojars.org/repo"
                                      :username :env/clojars_username
                                      :password :env/clojars_password
                                      :sign-releases false}]]

  :release-tasks [["vcs" "assert-committed"]
                  ["change" "version" "leiningen.release/bump-version" "release"]
                  ["deploy"]])
