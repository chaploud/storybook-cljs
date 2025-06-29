(ns io.factorhouse.storybook.compiler.hsx
  (:require [io.factorhouse.hsx.core :as hsx]
            [io.factorhouse.storybook.core :as storybook]
            [io.factorhouse.storybook.tagged-json :as tagged-json]))

(defn ^:export proxy
  [cp]
  (fn [props]
    (let [{:keys [children] :as props} (tagged-json/deserialize-values (js->clj props))
          props (dissoc props :children)
          props (when (seq props) props)]
      (hsx/create-element
       (cond-> [cp]
         (map? props) (conj props)
         (seq children) (into children))))))

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
