# lein-thriftc

[Apache Thrift](http://thrift.apache.org/) Plugin for Leiningen.

[![endorse](https://api.coderwall.com/xsc/endorsecount.png)](https://coderwall.com/xsc)

## Requirements

- Apache Thrift [tested with versions >= 0.9.0]
- [Leiningen](https://github.com/technomancy/leiningen) [tested with versions >= 2.0.0]

## Usage

__Leiningen__ ([via Clojars](https://clojars.org/lein-thriftc))

[![Clojars Project](http://clojars.org/lein-thriftc/latest-version.svg)](http://clojars.org/lein-thriftc)

Add the artifact to the `:plugins` vector of your `project.clj`. You can make it run automatically on
tasks like `jar` or `repl` using the built-in hook:

```clojure
...
  :hooks [leiningen.thriftc]
...
```

You can customize lein-thrift's behaviour by adding a map of options to your `project.clj` using the
`:thriftc` key:

```clojure
...
  :thriftc {:path          "thrift"        ;; path to Thrift executable
            :source-paths  ["src/thrift"]  ;; paths to Thrift files
            :java-gen-opts "bean,hashcode" ;; options for "--gen java:<options>"
            :force-compile false}          ;; true = do not check for changes
...
```

(The given values are the default ones.)

__Command Line__

```bash
$ lein thriftc
```

## License

Copyright &copy; 2013-2014 Yannick Scherer

Distributed under the Eclipse Public License, the same as Clojure.
