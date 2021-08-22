# How To Test

## Simple principles for writing unit tests
* One test namespace for each src namespace
* One deftest function for each function under test
* Multiple is assertions for one function
* Group assertions in testing and provide a meaningful description of that grouping, adding more information when reviewing test failures especially for larger code bases.
* `are for testing similar functionality with different data sets
* Test private functions (or don't use them) through public functions of each namespace (minimize test churn and time to run all tests)
* Use generative testing to create less code and yet test with more extensive range of data
* Use test selectors to organize tests and optimize speed of test runs
* As with file names, the namespaces for each test code file is the same as the source code it is testing, with a -test postfix.

---

## Run test

```clojure
lein test
```

