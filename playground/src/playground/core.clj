(ns playground.core
  (:require [cognitect.transcriptor :as xr]))

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






