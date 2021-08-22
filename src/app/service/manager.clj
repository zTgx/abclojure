(ns app.service.manager
  (:require [io.pedestal.http :as server]
            [io.pedestal.http.route :as route]
            [app.service.abcihost :as abcihost]
            [app.service.query :as query]
            [app.service.submit :as submit]
            [app.service.pubsub :as pubsub]))

(defonce run-service-abcihost
  (-> abcihost/service ;; start with production configuration
    (merge {:env :prod
            ;; do not block thread that starts web server
            ::server/join? false
            ;; Routes can be a function that resolve routes,
            ;;  we can use this to set the routes to be reloadable
            ::server/routes #(route/expand-routes (deref #'abcihost/grpc-routes)) ;; -- PROTOC-GEN-CLOJURE -- update route
            ;; all origins are allowed in dev mode
            ::server/allowed-origins {:creds true :allowed-origins (constantly true)}
            ;; Content Security Policy (CSP) is mostly turned off in dev mode
            ::server/secure-headers {:content-security-policy-settings {:object-src "none"}}})
    ;; Wire up interceptor chains
    server/default-interceptors
    server/dev-interceptors
    server/create-server
    server/start))

(defonce run-service-query
  (-> query/service ;; start with production configuration
    (merge {:env :prod
            ;; do not block thread that starts web server
            ::server/join? false
            ;; Routes can be a function that resolve routes,
            ;;  we can use this to set the routes to be reloadable
            ::server/routes #(route/expand-routes (deref #'query/routes)) ;; -- PROTOC-GEN-CLOJURE -- update route
            ;; all origins are allowed in dev mode
            ::server/allowed-origins {:creds true :allowed-origins (constantly true)}
            ;; Content Security Policy (CSP) is mostly turned off in dev mode
            ::server/secure-headers {:content-security-policy-settings {:object-src "none"}}})
    ;; Wire up interceptor chains
    server/default-interceptors
    server/dev-interceptors
    server/create-server
    server/start))

(defonce run-service-pubsub
  (-> pubsub/service ;; start with production configuration
    (merge {:env :prod
            ;; do not block thread that starts web server
            ::server/join? false
            ;; Routes can be a function that resolve routes,
            ;;  we can use this to set the routes to be reloadable
            ::server/routes #(route/expand-routes (deref #'pubsub/routes)) ;; -- PROTOC-GEN-CLOJURE -- update route
            ;; all origins are allowed in dev mode
            ::server/allowed-origins {:creds true :allowed-origins (constantly true)}
            ;; Content Security Policy (CSP) is mostly turned off in dev mode
            ::server/secure-headers {:content-security-policy-settings {:object-src "none"}}})
    ;; Wire up interceptor chains
    server/default-interceptors
    server/dev-interceptors
    server/create-server
    server/start))
  
(defonce run-service-submit
  (-> submit/service ;; start with production configuration
    (merge {:env :prod
            ;; do not block thread that starts web server
            ::server/join? true
            ;; Routes can be a function that resolve routes,
            ;;  we can use this to set the routes to be reloadable
            ::server/routes #(route/expand-routes (deref #'submit/routes)) ;; -- PROTOC-GEN-CLOJURE -- update route
            ;; all origins are allowed in dev mode
            ::server/allowed-origins {:creds true :allowed-origins (constantly true)}
            ;; Content Security Policy (CSP) is mostly turned off in dev mode
            ::server/secure-headers {:content-security-policy-settings {:object-src "none"}}})
    ;; Wire up interceptor chains
    server/default-interceptors
    server/dev-interceptors
    server/create-server
    server/start))

(defn start 
  [& args]
  (println "\nCreating your server...")
  (run-service-abcihost args)
  (run-service-query args)
  (run-service-pubsub args)
  (run-service-submit args)
  )
