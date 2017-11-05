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


(apply str (repeat 10 "s"))

(frequencies [1 2 3 4 5 6])
;; is this automatically sorted? :O
(frequencies [[1 1] [2 2] [3 3] [1 2] [1 1] [2 2]])

;; game of life stolen from http://clj-me.cgrand.net/2011/08/19/conways-game-of-life/
(defn neighbours [[x y]]
  (for [dx [-1 0 1]
        dy (if (zero? dx) [-1 -1] [-1 0 -1])]
    [(+ dx x) (+ dy y)]))

(defn neighbours
  "Determines all the neighbours of a given coordinate"
  [[x y]]
  (for [dx [-1 0 1] dy [-1 0 1] :when (not= 0 dx dy)]
    [(+ dx x) (+ dy y)]))

(neighbours [5 5])

(defn step [cells]
  (set (for [[loc n] (frequencies (mapcat neighbours cells))
             :when (or (= n 3) (and (= n 2) (cells loc)))]
         loc)))

(defn create-world [w h & living-cells]
  (vec (for [y (range w)]
         (vec (for [x (range h)]
                (if (contains? (first living-cells) [y x])
                  "X"
                  " "))))))

(def board #{[1 0] [1 1] [1 2]})

(create-world 4 4 board)

(take 1 (iterate step board))
(take 2 (iterate step board))

(defn stepper [neighbours birth? survive?]
  (fn [cells]
    (set (for [[loc n] (frequencies (mapcat neighbours cells))
               :when (if (cells loc)
                       (survive? n)
                       (birth? n))]
           loc))))

((stepper neighbours #{3} #{2 3}) #{[1 0] [1 1] [1 2]})
;; sets acts as function!
;; we can pass them as predicates to the stepper, and if the set has the arg it returns true
;; So conways game of life:
;; - if the cell is alive and it's neighbours are 3, then survive.
;; - if the cell is dead and has 3 living neighbours then it becomes live

(def glider #{[2 0] [2 1] [2 2] [1 2] [0 1]})
(def light-spaceship #{[2 0] [4 0] [1 1] [1 2] [1 3] [4 3] [1 4] [2 4] [3 4]})
(def blinker #{[1 0] [1 1] [1 2]})
;; [w h] is the format
(def blinker-middle #{[2 2] [2 3] [2 4]})

(def conway-stepper (stepper neighbours #{3} #{2 3}))

(defn conway [[w h] pattern iterations]
  (->> (iterate conway-stepper pattern)
       (drop iterations)
       first
       (create-world w h)
       (map println)))

(conway [5 15] blinker-middle 0)
(conway [5 15] blinker-middle 1)
(conway [5 15] blinker-middle 2)
(conway [5 15] blinker-middle 3)

(conway [5 15] light-spaceship 0)
(conway [5 15] light-spaceship 1)
(conway [5 15] light-spaceship 2)
(conway [5 15] light-spaceship 3)
(conway [5 15] light-spaceship 4)

(conway [5 15] blinker 0)
(conway [5 15] blinker 1)
(conway [5 15] blinker 2)
(conway [5 15] blinker 3)
(conway [5 15] blinker 4)










