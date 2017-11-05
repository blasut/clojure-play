(ns playground.core
  (:require [cognitect.transcriptor :as xr]
            [clojure.inspector :as ins]
            [clojure.pprint :as pprint]
            [clojure.walk :as walk]
            [clojure.zip :as zip]
            ))

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

(try (java.util.Date. "foo")
     (catch Exception e (Throwable->map e)))

(ins/inspect-tree (try (java.util.Date. "foo")
                       (catch Exception e (Throwable->map e))))

(def data (try (java.util.Date. "lol")
               (catch Exception e (Throwable->map e))))

;; it is a map, wtf
(= (type data)
   (type {:a 1 :b 2}))

(keys data)

(get data :via)
(get data :trace)
(get data :cause)

(first data)


;; NS
(all-ns)

;; walking
(walk/walk (fn [x] (+ x 10))
           (fn [seq] (reduce + seq))
           [1 2 3 4 5])


(partition 2 [:a 1 :b 2 :c 3])

(partition 2 1 [:a 1 :b 2 :c 3])

(def original [1 '(a b c) 2])

(def root-loc (zip/seq-zip (seq original)))

original

(zip/node (zip/down root-loc))

(zip/node root-loc)

(-> root-loc zip/down zip/right zip/node)

(-> root-loc zip/down zip/right zip/down zip/right zip/node)
;;           root  -> (a b c) ->    a  ->   b    ->   b

(-> root-loc zip/down zip/right zip/down zip/up zip/right zip/node)
(comment  "What happen when we go off the cliff?")
(try (-> root-loc zip/down zip/right zip/down zip/up zip/right zip/right zip/node)
     (catch NullPointerException e (println "so sad")))

(def b (-> root-loc zip/down zip/right zip/down zip/right))

(zip/node b)

(zip/lefts b)
(zip/rights b)

(zip/path b)

(def loc-in-new-tree (zip/remove (zip/up b)))

(zip/root loc-in-new-tree)

(zip/node loc-in-new-tree)

(zip/root (zip/append-child (zip/up b) '(1 2 3)))
(zip/root (zip/insert-left (zip/up b) '(1 2 3)))
(zip/root (zip/insert-right (zip/up b) '(1 2 3)))
(zip/root (zip/insert-child (zip/up b) '(1 2 3)))






