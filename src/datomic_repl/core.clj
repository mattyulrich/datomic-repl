(ns datomic-repl.core
  (:gen-class)
  (:require [datomic.api :as d]
            [environ.core :refer [env]]
            [clojure.pprint :refer [pprint]]
            ))

(defn -load-connections []
  (read-string (slurp (env :db-connections-file))))

(defonce -system-ns #{"db" "db.type" "db.install" "db.part" "db.lang" "fressian" "db.unique" "db.excise"
                      "db.cardinality" "db.fn" "db.alter" "db.bootstrap"})

(defonce connections (-load-connections))

(defonce conn-map (atom {}))

(defn list-connections
  "List all connections by keyword name configured for current session."
  []
  (pprint connections))

(defn -open-conn [conn-name]
  (let [conn (d/connect (conn-name connections))]
    (swap! conn-map assoc conn-name conn)
    conn))

(defn get-conn
  "Get a raw datomic connection for the connection keyword name."
  [conn-name]
  (if (nil? (conn-name connections))
    (do
      (println "No available connection named: " conn-name)
      nil)
    (if (conn-name @conn-map)
      (conn-name @conn-map)
      (-open-conn conn-name))))

(defn list-namespaces
  "List all available non-builtin namespaces for the provided connection."
  [conn-name]
  (let [conn (get-conn conn-name)]
    (when (not (nil? conn))
      (let [res-set (d/q '[:find [?ns ...]
                           :in $ ?system-ns
                           :where [?e :db/ident ?ident]
                           [(namespace ?ident) ?ns]
                           [((comp not contains?) ?system-ns ?ns)]]
                         (d/db conn) -system-ns)]
        (sort (map keyword (into [] res-set)))))))

(defn list-attributes
  "list all attributes for a provided namespace."
  [conn-name namespace]
  (let [conn (get-conn conn-name)
        ns-val (if (keyword? namespace) (name namespace) namespace)]
    (when (not (nil? conn))
      (let [res-set (d/q '[:find [?ident ...]
                           :in $ ?in-ns
                           :where [?e :db/ident ?ident]
                           [(namespace ?ident) ?ns]
                           [(= ?ns ?in-ns)]]
                         (d/db conn) ns-val)]
        (sort (into [] res-set))))))

(defn query
  "Execute query for given connection with optional inputs.
  There is no need to specify the db in the inputs, the function
  will insert the db in the appropriate place."
  [conn-name query-spec & inputs]
  (let [seq-in (if (seq inputs) inputs [])
        conn (get-conn conn-name)]
    (when (not (nil? conn))
      (let [input-params (reduce conj [(d/db conn)] inputs)]
        (apply d/q query-spec input-params)))))

(defn pull-from-query
  "Executes a pull request for entities resolved by a provided query.
  If the query doesn't resolve to a list of entity identifiers, an error
  will be printed and no data returned."
  [conn-name pull-spec query-spec & inputs]
  (let [entities (query conn-name query-spec inputs)
        conn (get-conn conn-name)]
    (when (not (nil? conn))
      (if (and (seq entities)
            (and (every? #(and (= 1 (count %1)) (number? (first %1))) entities)))
        (d/pull (d/db conn) pull-spec entities)
        (println "Query Results are Not an Entity Set - Please Restructure Query to return a list of Entities for Pulling")))))

(def help-functions {"list-connections" (var list-connections)
                     "get-conn"         (var get-conn)
                     "list-namespaces"  (var list-namespaces)
                     "list-attributes"  (var list-attributes)
                     "query"            (var query)
                     "pull-from-query"  (var pull-from-query)})

(defn help []
  "Display doc-strings for available functions in the namespace."
  (let [printouts (map (fn [[name function] ] (str "\n" name ":\n\t" (:doc (meta function)) "\n")) help-functions)]
    (apply println printouts)))

(defn -main []
  (println "This project is meant to be run only from the REPL.  Please run 'lein repl'.")
  (System/exit 0))


