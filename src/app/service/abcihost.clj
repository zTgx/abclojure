;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;
;;; abcihost - service : tendermint abci host api service
;;; default port: 26658
;;; accept tendermint abci callbacks, including echo/info/check-tx/begin-block/deliver-tx/end-block/commit, etc
;;;
;;; ABCI methods are split across four separate ABCI connections:
;;; - Consensus connection: InitChain, BeginBlock, DeliverTx, EndBlock, Commit
;;; - Mempool connection: CheckTx
;;; - Info connection: Info, Query
;;; - Snapshot connection: ListSnapshots, LoadSnapshotChunk, OfferSnapshot, ApplySnapshotChunk
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(ns app.service.abcihost
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

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; keep a Tabular route 
(defn route-test 
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

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; init chain
;;
;; Usage:
;;
;; Called once upon genesis.
;; If ResponseInitChain.Validators is empty, the initial validator set will be the RequestInitChain.Validators
;; If ResponseInitChain.Validators is not empty, it will be the initial validator set (regardless of what is in RequestInitChain.Validators).
;; This allows the app to decide if it wants to accept the initial validator set proposed by tendermint (ie. in the genesis file), or if it wants to use a different one (perhaps computed based on some application specific information in the genesis file).
;;
(defn init-chain
  [request-init-chain]
  (ring-resp/response {
    ;; :consensus-params
    :validators (22 33 44)
    :app-hash (byte-array 3)
  }))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; echo - info - query
;;
;; Usage:
;;
;; Echo a string to test an abci client/server implementation
;;
(defn echo 
  [request-echo]
  (ring-resp/response {
    :message (:message request-echo)
    }))

;;
;;Usage:
;;
;; Return information about the application state.
;; Used to sync Tendermint with the application during a handshake that happens on startup.
;; The returned app_version will be included in the Header of every block.
;; Tendermint expects last_block_app_hash and last_block_height to be updated during Commit, ensuring that Commit is never called twice for the same block height.
;;
(defn info 
  [request-info]
  (ring-resp/response {
    :data "data"
    :version "xxx--version"
    :app-version 3
    :last-block-height 7
    :last-block-app-hash (byte-array 11)
  }))
    
;;
;; Usage:
;;
;; Query for data from the application at current or past height.
;; Optionally return Merkle proof.
;; Merkle proof includes self-describing type field to support many types of Merkle trees and encoding formats.
;;
(defn query 
  []
  (ring-resp/response {
    :code 200
    :info "query info"
  }))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; check tx -> mempool
;;
;; Usage:
;;
;; Technically optional - not involved in processing blocks.
;; Guardian of the mempool: every node runs CheckTx before letting a transaction into its local mempool.
;; The transaction may come from an external user or another node
;; CheckTx need not execute the transaction in full, but rather a light-weight yet stateful validation, like checking signatures and account balances, but not running code in a virtual machine.
;; Transactions where ResponseCheckTx.Code != 0 will be rejected - they will not be broadcast to other nodes or included in a proposal block.
;; Tendermint attributes no other value to the response code
;;
(defn check-tx 
  []
  (ring-resp/response {
    :code 200
    :data (byte-array 33)
    :log "check-tx log"
  }))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; begin -> deliver -> end -> commit
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;
;; Usage:
;;
;; Signals the beginning of a new block. Called prior to any DeliverTxs.
;; The header contains the height, timestamp, and more - it exactly matches the Tendermint block header. We may seek to generalize this in the future.
;; The LastCommitInfo and ByzantineValidators can be used to determine rewards and punishments for the validators. NOTE validators here do not include pubkeys.
;;
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

;;
;;Usage:
;;
;; The workhorse of the application - non-optional.
;; Execute the transaction in full.
;; ResponseDeliverTx.Code == 0 only if the transaction is fully valid.
;;
(defn deliver-tx 
  []
  (ring-resp/response {
    :code 200
    :info "deliver tx info"
  }))

;;  Usage:
;;
;;  Signals the end of a block.
;;  Called after all transactions, prior to each Commit.
;;  Validator updates returned by block H impact blocks H+1, H+2, and H+3, but only effects changes on the validator set of H+2:
;;  H+1: NextValidatorsHash
;;  H+2: ValidatorsHash (and thus the validator set)
;;  H+3: LastCommitInfo (ie. the last validator set)
;;  Consensus params returned for block H apply for block H+1
;;
(defn end-block
  []
  (ring-resp/response {
    ;; :validator-updates 
    ;; :consensus-param-updates
    ;; :events 
  }))

;;
;;  Usage:
;;
;;  Persist the application state.
;;  Return an (optional) Merkle root hash of the application state
;;  ResponseCommit.Data is included as the Header.AppHash in the next block
;;  it may be empty
;;  Later calls to Query can return proofs about the application state anchored in this Merkle root hash
;;  Note developers can return whatever they want here (could be nothing, or a constant string, etc.), so long as it is deterministic - it must not be a function of anything that did not come from the BeginBlock/DeliverTx/EndBlock methods.
;;  Use RetainHeight with caution! If all nodes in the network remove historical blocks then this data is permanently lost, and no new nodes will be able to join the network and bootstrap. Historical blocks may also be required for other purposes, e.g. auditing, replay of non-persisted heights, light client verification, and so on.
;;
(defn commit 
  []
  (ring-resp/response {
    :retain-height 3
    :data (byte-array 3)
  }))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; snapshot

;;
;; Usage:
;;
;; Used during state sync to discover available snapshots on peers.
;; See Snapshot data type for details.
;;
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

;;
;; Usage:
;;
;; Used during state sync to retrieve snapshot chunks from peers.
;;
(defn load-snapshot-chunk
  []
  (ring-resp/response {
    :chunk (byte-array 3)
  }))

;;
;; Usage:
;;
;; OfferSnapshot is called when bootstrapping a node using state sync. The application may accept or reject snapshots as appropriate. Upon accepting, Tendermint will retrieve and apply snapshot chunks via ApplySnapshotChunk. The application may also choose to reject a snapshot in the chunk response, in which case it should be prepared to accept further OfferSnapshot calls.
;; Only AppHash can be trusted, as it has been verified by the light client. Any other data can be spoofed by adversaries, so applications should employ additional verification schemes to avoid denial-of-service attacks. The verified AppHash is automatically checked against the restored application at the end of snapshot restoration.
;;
(defn offer-snapshot
  []
  (ring-resp/response {
    :result 3
  }))

;;
;; Usage:
;;
;; The application can choose to refetch chunks and/or ban P2P peers as appropriate. Tendermint will not do this unless instructed by the application.
;; The application may want to verify each chunk, e.g. by attaching chunk hashes in Snapshot.Metadata and/or incrementally verifying contents against AppHash.
;; When all chunks have been accepted, Tendermint will make an ABCI Info call to verify that LastBlockAppHash and LastBlockHeight matches the expected values, and record the AppVersion in the node state. It then switches to fast sync or consensus and joins the network.
;; If Tendermint is unable to retrieve the next chunk after some time (e.g. because no suitable peers are available), it will reject the snapshot and try a different one via OfferSnapshot. The application should be prepared to reset and accept it or abort as appropriate.
;;
(defn apply-snapshot-chunk
  []
  (ring-resp/response {
    :refetch-chunks 3
  }))
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; flush

;;
;; Usage:
;;
;; Signals that messages queued on the client should be flushed to the server. It is called periodically by the client implementation to ensure asynchronous requests are actually sent, and is called immediately to make a synchronous request, which returns when the Flush response comes back.
;;
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
    (echo (-> params :grpc-params)))
    ;; (echo (:grpc-params params)))

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
(def routes #{["/test" :get (conj common-interceptors `route-test)]})

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
