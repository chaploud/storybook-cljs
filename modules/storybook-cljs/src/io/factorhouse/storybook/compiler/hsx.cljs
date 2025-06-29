(ns io.factorhouse.storybook.compiler.hsx
  (:require [io.factorhouse.hsx.core :as hsx]
            [io.factorhouse.storybook.core :as storybook]
            [io.factorhouse.storybook.tagged-json :as tagged-json]))

(defn ^:export proxy
  [cp]
  (fn [props]
    (let [{:keys [props _children]} (tagged-json/deserialize-values (js->clj props))
          cp (if (or (map? props) (seq _children))
               [cp]
               cp)]
      (hsx/create-element
        (cond-> cp
          (map? props) (conj props)
          (seq _children) (into _children))))))

(defn ^:export storybook
  [id]
  (-> (storybook/story id)
      (update :component proxy)
      (update :stories
              (fn [stories]
                (into {} (map (fn [[k v]]
                                (let [args (cond-> {:props (:props v)}
                                             (seq (:children v)) (assoc :_children (:children v)))]
                                  [k (assoc v :args (tagged-json/serialize-values args))])))
                      stories)))
      (clj->js)))
