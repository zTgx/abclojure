# [abclojure](https://github.com/zTgx/abclojure)

# WIP

A Block Chain Application In Clojure Drived by Tendermint.

---

### How to run
#### first, run abclojure service 
```shell
lein run
```

#### second, start tendermint
```shell
tendermint init
```
```shell
tendermint node --home "./" --abci "grpc"
```
if all above commands are successd, abclojure is running now!

#### and last, let's play.(send a tx first)
```shell
curl -X POST -d '{"tx_id": "1", "echo": "hello"}' "http://localhost:9528"
```
will output a echo string:
```shell
hello, this tx is successed submit to server already.
```