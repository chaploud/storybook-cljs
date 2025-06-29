(ns example.core
  (:require [io.factorhouse.storybook.core :as storybook]))

(defn button []
  [:button {} "Hello world!"])

(defmethod storybook/story "Kpow/Buttons/PrimaryButton" [_]
  {:component [button {}]
   :stories   {:Filter {}}})