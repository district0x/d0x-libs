(ns district.server.web3
  (:require [cljs-web3-next.core :as web3-core]
            [cljs-web3-next.eth :as web3-eth]
            [cljs-web3-next.helpers :as web3-helpers]
            [clojure.string :as string]
            [district.server.config :refer [config]]
            [mount.core :as mount :refer [defstate]]
            [taoensso.timbre :as log]))

(declare start)
(declare stop)

(defstate web3
  :start (start (merge (:web3 @config)
                       (:web3 (mount/args))))
  :stop (stop web3))

(defn websocket-connection? [uri]
  (string/starts-with? uri "ws"))

(defn create [{:keys [:host :port :url] :as opts}]
  (let [uri (if url
              url
              (str (or host "http://127.0.0.1") ":" port))]
    (if (websocket-connection? uri)
      (web3-core/websocket-provider uri)
      (web3-core/http-provider uri))))

(defn start [{:keys [:port :url :on-online :on-offline :healthcheck-interval :polling-interval]
              :or {polling-interval 3000} :as opts}]
  (let [this-web3 (create opts)
        interval-id (atom nil)
        reset-connection (fn []
                           (on-offline)
                           (reset! interval-id (js/setInterval (fn []
                                                                 (let [new-web3 (create opts)]
                                                                   (web3-eth/is-listening? new-web3
                                                                                           (fn [error result]
                                                                                             (let [connected? (and (nil? error) result)]
                                                                                               (log/debug "Polling..." {:connected? connected?})
                                                                                               (when connected?
                                                                                                 (js/clearInterval @interval-id)
                                                                                                 ;; swap websocket
                                                                                                 (web3-core/set-provider @web3 (web3-core/current-provider new-web3))
                                                                                                 (on-online)))))))
                                                               polling-interval)))]

    (when (and (not port) (not url))
      (throw (js/Error. "You must provide port or url to start the web3 component")))

    (web3-core/on-disconnect this-web3 reset-connection)

    (web3-core/extend this-web3
      :evm
      ;; extending ganache defined json rpc calls
      [(web3-helpers/method {:name "increaseTime"
                             :call "evm_increaseTime"
                             :params 1})
       (web3-helpers/method {:name "mineBlock"
                             :call "evm_mine"})
       (web3-helpers/method {:name "snapshot"
                             :call "evm_snapshot"})
       (web3-helpers/method {:name "revert"
                             :call "evm_revert"})])))

(defn stop [this]
  (web3-core/disconnect @this))
