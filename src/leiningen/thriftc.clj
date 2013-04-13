(ns ^{ :doc "A Thrift Plugin for Leiningen"
       :author "Yannick Scherer" }
  leiningen.thriftc
  (:require [robert.hooke]
            [leiningen.javac :only [javac]]
            [leiningen.core.main :as main]
            [clojure.tools.reader.edn :as edn :only [read-string]]
            [clojure.java.io :as io]
            [clojure.java.shell :as shell])
  (:import java.io.File))

;; ## Print

(def ^:private ^:dynamic *verbose* nil)

(defn- verbose*
  "Print Log Output."
  [& msg]
  (when *verbose*
    (print (apply str msg))
    (.flush *out*)))

(defn- verbose
  "Print Log Output with Linefeed."
  [& msg]
  (when *verbose*
    (println (apply str msg))
    (.flush *out*)))

;; ## Helpers

(defn- read-edn
  "Read EDN file."
  [target-file]
  (let [f (io/file target-file)]
    (when (.exists f)
      (edn/read-string (slurp f)))))

(defn- thrift-sources
  "Find Thrift Sources in the given directories."
  [dirs ]
  (mapcat
    (fn [dir]
      (let [^File f (io/file dir)]
        (filter #(-> ^File % (.getName) (.endsWith ".thrift")) (file-seq f))))
    dirs))

(defn- check-thrift-available
  "Check if the given Thrift executable is available."
  [thrift-executable]
  (try
    (do
      (shell/sh thrift-executable "-version")
      true)
    (catch Exception _ nil)))

(defn- compile-thrift
  [thrift out-dir thrift-java-opts src-file]
  (try
    (let [{:keys[exit out err]} (shell/sh 
                                  thrift "-r"
                                  "--gen" "java" #_(str "java:" thrift-java-opts) 
                                  "-out" out-dir
                                  src-file)]
      (when-not (zero? exit)
        (verbose)
        (println (str "Could not compile `" src-file "':"))
        (println err))
      (zero? exit))
    (catch Exception ex
      (verbose)
      (.printStackTrace ex)
      nil))) 

(defn- compile-stale-thrift-files
  "Compile all stale Thrift files based on a compile function, a file with
   last-modified information and a seq of source files."
  [compile! mod-file src-files & {:keys[force-compile]}]
  (let [last-modified (or (read-edn mod-file) {})]
    (loop [src-files src-files
           last-modified last-modified]
      (if-let [[src-file & rst] (seq src-files)]
        (let [p (.getPath src-file)
              m (get last-modified p)]
          (verbose* (str  "Generating Java Sources for `" p "' ... "))
          (if (and (not force-compile) m (<= (.lastModified src-file) m))
            (do
              (verbose "No Changes.")
              (recur rst last-modified))
            (when (compile! p)
              (verbose "OK.")
              (recur rst (assoc last-modified p (.lastModified src-file))))))
        (do
          (spit mod-file last-modified)
          true)))))

;; ## Get Thrift Data

(defn- thrift-project
  [project]
  (let [data (:thrift project {})
        target-dir (:target-path project "target")]
    (-> {}
      (assoc :path (:path data "thrift"))
      (assoc :source-paths (:source-paths data ["src/thrift"]))
      (assoc :java-opts (:java-opts data "bean,hashcode"))
      (assoc :javac-opts (:javac-opts data []))
      (assoc :target-path (str target-dir "/thrift-java"))
      (assoc :modified-file (str target-dir "/.lein-thriftc-modified"))
      (assoc :force-compile (:force-compile data nil)))))

;; ## Leiningen Command

(defn thriftc
  "Generate Java Sources for Thrift Files.
   
   project.clj:

    :thrift { :path \"thrift\"
              :source-paths \"src/thrift\"
              :java-opts \"bean,hashcode\"
              :force-compile false }
  "
  [project & args]
  (when-not (:root project)
    (println "Can only be run inside a Project!")
    (main/exit 1))
  (let [thrift-project (thrift-project project)
        thrift (:path thrift-project "thrift")]
    (when-not (check-thrift-available thrift)
      (println "Could not find Thrift at:" thrift)
      (main/exit 1))
    (let [verbose? (= (first args) ":verbose")
          src-dirs (:source-paths thrift-project)
          java-dir (:target-path thrift-project)
          mod-file (:modified-file thrift-project)
          java-opts (:java-opts thrift-project)
          javac-opts (:javac-opts thrift-project)
          src-files (thrift-sources src-dirs)
          compile! (partial compile-thrift thrift java-dir java-opts)]
      (binding [*verbose* (= (first args) ":verbose")]
        (when (seq src-files)
          (.mkdirs (io/file java-dir))
          (verbose "------------------------------ Compiling Thrift ------------------------------") 
          (verbose "Thrift Path:    " thrift)
          (verbose "Source Dirs:    " src-dirs)
          (verbose "Java Output:    " java-dir)
          (verbose)
          (let [success? (compile-stale-thrift-files 
                           compile! mod-file src-files
                           :force-compile (:force-compile thrift-project))]
            (verbose "------------------------------------------------------------------------------")
            (when-not success?
              (main/exit 1)))
          (leiningen.javac/javac 
            (-> project
              (update-in [:java-source-paths] #(conj (vec %) java-dir))
              (update-in [:javac-options] #(reduce conj (vec %) javac-opts)))))))))
