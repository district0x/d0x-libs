(ns district.server.db.sqlite
  (:require
    [district.server.config :refer [config]]
    [district.server.db.db-client :as db-client]
    [district.server.db.honeysql-extensions :refer [load-sqlite-extensions]]
    ["better-sqlite3" :as Sqlite3Database]))

(defrecord SQLite []
  db-client/DBClient

  (start [this {:keys [:path :opts] :as args}]
    (load-sqlite-extensions)
    (assoc this :js-client (new Sqlite3Database path (clj->js opts))))

  (stop [this]
    (.close (:js-client this)))

  (run! [this query values]
    (js->clj (.run (.prepare (:js-client this) query) values) :keywordize-keys true))

  (get [this query values]
    (js->clj (.get (.prepare (:js-client this) query) values)))

  (all [this query values]
    (js->clj (.all (.prepare (:js-client this) query) values))))

