(ns io.factorhouse.storybook.compiler.uix
  (:require [cljs-bean.core :as bean]
            [io.factorhouse.storybook.core :as storybook]
            [io.factorhouse.storybook.tagged-json :as tagged-json]
            [uix.core :as uix :refer [$]]))

(defn ^:export proxy
  [cp]
  (uix/as-react
   (fn [props]
     ($ cp (tagged-json/deserialize-values (bean/->clj props))))))

(defn ^:export storybook
  [id]
  (-> (storybook/story id)
      (update :component proxy)
      (update :stories
              (fn [stories]
                (into {} (map (fn [[k v]]
                                [k {:args (tagged-json/serialize-values v)}]))
                      stories)))
      (clj->js)))