(defproject district0x/district-server-web3 "1.0.0"
  :description "district server module for setting up web3"
  :url "https://github.com/district0x/district-server-web3"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[cljs-web3 "0.19.0-0-8"]
                 [district0x/district-server-config "1.0.0"]
                 [mount "0.1.11"]
                 [org.clojure/clojurescript "1.9.946"]]

  :npm {:dependencies [[ganache-core "2.0.2"]
                       [web3 "0.19.0"]]})
