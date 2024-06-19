(ns district.server.db.async.sqlite
  (:require
    [cljs.core.async :refer [go <!]]
    [district.server.config :refer [config]]
    [district.server.db.db-client :as db-client]
    [district.server.db.honeysql-extensions :refer [load-sqlite-extensions]]
    [mount.core :as mount :refer [defstate]]
    ["sqlite3" :as sqlite3]
    ["sqlite" :refer [open]]))

(declare start)
(declare stop)
(defstate ^{:on-reload :noop} db
          :start (start (merge (:db @config)
                               (:db (mount/args))))
          :stop (stop db))

(defrecord SQLite []
  db-client/DBClient

  (start [this {:keys [:path :opts] :as args}]
    (load-sqlite-extensions)
    (assoc this :js-client (open #js {:filename path
                                      :driver sqlite3/Database})))

  (stop [this]
    (.close (:js-client this)))

  (run! [this query values]
    (go
      (js->clj (<! (.run (<! (:js-client this)) query values)))))

  (get [this query values]
    (go
      (js->clj (<! (.get (<! (:js-client this)) query values)))))

  (all [this query values]
    (go
      (js->clj (<! (.all (<! (:js-client this)) query values))))))

