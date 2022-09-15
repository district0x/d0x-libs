#!/usr/bin/env bb

(require '[babashka.process :as p])

(defn get-aliases []
  [":district-server-smart-contracts"]) ; FIXME: parse from deps.edn

(defn io-prepl []
  (let [aliases (clojure.string/join (get-aliases))
        cmd ["npx" "shadow-cljs" (str "-A" aliases) "watch" "server-repl"]
        proc (p/process cmd
                        {:inherit true
                         :shutdown p/destroy-tree})]
    proc))

@(io-prepl)
