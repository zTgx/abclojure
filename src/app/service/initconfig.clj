;; load configure file, and init system config

(ns app.service.initconfig
  (:require 
    [io.pedestal.log :as log]
    [toml.core :as toml]))

(defn read-config []
  (slurp "./config/config.toml"))

(defn init
  [args]
  (log/info :msg (str "\nInit started...: " args))
  (let [cc (read-config)]
    ;; (log/info :msg cc)
    (println (:tendermint (toml/read cc :keywordize)))))
