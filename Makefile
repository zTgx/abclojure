all:
	protoc --clojure_out=grpc-server:src --proto_path=resources/ resources/tendermint/abci/types.proto
	protoc --clojure_out=src --proto_path=resources resources/tendermint/crypto/proof.proto resources/tendermint/crypto/keys.proto

	protoc --clojure_out=grpc-server:src --proto_path=resources/ resources/tendermint/types/block.proto resources/tendermint/types/types.proto resources/tendermint/types/params.proto
	# protoc --clojure_out=grpc-server:src --proto_path=resources/ resources/tendermint/types/canonical.proto
	# protoc --clojure_out=grpc-server:src --proto_path=resources/ resources/tendermint/types/events.proto
	# protoc --clojure_out=grpc-server:src --proto_path=resources/ resources/tendermint/types/evidence.proto
	# protoc --clojure_out=grpc-server:src --proto_path=resources/ resources/tendermint/types/validator.proto

	protoc --clojure_out=grpc-server:src --proto_path=resources/ resources/tendermint/version/types.proto
