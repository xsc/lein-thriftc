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
  :thriftc {:path          "thrift"             ;; path to Thrift executable
            :source-paths  ["src/thrift"]       ;; paths to Thrift files
            :target-path   "target/thrift-java" ;; path for Java file generation
            :java-gen-opts "bean,hashcode"      ;; options for "--gen java:<options>"
            :force-compile false}               ;; true = do not check for changes
...
```

(The given values are the default ones.)

__Command Line__

```bash
$ lein thriftc
```

## License

```
The MIT License (MIT)

Copyright (c) 2013-2015 Yannick Scherer

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
```
