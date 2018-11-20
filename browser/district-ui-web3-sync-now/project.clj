(defproject district0x/district-ui-web3-sync-now "1.0.3-2"
  :description "district0x UI module for syncing time between the blockchain and the frontend."
  :url "https://github.com/district0x/"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}

  :dependencies [[cljs-web3 "0.19.0-0-11"]
                 [district0x.re-frame/web3-fx "1.0.4"]
                 [district0x/district-ui-logging "1.0.3"]
                 [district0x/district-ui-now "1.0.2"]
                 [district0x/district-ui-web3 "1.0.1"]
                 [district0x/district-web3-utils "1.0.2"]
                 [day8.re-frame/forward-events-fx "0.0.5"]
                 [mount "0.1.11"]
                 [org.clojure/clojurescript "1.9.946"]
                 [re-frame "0.10.5"]]

  :exclusions [[org.clojure/clojure]
               [org.clojure/clojurescript]]

  :plugins [[lein-npm "0.6.2"]
            [lein-figwheel "0.5.14"]]

  :npm {:devDependencies [[karma "1.7.1"]
                          [karma-chrome-launcher "2.2.0"]
                          [karma-cli "1.0.1"]
                          [karma-cljs-test "0.1.0"]]}

  :doo {:paths {:karma "./node_modules/karma/bin/karma"}}

  :clean-targets ^{:protect false} ["target" "tests-output"]

  :profiles {:dev {:dependencies [[com.andrewmcveigh/cljs-time "0.5.2"]
                                  [day8.re-frame/test "0.1.5"]
                                  [org.clojure/clojure "1.8.0"]
                                  [lein-doo "0.1.8"]]
                   :plugins [[lein-cljsbuild "1.1.7"]
                             [lein-doo "0.1.8"]]}}

  :cljsbuild {:builds [{:id "tests"
                        :source-paths ["src" "test"]
                        :compiler {:output-to "tests-output/tests.js"
                                   :output-dir "tests-output"
                                   :main "tests.runner"
                                   :optimizations :none}}]})
