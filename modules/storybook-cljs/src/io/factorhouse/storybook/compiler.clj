(ns io.factorhouse.storybook.compiler
  (:require [clojure.java.io :as io])
  (:import (java.lang ProcessBuilder))
  (:refer-clojure :exclude [compile]))

(defn run-process
  [args]
  (let [pb      (ProcessBuilder. args)
        process (.start pb)
        stdout  (.getInputStream process)
        stderr  (.getErrorStream process)]

    (.addShutdownHook
     (Runtime/getRuntime)
     (Thread. (fn []
                (when (.isAlive process)
                  (.destroy process)
                  (Thread/sleep 500)
                  (when (.isAlive process)
                    (.destroyForcibly process))))))

    (future
      (with-open [reader (io/reader stdout)]
        (doseq [line (line-seq reader)]
          (println "[:storybook-cljs] " line))))

    (future
      (with-open [reader (io/reader stderr)]
        (doseq [line (line-seq reader)]
          (println "[:storybook-cljs] " line))))

    process))

(defn configure
  {:shadow.build/stage :configure}
  [build-state {:keys [compiler npm-cmd js-out] :or {npm-cmd "@factorhouse/storybook-cljs" js-out ".storybook/js-out"}}]
  (assert (symbol? compiler) "Missing argument :compiler. Expected a symbol like io.factorhouse.storybook.compiler.hsx")
  (let [output-dir (-> build-state :shadow.build/config :output-dir)
        entry      (some-> build-state :shadow.build/config :entries first name)
        args       ["npx" npm-cmd "compile" (name compiler) output-dir js-out entry]]
    (-> build-state
        (assoc ::args args)
        (update-in [:shadow.build/config :entries] conj compiler)
        (assoc-in [:shadow.build/config :js-options :resolve "os"] {:target :npm :require "os-browserify/browser"})
        (assoc-in [:shadow.build/config :js-options :resolve "tty"] {:target :npm :require "tty-browserify"}))))

(defn compile
  {:shadow.build/stage :flush}
  [build-state]
  (let [args    (::args build-state)
        process (run-process args)
        result  (.waitFor process)]
    (when (= :release (:shadow.build/mode build-state))
      (when-not (= 0 result)
        (throw (Exception. "Failed to compile storybook-cljs")))))
  build-state)