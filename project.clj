(defproject datomic-repl "0.1.0-SNAPSHOT"
  :description "REPL for working with Datomic."
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.9.0-alpha10"]
                 [com.cemerick/piggieback "0.2.1"]
                 [environ "1.0.3"]
                 [org.clojure/tools.namespace "0.2.11"]]
  :repl-options {:nrepl-middleware [cemerick.piggieback/wrap-cljs-repl]
                 :welcome          (println "\n"
                                            "---------------------------\n"
                                            "Welcome to the Datomic-REPL\n"
                                            "---------------------------\n"
                                            "Some helpful tools:\n\t"
                                                "(list-connections) - List Available Connections\n\t"
                                                "(list-namespaces <conn-name>) - Sorted Vector of Available Namespaces for <conn-name>\n\t"
                                                "(help) - Display a list of available functions in namespace\n\t")}
  :plugins [[lein-environ "1.0.3"]]
  :source-paths ["src"]
  :target-path "target/%s"
  :main ^{:skip-aot true} datomic-repl.core
  :profiles {
             :uberjar {:aot :all}
             :dev {
                   :dependencies [[com.datomic/datomic-free "0.9.5206" :exclusions [joda-time]]
                                  [cursive/datomic-stubs "0.9.5153" :scope "provided"]]
                   :env {:db-connections-file "/Users/mattyu/.datomic.connections"}
                   }
             :prod {
                    :repositories {"my.datomic.com" {:url "https://my.datomic.com/repo"
                                                     :creds :gpg}}
                    :dependencies [[com.datomic/datomic-pro "0.9.5372" :exclusions [joda-time]]
                                   [com.amazonaws/aws-java-sdk-dynamodb "1.9.39"]]
                    :env {:db-connections-file "./datomic.connections" }
                    }})
