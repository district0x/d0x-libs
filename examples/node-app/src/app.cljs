(ns app
  (:require [goog.string :as gstring]
            [ipfs-tryout]))

(defn exit-gracefully [message]
  (println message " Exiting.")
  (.exit js/process 0))

(.on js/process "SIGTERM" (partial exit-gracefully "Received SIGTERM"))
(.on js/process "SIGINT" (partial exit-gracefully "Received SIGINT"))

(defn main [& args]
  (println (gstring/format "Node app running on PID: %s" (. js/process -pid)))
  (apply ipfs-tryout/main args))
