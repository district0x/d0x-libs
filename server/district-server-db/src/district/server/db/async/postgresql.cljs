(ns district.server.db.async.postgresql
  (:require
    [district.server.db.db-client :as db-client]
    [cljs.core.async :refer [go <!]]
    [district.shared.async-helpers :refer [<? safe-go]]
    [district.server.config :refer [config]]
    [mount.core :as mount :refer [defstate]]
    ["pg" :refer [Client]]))

(declare start)
(declare stop)
(defstate ^{:on-reload :noop} db
          :start (start (merge (:db @config)
                               (:db (mount/args))))
          :stop (stop db))

(defrecord PostgreSQL []
  db-client/DBClient

  (start [this {:keys [:user :password :host :port :database] :as args}]
    (let [client (Client. #js {:user user
                  :password password
                  :host host
                  :port port
                  :database database})]
      (assoc this :js-client (-> (.connect client)
                                 (.then (fn [] client))))))

  (stop [this]
    (.end (:js-client this)))

  (run! [this query values]
    (safe-go
      (js->clj (.-rows (<? (.query (<! (:js-client this)) query values))))))

  (get [this query values]
    (safe-go
      (first (<? (db-client/all this query values)))))

  (all [this query values]
    (db-client/run! this query values)))

