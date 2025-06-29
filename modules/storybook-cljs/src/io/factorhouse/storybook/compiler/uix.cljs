(ns io.factorhouse.storybook.compiler.uix
  (:require [io.factorhouse.storybook.core :as storybook]
            [io.factorhouse.storybook.tagged-json :as tagged-json]
            [uix.core :as uix :refer [$]]))

(defn ^:export proxy
  [cp]
  (uix/as-react
   (fn [props]
     ($ cp (tagged-json/deserialize-values (js->clj props))))))

(defn ^:export storybook
  [id]
  (-> (storybook/story id)
      (update :component proxy)
      (update :stories
              (fn [stories]
                (into {} (map (fn [[k v]]
                                [k (update v :args tagged-json/serialize-values)]))
                      stories)))
      (clj->js)))