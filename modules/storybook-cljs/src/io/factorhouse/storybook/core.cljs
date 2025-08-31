(ns io.factorhouse.storybook.core
  (:require [clojure.string :as str]))

(defmulti story identity)

(defn- esm-mode? [target]
  (= target "esm"))

(defn- format-import-path
  "Format import path based on target mode"
  [base-path target]
  (if (esm-mode? target)
    (str "./" base-path)  ; esm requires explicit relative paths
    base-path))           ; npm-module uses regular paths

(defn- generate-import
  "Generate import statement based on import type"
  [imports path]
  (if (empty? imports)
    (str "import " path ";")                      ; side-effect import
    (str "import {" imports "} from " path ";"))) ; named import

(defn export-story
  [output-dir compiler-ns entry id target]
  (let [{:keys [stories]} (story id)
        depth (count (str/split id #"/"))
        dirs (str/join (repeat depth "../"))
        pr-id (pr-str (str id))

        ;; Format paths based on target mode
        proj-main (pr-str (format-import-path (str dirs output-dir entry ".js") target))
        compiler-ns-path (pr-str (format-import-path (str dirs output-dir compiler-ns ".js") target))

        ;; Generate import statements
        js-str [(generate-import "" proj-main)
                (generate-import "storybook" compiler-ns-path)
                ""
                (str "const story = storybook(" pr-id ");")
                (str "export default {title: " pr-id ", component: story.component}")
                ""]
        js-str (into js-str
                     (map (fn [[k _]] (str "export const " (name k) " = story.stories." (name k) ";")))
                     stories)]
    (str/join "\n" js-str)))

(defn ^:export export-stories
  [output-dir compiler-ns entry target]
  (clj->js
   (for [[k _] (methods story)]
     (let [file (str/join "_" (str/split (str/lower-case k) " "))]
       [(str file "_story.js") (export-story output-dir compiler-ns entry k target)]))))
