(defproject district0x/district-server-web3 "1.2.4-SNAPSHOT"
  :description "district0x server module for setting up web3"
  :url "https://github.com/district0x/district-server-web3"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[cljs-web3-next "0.1.2"]
                 [com.taoensso/timbre "4.10.0"]
                 [district0x/async-helpers "0.1.3"]
                 [district0x/district-server-config "1.0.1"]
                 [mount "0.1.16"]
                 [org.clojure/clojurescript "1.10.520"]]

  :npm {:dependencies [[web3 "1.2.0"]]
        :devDependencies [[ws "2.0.1"]]}

  :profiles {:dev {:dependencies [[org.clojure/clojure "1.10.1"]
                                  [org.clojure/core.async "0.4.500"]]
                   :plugins [[lein-cljsbuild "1.1.7"]
                             [lein-npm "0.6.2"]
                             [lein-doo "0.1.8"]]
                   :source-paths ["src" "test"]}}

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
                  ["deploy"]]

  :cljsbuild {:builds [{:id "nodejs-tests"
                        :source-paths ["src" "test"]
                        :compiler {:output-to "tests-compiled/run-tests.js"
                                   :output-dir "tests-compiled"
                                   :main "tests.runner"
                                   :target :nodejs
                                   :optimizations :none
                                   :source-map true}}]})
