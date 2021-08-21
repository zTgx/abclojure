(ns abcihost.query
    (:require [io.pedestal.http :as http]
              [io.pedestal.http.route :as route]
              [io.pedestal.http.body-params :as body-params]
              [ring.util.response :as ring-resp]
  
              ;; -- PROTOC-GEN-CLOJURE --
              [protojure.pedestal.core :as protojure.pedestal]
              [protojure.pedestal.routes :as proutes]))

(defn home-page
[request]
(ring-resp/response "Hello from abcihost, backed by Protojure Template!"))

(defn about-page 
    [request]
    (ring-resp/response (format "Clojure %s - served from %s"
                                (clojure-version)
                                (route/url-for ::about-page))))
  
(def common-interceptors [(body-params/body-params) http/html-body])

(def routes #{["/" :get (conj common-interceptors `home-page)]
    ["/about" :get (conj common-interceptors `about-page)]})

(def service {:env :prod
    ::http/routes routes

    ;; -- PROTOC-GEN-CLOJURE --
    ;; We override the chain-provider with one provided by protojure.protobuf
    ;; and based on the Undertow webserver.  This provides the proper support
    ;; for HTTP/2 trailers, which GRPCs rely on.  A future version of pedestal
    ;; may provide this support, in which case we can go back to using
    ;; chain-providers from pedestal.
    ;; ::http/type protojure.pedestal/config
    ;; ::http/chain-provider protojure.pedestal/provider
    ::http/type :jetty
    :io.pedestal.http/join :false
    ;; ::http/host "localhost"
    ::http/port 9527})