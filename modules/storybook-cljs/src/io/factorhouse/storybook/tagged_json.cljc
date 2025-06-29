(ns io.factorhouse.storybook.tagged-json
  (:require [clojure.string :as str]))

(derive #?(:clj clojure.lang.Keyword :cljs cljs.core.Keyword) ::kw!)
(derive #?(:clj clojure.lang.APersistentSet :cljs cljs.core.PersistentHashSet) ::set!)

(defmulti serialize type)

(defmethod serialize :default [this] this)

(defmethod serialize ::kw! [this]
  ["kw!" (if-let [ns (namespace this)]
           (str ns "/" (name this))
           (name this))])

(defmethod serialize ::set! [this]
  ["set!" (vec this)])

(defn tagged-tuple? [x]
  (and (vector? x)
       (not (map-entry? x))
       (= 2 (count x))
       (string? (first x))))

(defmulti deserialize (fn [[id _]] (keyword "io.factorhouse.storybook.tagged-json" id)))

(defmethod deserialize :default [x] x)

(defmethod deserialize ::kw! [[_ kw]]
  (let [[ns name] (str/split kw "/")]
    (if name
      (keyword ns name)
      (keyword ns))))

(defmethod deserialize ::set! [[_ val]]
  (set val))

(defn serialize-values
  [x]
  (letfn [(thisfn [x] (cond
                        (map? x) (into {} (map (fn [[k v]]
                                                 [k (thisfn v)])
                                               x))
                        (coll? x) (into [] (map thisfn) x)
                        :else (serialize x)))]
    (thisfn x)))

(defn deserialize-values
  [x]
  (letfn [(thisfn [x]
            (cond
              (tagged-tuple? x) (deserialize x)
              (map? x) (into {} (map (fn [[k v]]
                                       [(keyword k) (thisfn v)])
                                     x))
              (coll? x) (into [] (map thisfn) x)
              :else x))]
    (thisfn x)))

(comment
  (deserialize-values
   (serialize-values {:title       "Foo!"
                      :description "Hello world"
                      :items       [{:icon        :cog
                                     :icon-bg     "bg-teal-500"
                                     :title       "Setup"
                                     :description "Get started using Kafka Connect with Kpow."
                                     :href        "#"}
                                    {:icon        :document
                                     :icon-bg     "bg-sky-500"
                                     :title       "Documentation"
                                     :description "View Kpow's Kafka Connect usage documentation."
                                     :href        "#"}]})))
