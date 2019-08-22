(defproject district0x/district-sendgrid "1.0.1-SNAPSHOT"
  :description "Helper functions for sending emails via Sendgrid API"
  :url "https://github.com/district0x/district-sendgrid"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[cljs-ajax "0.7.2"]
                 [org.clojure/clojurescript "1.10.439"]]
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
