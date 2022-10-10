#!/usr/bin/env bb

(ns run-tests
  (:require [clojure.java.shell :refer [sh with-sh-dir]]
            [clojure.string :refer [trim]]
            [babashka.fs :as fs]
            [clojure.edn :as edn]
            [clojure.pprint :as pp]
            [script-helpers :refer [log read-edn write-edn] :as helpers]))

(defn generate-test-run-config [libraries]
  (->> ["version: 2.1"
       "jobs:"
       "  test:"
       "    working_directory: ~/ci"
       "    docker:"
       "      # Primary container image where all steps run."
       "      - image: 487920318758.dkr.ecr.us-west-2.amazonaws.com/cljs-web3-ci:latest"
       "        aws_auth:"
       "            aws_access_key_id: $AWS_ACCESS_KEY_ID"
       "            aws_secret_access_key: $AWS_SECRET_ACCESS_KEY"
       "      # Secondary container image on common network."
       "      - image: trufflesuite/ganache-cli:latest"
       "        command: [-d, -m district0x, -p 8549, -l 8000000]"
       "    steps:"
       "      - checkout"
       "      - restore_cache:"
       "          name: Restore npm package cache"
       "          keys:"
       "            - npm-packages-{{ checksum \"yarn.lock\" }}"
       "      - run:"
       "          name: Install node modules"
       "          command: yarn install"
       "      - save_cache:"
       "          name: Save npm package cache"
       "          key: npm-packages-{{ checksum \"yarn.lock\" }}"
       "          paths:"
       "            - ./node_modules/"
       "      - run:"
       "          name: Deploy library's smart contracts"
       "          command: \"cd server/district-server-smart-contracts && npx truffle migrate --network ganache --reset\""
       "      - run:"
       "          name: Compile Node tests"
       "          command: clj -Ashadow:district-server-smart-contracts compile test-node"
       "      - run:"
       "          name: Run Node tests"
       "          command: node out/node-tests.js"
       "  deploy:"
       "    working_directory: ~/ci"
       "    docker:"
       "      - image: 487920318758.dkr.ecr.us-west-2.amazonaws.com/cljs-web3-ci:latest"
       "        aws_auth:"
       "            aws_access_key_id: $AWS_ACCESS_KEY_ID"
       "            aws_secret_access_key: $AWS_SECRET_ACCESS_KEY"
       "    steps:"
       "      - checkout"
       "      - run:"
       "          name: Build JAR"
       "          command: clojure -T:build jar"
       "      - run:"
       "          name: Release to clojars"
       "          command: clojure -T:build deploy"
       "workflows:"
       "  version: 2"
       "  test_and_deploy:"
       "    jobs:"
       "      - test"
       "      - deploy:"
       "          requires:"
       "            - test"
       "          filters:"
       "            branches:"
       "              only: master"
       ""]
       (clojure.string/join "\n")))

(defn -main [& args]
  (let [continuation-filename (first args)
        release-config (first (helpers/read-edn "./releases.edn"))
        libraries (:libs release-config)
        generated-config (generate-test-run-config libraries)]
    (log "Generating dynamic config for CircleCI continuation")
    (log generated-config)
    (spit continuation-filename generated-config)))

; To allow running as commandn line util but also required & used in other programs or REPL
; https://book.babashka.org/#main_file
(when (= *file* (System/getProperty "babashka.file"))
  (apply -main *command-line-args*))
