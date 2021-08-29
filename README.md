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

---

### License

Copyright 2021 zTgx

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.