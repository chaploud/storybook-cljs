(ns io.factorhouse.storybook.core
  (:require [clojure.string :as str]))

(defmulti story identity)

(defn export-story
  [output-dir compiler-ns entry id]
  (let [{:keys [stories]} (story id)
        depth (count (str/split id #"/"))
        dirs (str/join (repeat depth "../"))
        pr-id (pr-str (str id))
        proj-main (pr-str (str dirs output-dir entry ".js"))
        compiler-ns (pr-str (str dirs output-dir compiler-ns ".js"))
        js-str [(str "import " proj-main ";")
                (str "import {storybook} from " compiler-ns ";")
                ""
                (str "const story = storybook(" pr-id ");")
                (str "export default {title: " pr-id ", component: story.component}")
                ""]
        js-str (into js-str
                     (map (fn [[k _]] (str "export const " (name k) " = story.stories." (name k) ";")))
                     stories)]
    (str/join "\n" js-str)))

(defn ^:export export-stories
  [output-dir compiler-ns entry]
  (clj->js
   (for [[k _] (methods story)]
     (let [file (str/join "_" (str/split (str/lower-case k) " "))]
       [(str file "_story.js") (export-story output-dir compiler-ns entry k)]))))
