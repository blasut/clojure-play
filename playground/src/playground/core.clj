(ns playground.core
  (:require [cognitect.transcriptor :as xr]
            [clojure.inspector :as ins]))

(defn foo
  "I don't do a whole lot."
  [x]
  (println x "Hello, World!"))

(xr/repl-files ".")

(+ 5 5)

(xr/run (first (xr/repl-files ".")))

(def a 5)
(def b 10)
(def c 15)

(ins/inspect-tree {:a 1 :b 2 :c [1 2 3 {:d 4 :e 5 :f [8 7 5]}]})

(ins/inspect-table [[1 2 3] [4 5 6] [7 8 9 2 4 5]])

(ins/inspect (System/getProperties))














