(ns district.server.db
  (:refer-clojure :exclude [get run!])
  (:require
    [clojure.string :as string]
    [district.server.config :refer [config]]
    [district.server.db.honeysql-extensions]
    [district.server.db.db-client :as db-client]
    [district.server.db.utils :as utils]
    [district.server.db.sqlite :as sqlite]
    [honeysql-postgres.format]
    [honeysql.core :as sql]
    [honeysql.format :as sql-format]
    [mount.core :as mount :refer [defstate]]))

(declare start)
(declare stop)
(defstate ^{:on-reload :noop} db
  :start (start (merge (:db @config)
                       (:db (mount/args))))
  :stop (stop db))

(def ^:dynamic *transform-result-keys-fn* (atom nil))

(defn start [{:keys [:path :opts :transform-result-keys-fn :sql-name-transform-fn]
              :as args
              :or {path ":memory:"
                   sql-name-transform-fn (comp #(string/replace % "_STAR_" "*")
                                               #(string/replace % "_PERCENT_" "%")
                                               munge)
                   transform-result-keys-fn (comp keyword demunge)}}]

  (set! *transform-result-keys-fn* transform-result-keys-fn)
  (set! sql-format/*name-transform-fn* sql-name-transform-fn)

  (let [client (sqlite/->SQLite)]
    (db-client/start client args)))


(defn stop [db]
  (db-client/stop @db))


(defn- map-keys [f m]
  (into {} (map (fn [[k v]] [(f k) v]) m)))


(defn run! [sql-map & [{:keys [:format-opts]}]]
  (let [[query & values] (apply sql/format sql-map (reduce into [] format-opts))]
    (db-client/run! @db query (clj->js (or values [])))))


(defn get [sql-map & [{:keys [:format-opts]}]]
  (let [[query & values] (apply sql/format sql-map (reduce into [] format-opts))]
    (map-keys *transform-result-keys-fn* (db-client/get @db query (clj->js (or values []))))))


(defn all [sql-map & [{:keys [:format-opts]}]]
  (let [[query & values] (apply sql/format sql-map (reduce into [] format-opts))]
    (map (partial map-keys *transform-result-keys-fn*)
         (db-client/all @db query (clj->js (or values []))))))


(defn total-count [sql-map & [opts]]
  (second (first (get (utils/total-count-query sql-map opts)))))
