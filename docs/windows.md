### How to close abci host on windows platform

#### lookup 26658
```shell
netstat -aon|findstr "26658"
```

#### kill pid
```shell
taskkill /T /F /PID [pid]
```