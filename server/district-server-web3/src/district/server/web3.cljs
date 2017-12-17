(ns district.server.web3
  (:require
    [cljs-web3.core :refer [http-provider]]
    [cljs.nodejs :as nodejs]
    [district.server.config :refer [config]]
    [mount.core :as mount :refer [defstate]]))

(declare start)
(declare stop)

(defstate web3
  :start (start (merge (:web3 @config)
                       (:web3 (mount/args))))
  :stop (stop web3))

(def Web3 (nodejs/require "web3"))
(def Ganache (nodejs/require "ganache-core"))

(set! js/Web3 Web3)

(defn start-ganache-server [{:keys [:port :ganache-opts]} callback]
  #_(let [server (.server Ganache (clj->js ganache-opts))
          listen (aget server "listen")]
      (listen port)
      server))

(defn start [{:keys [:port :url :start-ganache-server? :use-ganache-provider? :ganache-opts] :as opts}]
  (when (and (not port) (not url) (not use-ganache-provider?))
    (throw (js/Error. "You must provide port or url to web3 state component")))
  ;; Temporarily disabled. When synchronous calls bug gets solved we can switch to commented solution below
  ;; https://github.com/trufflesuite/ganache-core/issues/15
  #_(when (and port start-ganache-server?)
      (reset! *ganache-server-process* (start-ganache-server opts)))
  (let [provider (if false #_use-ganache-provider?
                   (.provider Ganache (clj->js ganache-opts))
                   (http-provider Web3 (if url url (str "http://127.0.0.1:" port))))]
    (new Web3 provider)))

(defn stop [web3]
  #_(when @*ganache-server*
      (let [close-server (aget @*ganache-server* "close")]
        (close-server))
      (reset! *ganache-server* nil)))

