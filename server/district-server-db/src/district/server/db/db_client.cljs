(ns district.server.db.db-client)

(defprotocol DBClient
  (start [this args])
  (stop [this])
  (run! [this query values])
  (get [this query values])
  (all [this query values]))
