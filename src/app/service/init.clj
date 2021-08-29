;; load configure file, and init system config

(ns app.service.init
  (:import (org.apache.tuweni.toml Toml)
    (java.nio.file Paths)))

;; (defn init-config
;;   [path]
;;   (let [result (toml/Toml/parse path)]
;;     (println result)))

(defn init
  []
  (println "\nInit started...")
  (println (Paths/get "./config/config.toml"))

  ;; (init-config (toml/Paths "./config/config.toml"))
  )