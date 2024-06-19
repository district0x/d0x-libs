(ns district.server.db.async.postgresql
  (:require
    [cljs.core.async :refer [go <!]]
    [district.server.db.db-client :as db-client]
    [district.shared.async-helpers :refer [<? safe-go]]
    [district.server.config :refer [config]]
    ["pg" :refer [Client]]))

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

