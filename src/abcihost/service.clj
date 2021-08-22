(ns abcihost.service
  (:require [io.pedestal.http :as http]
            [io.pedestal.http.route :as route]
            [io.pedestal.http.body-params :as body-params]
            [ring.util.response :as ring-resp]

            ;; -- PROTOC-GEN-CLOJURE --
            [protojure.pedestal.core :as protojure.pedestal]
            [protojure.pedestal.routes :as proutes]
            ;; [com.example.addressbook.Greeter.server :as greeter]
            ;; [com.example.addressbook :as addressbook]
            [tendermint.abci.ABCIApplication.server :as server]
            [tendermint.abci :as abci]))

(defn home-page
  [request]
  (ring-resp/response "Hello from abcihost, backed by Protojure Template!"))
  
  (defn about-page 
      [request]
      (ring-resp/response (format "Clojure %s - served from %s"
                                  (clojure-version)
                                  (route/url-for ::about-page))))
              
;; -- PROTOC-GEN-CLOJURE --
;; Implement our "Greeter" service interface.  The compiler generates
;; a defprotocol (greeter/Service, in this case), and it is our job
;; to define an implementation of every function within it.  These will be
;; invoked whenever a request arrives, similarly to if we had defined
;; these functions as pedestal defhandlers.  The main difference is that
;; the :body returned in the response should correlate to the protobuf
;; return-type declared in the Service definition within the .proto
;;
;; Note that our GRPC parameters are associated with the request-map
;; as :grpc-params, similar to how the pedestal body-param module
;; injects other types, like :json-params, :edn-params, etc.
;;
;; see http://pedestal.io/reference/request-map

(defn mock-hash
  []
  (hash "1"))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; init chain
(defn init-chain
  [request-init-chain]
  (ring-resp/response {
    ;; :consensus-params
    :validators (22 33 44)
    :app-hash (byte-array 3)
  }))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; echo - info - query
(defn echo 
  [request-echo]
  (ring-resp/response {
    :message (:message request-echo)
    }))

(defn info 
  [request-info]
  (ring-resp/response {
    :data "data"
    :version "xxx--version"
    :app-version 3
    :last-block-height 7
    :last-block-app-hash (byte-array 11)
  }))
    
(defn query 
  []
  (ring-resp/response {
    :code 200
    :info "query info"
  }))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; check tx -> mempool
(defn check-tx 
  []
  (ring-resp/response {
    :code 200
    :data (byte-array 33)
    :log "check-tx log"
  }))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; begin -> deliver -> end -> commit
(defn begin-block
  [request-begin-block]
  ;; (println (str "begin block : " (apply str (map char (:hash request-begin-block)))))
  (println (str "last-commit-info : " (:last-commit-info request-begin-block)))
  (ring-resp/response {
    :events {
      :type "begin-block-type"
      :attributes {
        :key "begin-block-key"
        :value "begin-block-value"
        :index false
      }
    } 
  }))

(defn deliver-tx 
  []
  (ring-resp/response {
    :code 200
    :info "deliver tx info"
  }))

(defn end-block
  []
  (ring-resp/response {
    ;; :validator-updates 
    ;; :consensus-param-updates
    ;; :events 
  }))

(defn commit 
  []
  (ring-resp/response {
    :retain-height 3
    :data (byte-array 3)
  }))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; snapshot
(defn list-snapshots
  []
  (ring-resp/response {
    :snapshot {
      :height 7
      :format 3
      :chunks 3
      :hash (hash 2)
      :metadata (hash 3)
    }
  }))

(defn load-snapshot-chunk
  []
  (ring-resp/response {
    :chunk (byte-array 3)
  }))

(defn offer-snapshot
  []
  (ring-resp/response {
    :result 3
  }))

(defn apply-snapshot-chunk
  []
  (ring-resp/response {
    :refetch-chunks 3
  }))
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; flush
(defn abci-flush
  []
  (println "flushed, no return."))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; match protobuf Service
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
(deftype ABCIApplication []
  server/Service
  (Info
    [this params]
    (println (str "version: " (:version (:grpc-params params))))
    (info (:grpc-params params)))

  (Echo
    [this params]
    (println (str "request-echo-message " (:message (:grpc-params params))))
    (echo (:grpc-params params)))

  (Query
    [this params]
    (query (:grpc-paarams params)))   
    
  (InitChain
    [this params]
    (println (str "init chain " params))
    (init-chain (:grpc-params params)))

  (BeginBlock
    [this params]
    (begin-block (:grpc-params params)))

  (CheckTx
    [this params]
    (println (str "check-tx type : " (:type (:grpc-params params))))
    (check-tx (:grpc-params params)))
      
  (DeliverTx
    [this params]
    (deliver-tx (:grpc-params params)))  

  (EndBlock
    [this params]
    (end-block (:grpc-params params)))

  (Commit
    [this params]
    (commit (:grpc-params params)))     
      
  (ListSnapshots
    [this params]
    (list-snapshots (:grpc-params params)))     
    
  (LoadSnapshotChunk
    [this params]
    (load-snapshot-chunk (:grpc-params params)))

  (OfferSnapshot
    [this params]
    (offer-snapshot (:grpc-params params)))

  (ApplySnapshotChunk
    [this params]
    (apply-snapshot-chunk (:grpc-params params)))   
    
  (Flush
    [this params]
    (abci-flush (:grpc-params params))))  
     

;; Defines "/" and "/about" routes with their associated :get handlers.
;; The interceptors defined after the verb map (e.g., {:get home-page}
;; apply to / and its children (/about).
(def common-interceptors [(body-params/body-params) http/html-body])

;; Tabular routes
(def routes #{["/" :get (conj common-interceptors `home-page)]
              ["/about" :get (conj common-interceptors `about-page)]})

;; -- PROTOC-GEN-CLOJURE --
;; Add the routes produced by Greeter->routes
(def grpc-routes (reduce conj routes (proutes/->tablesyntax {:rpc-metadata server/rpc-metadata :interceptors common-interceptors :callback-context (ABCIApplication.)})))

(def service {:env :prod
              ::http/routes grpc-routes

              ;; -- PROTOC-GEN-CLOJURE --
              ;; We override the chain-provider with one provided by protojure.protobuf
              ;; and based on the Undertow webserver.  This provides the proper support
              ;; for HTTP/2 trailers, which GRPCs rely on.  A future version of pedestal
              ;; may provide this support, in which case we can go back to using
              ;; chain-providers from pedestal.
              ::http/type protojure.pedestal/config
              ::http/chain-provider protojure.pedestal/provider
              :io.pedestal.http/join :true
              ;;::http/host "localhost"
              ::http/port 26658})
