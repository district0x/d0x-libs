(defproject district0x/district-ui-tx-button "1.0.0"
  :description "district UI reagent component for sending blockchain transactions."
  :url "https://github.com/district0x/district-ui-tx-button"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}

  :dependencies [[district0x/district-ui-web3-accounts "1.0.5"]
                 [org.clojure/clojurescript "1.9.946"]
                 [re-frame "0.10.5"]
                 [soda-ash "0.76.0"]]

  :exclusions [[cljsjs/react]
               [org.clojure/clojure]
               [org.clojure/clojurescript]]

  :npm {:devDependencies [[karma-chrome-launcher "2.2.0"]
                          [karma-cli "1.0.1"]
                          [karma-cljs-test "0.1.0"]
                          [karma "1.7.1"]]}

  :doo {:paths {:karma "./node_modules/karma/bin/karma"}}

  :clean-targets ^{:protect false} ["tests-output"]

  :profiles {:dev {:source-paths ["dev"]
                   :dependencies [[cljs-react-test "0.1.4-SNAPSHOT"]
                                  [cljsjs/react-with-addons "15.2.0-0"]
                                  [day8.re-frame/test "0.1.5"]
                                  [district0x/district-ui-reagent-render "1.0.0"]
                                  [district0x/district-ui-web3 "1.0.1"]
                                  [hickory "0.7.1"]
                                  [lein-doo "0.1.8"]
                                  [mount "0.1.11"]
                                  [org.clojure/clojure "1.8.0"]]
                   :plugins [[lein-cljsbuild "1.1.7"]
                             [lein-doo "0.1.8"]
                             [lein-npm "0.6.2"]]}}

  :cljsbuild {:builds [{:id "tests"
                        :source-paths ["src" "test"]
                        :compiler {:output-to "tests-output/tests.js"
                                   :output-dir "tests-output"
                                   :main "tests.runner"
                                   :optimizations :none}}]})
