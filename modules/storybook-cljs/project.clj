(defproject io.factorhouse/storybook-cljs "0.1.20"
  :description "StorybookJS with ClojureScript"
  :url "http://github.com/factorhouse/storybook-cljs"
  :license {:name         "Apache-2.0 License"
            :url          "https://www.apache.org/licenses/LICENSE-2.0"
            :distribution :repo
            :comments     "same as Kafka"}
  :dependencies [[org.clojure/clojure "1.12.0" :scope "provided"]
                 [org.clojure/clojurescript "1.12.42" :scope "provided"]
                 [io.factorhouse/hsx "0.1.23" :scope "provided"]]
  :source-paths ["src"]
  :deploy-repositories [["clojars" {:url      "https://repo.clojars.org"
                                    :username :env/CLOJARS_USERNAME
                                    :password :env/CLOJARS_PASSWORD}]])
