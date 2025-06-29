(defproject io.factorhouse/storybook-cljs "0.2.0"
  :description "StorybookJS with ClojureScript"
  :url "http://github.com/factorhouse/storybook-cljs"
  :license {:name         "Apache-2.0 License"
            :url          "https://www.apache.org/licenses/LICENSE-2.0"
            :distribution :repo
            :comments     "same as Kafka"}
  :dependencies [[org.clojure/clojure "1.12.0" :scope "provided"]
                 [org.clojure/clojurescript "1.12.42" :scope "provided"]
                 [io.factorhouse/hsx "0.1.23" :scope "provided"]
                 [com.pitch/uix.core "1.4.4" :scope "provided"]]
  :profiles {:dev   {:resource-paths ["dev-resources"]
                     :plugins        [[dev.weavejester/lein-cljfmt "0.13.0"]]
                     :dependencies   [[org.slf4j/slf4j-api "2.0.16"]
                                      [ch.qos.logback/logback-classic "1.3.14"]
                                      [cheshire "5.13.0" :exclusions [com.fasterxml.jackson.core/jackson-databind]]
                                      [clj-kondo "2025.01.16" :exclusions [com.cognitect/transit-java javax.xml.bind/jaxb-api]]]}
             :smoke {:pedantic? :abort}}
  :source-paths ["src"]
  :deploy-repositories [["clojars" {:url      "https://repo.clojars.org"
                                    :username :env/CLOJARS_USERNAME
                                    :password :env/CLOJARS_PASSWORD}]]
  :aliases {"kondo"  ["with-profile" "+smoke" "run" "-m" "clj-kondo.main" "--lint" "src"]
            "fmt"    ["with-profile" "+smoke" "cljfmt" "check"]
            "fmtfix" ["with-profile" "+smoke" "cljfmt" "fix"]})