# datomic-repl

A Clojure Program with some Tools for working with Datomic databases.

## Usage

This project currently only runs with 'lein repl'.  Entering the -main function will return immediately with an error response.

Configure your datomic connections file in the project.clj.  Datomic connections files is an edn file representing a map of keyword connection name to connection string.

Eg.

{
:work-db "datomic:free://localhost:4334/working"
:test-db "datomic:free://localhost:4334/testing"
:other "datomic:free://localhost:4334/other"
}

Within the datomic-repl, you can refer to connections via their keyword name.  The actual datomic connection will be lazily cached on first request.

There is self-documentaion on the available functions from the repl by issuing the (help) command.

## License

Copyright © 2016 FIXME

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
