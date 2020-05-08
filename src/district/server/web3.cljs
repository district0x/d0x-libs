(ns district.server.web3
  (:require [cljs-web3-next.core :as web3-core]
            [cljs-web3-next.eth :as web3-eth]
            [cljs-web3-next.helpers :as web3-helpers]
            [clojure.string :as string]
            [cljs.core.async :as async :refer [<! go go-loop]]
            [district.shared.async-helpers :as async-helpers]
            [district.server.config :refer [config]]
            [mount.core :as mount :refer [defstate]]
            [taoensso.timbre :as log]))

(async-helpers/extend-promises-as-channels!)

(declare start)
(declare stop)
(declare ping-start)
(declare ping-stop)

(defonce ping (atom nil))

(defstate web3
  :start (start (merge (:web3 @config)
                       (:web3 (mount/args))))
  :stop (stop web3))

(defn websocket-connection? [uri]
  (string/starts-with? uri "ws"))

(defn create [{:keys [:host :port :url :client-config] :as opts}]
  (let [uri (if url
              url
              (str (or host "http://127.0.0.1") ":" port))]
    (if (websocket-connection? uri)
      (web3-core/websocket-provider uri {:client-config (merge {:max-received-frame-size 100000000
                                                                :max-received-message-size 100000000}
                                                               client-config)})
      (throw (js/Error. "web3 component needs a websocket connection!")))))

(defn- exponential-backoff
  "Retries reconnection attempts to a web3 node exponentially, increasing the waiting time between retries up to a maximum backoff time of 32 seconds
  after which every reconnection attempt is tried every 32 seconds + delta."
  [{:keys [on-offline on-online web3-opts]}]
  (let [maximum-backoff 32000
        backoff-rate 2]
    (on-offline)
    (ping-stop)
    (go-loop [backoff 1000]
      (let [new-web3 (create web3-opts)
            connected? (true? (<! (web3-eth/is-listening? new-web3)))]
        (log/info "Polling..." {:connected? connected? :backoff backoff})
        (if connected?
          (do
            (log/info "Reconnecting web3...")
            ;; swap websocket
            (web3-core/set-provider @web3 (web3-core/current-provider new-web3))
            (on-online))
          (do
            (<! (async/timeout backoff))
            (recur (if (>= backoff maximum-backoff)
                       backoff
                       (+ (* backoff backoff-rate) (rand-int 1000))))))))))

(defn- keep-alive [interval]
  (js/setInterval
   (fn []
     (go
       (let [connected? (true? (<! (web3-eth/is-listening? @web3)))]
         (if connected?
           (log/debug "ping" {:connected? connected?})
           (do
             (log/warn "ping error" {:connected? connected?})
             ;; NOTE: triggers exponential backoff
             (web3-core/disconnect @web3))))))
   interval))

(defn ping-start [{:keys [:ping-interval]
                   :or {ping-interval 10000}}]
  (reset! ping (keep-alive ping-interval)))

(defn ping-stop []
  (js/clearInterval @ping))

(defn start [{:keys [:port :url :client-config :on-online :on-offline]
              :as opts}]
  (let [this-web3 (create opts)]

    (when (and (not port) (not url))
      (throw (js/Error. "You must provide port or url to start the web3 component")))

    (web3-core/on-disconnect this-web3 (fn []
                                         (log/warn "web3 disconnected")
                                         (exponential-backoff {:on-offline on-offline :on-online on-online :web3-opts opts})))
    (web3-core/on-error this-web3 (fn [error]
                                    (log/error "web3 error" {:error error})
                                    (exponential-backoff {:on-offline on-offline :on-online on-online :web3-opts opts})))

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
  (ping-stop)
  (web3-core/disconnect @this))
