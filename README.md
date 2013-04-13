# lein-thriftc

[Apache Thrift](http://thrift.apache.org/) Plugin for Leiningen.

## Usage

__lein-thriftc__ is available via [Clojars](https://clojars.org/lein-thriftc).

### project.clj


```clojure
  :plugins [[lein-thriftc "0.1.0"] ...]
  :prep-tasks ["thriftc" ...]
```

This will run the plugin automatically. (If you don't want that, leave out the `:prep-tasks` line.)
You can customize its behaviour by adding a map of options to your `project.clj` using the `:thrift`
key:

```clojure
...
  :thriftc { :path          "thrift"        ;; path to Thrift executable
             :source-paths  ["src/thrift"]  ;; paths to Thrift files
             :java-gen-opts "bean,hashcode" ;; options for "--gen java:<options>"
             :javac-opts    []              ;; options for javac
             :force-compile false }         ;; do not check for changes
...
```

(The given values are the default ones.)

### Command Line

```
lein thriftc [:verbose]
```

This runs the plugin manually based on the information given in `project.clj`. `:verbose`
results in a few log messages.

## License

Copyright &copy; 2013 Yannick Scherer

Distributed under the Eclipse Public License, the same as Clojure.
