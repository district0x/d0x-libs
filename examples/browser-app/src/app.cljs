(ns app
  (:require [goog.string :as gstring]
            [ipfs-tryout]))

; https://shadow-cljs.github.io/docs/UsersGuide.html#_lifecycle_hooks
(defn main [& args]
  (println  "Browser app starting")
  (try
    (apply ipfs-tryout/main args)
    (catch js/Error e
      (js/console.error "There was an error:" e))))

(defn ^:dev/after-load start []
  (js/console.log "start")
  (main))

(defn ^:dev/before-load stop []
  (js/console.log "stop"))
