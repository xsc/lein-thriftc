(ns ^{:doc "A Thrift Plugin for Leiningen"
      :author "Yannick Scherer"}
  leiningen.thriftc
  (:require [robert.hooke]
            [leiningen
             [clean :as c]
             [javac :as j]]
            [leiningen.core.main :as main]
            [clojure.tools.reader.edn :as edn :only [read-string]]
            [clojure.java.io :as io]
            [clojure.java.shell :as shell])
  (:import java.io.File))

;; ## Helpers

(def ^:private tag "[thriftc]")

(defn- read-edn
  "Read EDN file."
  [^File target-file]
  (when (.exists target-file)
    (edn/read-string (slurp target-file))))

(defn- collect-thrift-sources
  "Find Thrift Sources in the given directories."
  [{:keys [source-paths]}]
  (mapcat
    (fn [dir]
      (let [^File f (io/file dir)]
        (filter #(-> ^File % (.getName) (.endsWith ".thrift")) (file-seq f))))
    source-paths))

(defn- assert-thrift-available!
  "Check if the given Thrift executable is available."
  [{:keys [path]}]
  (try
    (let [{:keys [exit out]} (shell/sh path "-version")]
      (when out
        (main/debug tag (.trim (str out))))
      true)
    (catch Exception _ nil
      (main/abort tag "could not find thrift at:" path))))

(defn- compile-thrift-file!
  [{:keys [path target-path java-gen-opts]} src-file]
  (main/debug tag "*compile*" (.getPath ^File src-file))
  (let [full-target-path (.getCanonicalPath ^File target-path)
        full-source-path (.getCanonicalPath ^File src-file)
        {:keys[exit out err]} (shell/sh
                                path "-r"
                                "--gen" "java" #_(str "java:" java-gen-opts)
                                "-out" full-target-path
                                full-source-path)]
    (when-not (zero? exit)
      (main/info tag "could not compile thrift file:" full-source-path)
      (main/info err))
    (zero? exit)))

(defn- compile-stale-thrift-files!
  [{:keys [modified-file force-compile] :as opts} src-files]
  (let [last-modified (or (read-edn modified-file) 0)
        last-modified (if (number? last-modified) last-modified 0)]
    (doseq [^File src-file src-files]
      (let [ts (.lastModified src-file)]
        (if (> ts last-modified)
          (when-not (compile-thrift-file! opts src-file)
            (main/abort tag "compilation failed."))
          (main/debug tag "*keeping*" (.getPath src-file)))))
    (spit modified-file (System/currentTimeMillis))))

(defn- compile-all-sources!
  [{:keys [target-path] :as opts}]
  (if-let [src-files (seq (collect-thrift-sources opts))]
    (do
      (main/debug tag "sources:" (pr-str (vec src-files)))
      (.mkdirs ^File target-path)
      (compile-stale-thrift-files! opts src-files))
    (main/debug tag "no source files found.")))

;; ## Settings

(def ^:private default-settings
  {:path "thrift"
   :source-paths ["src/thrift"]
   :java-gen-opts "bean/hashcode"
   :force-compile false})

(defn thrift-settings
  [{:keys [thriftc target-path root]}]
  (->> (select-keys thriftc (keys default-settings))
       (merge default-settings)
       (merge
         {:target-path   (io/file target-path "thrift-java")
          :modified-file (io/file target-path ".lein-thriftc-modified")})))

;; ## Leiningen Command

(defn thriftc
  "Generate Java Sources for Thrift Files. Options include:

     :thriftc {:path \"thrift\"
               :source-paths [\"src/thrift\"]
               :java-gen-opts \"bean,hashcode\"
               :force-compile false}
   "
  [project & args]
  (when-not (:root project)
    (main/abort "Can only be run inside a Project!"))
  (let [opts (thrift-settings project)]
    (main/debug tag "options:" (pr-str opts))
    (assert-thrift-available! opts)
    (compile-all-sources! opts)))

;; ## Hook

(defn activate
  "Register thrift compilation as a hook before javac."
  []
  (->> (fn [f project & args]
         (let [{:keys [modified-file]} (thrift-settings project)]
           (when (.isFile modified-file)
             (.delete modified-file))
           (apply f project args)))
       (robert.hooke/add-hook #'c/clean :thriftc))
  (->> (fn [f project & args]
         (thriftc project)
         (let [{:keys [target-path javac-opts]} (thrift-settings project)
               path (.getPath ^File target-path)
               project' (update-in project [:java-source-paths] concat [path])]
           (apply f project' args)))
       (robert.hooke/add-hook #'j/javac :thriftc)))
