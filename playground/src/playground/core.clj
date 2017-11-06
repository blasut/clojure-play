(ns playground.core
  (:require [cognitect.transcriptor :as xr]
            [clojure.inspector :as ins]
            [clojure.pprint :as pprint]
            [clojure.walk :as walk]
            [clojure.zip :as zip]
            [clojure.string :as str]
            [clojure.java.io :as io]
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


;; ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;


;; clojure programming

(comment
  "Using quote to figure out how different reader macros expand"
  ''x
  '''''''''''''''''''x
  '@x
  '#(+ % %)
  "pretty neat"
  '`(a b ~c)
  (pprint/pprint '`(a b ~c))
  (pprint/pprint '`(a 1))
  '(a 1)
  (pprint/pprint '`(a ~@(1 2 3) c))

  )

;; a lot of stuff has a implicit do block (progn)
(macroexpand '(let [a 5] (println 5) (println (+ a 1))))
;; cant be seen here though?


(def chas {:name "Chas" :age 31 :location "Massachusetts"})

;; this is reallly nice
;; fucking awesome u can use keys,strs and syms
(let [{:keys [name age location]} chas]
  (format "%s is %s years old and lives in %s." name age location))

(def brian {"name" "Brian" "age" 31 "location" "British Columbia"})
(let [{:strs [name age location]} brian]
  (format "%s is %s years old and lives in %s." name age location))

(def christophe {'name "Christophe" 'age 33 'location "Rhône-Alpes"})
(let [{:syms [name age location]} christophe]
  (format "%s is %s years old and lives in %s." name age location))

;; mutually recursive functions
;; letfn creates several named functions at once
(letfn [(odd? [n]
          (even? (dec n)))
        (even? [n]
          (or (zero? n)
              (odd? (dec n))))] (odd? 11))

;; function literals:
(read-string "(fn [x y] (Math/pow x y))")
;; both of thes are the same
(read-string "#(Math/pow %1 %2)")

(defn embedded-repl
  "A naive repl from clojure programming book. Enter :quit to exit"
  []
  (print (str (ns-name *ns*) ">>> "))
  (flush)
  (let [expr (read)
        value (eval expr)]
    (when (not= :quit value)
      (println value)
      (recur))))

(comment
  (embedded-repl)
  )

;; fun

;; compps
(def negated-sum-str (comp str - +))

(negated-sum-str 10 12 3.4)

(def camel->keyword (comp keyword
                          str/join
                          (partial interpose \-)
                          (partial map str/lower-case)
                          #(str/split % #"(?<=[a-z])(?=[A-Z])")))

(camel->keyword "CamelCase")
(camel->keyword "lowerCamelCase")

;; comp and ->> is the same thing. or has the same behaviour
;; comp and partial = point free programming

;; THE LOGGER

(defn print-logger
  [writer]
  #(binding [*out* writer]
     (println %)))

(def *out*-logger (print-logger *out*))
(*out*-logger "hello")

(def writer (java.io.StringWriter.))
(def retained-logger (print-logger writer))

(retained-logger "hello")

(str writer)

(defn file-logger
  [file]
  #(with-open [f (io/writer file :append true)]
     ((print-logger f) %)))

(def log->file (file-logger "messages.log"))

(log->file "hello")

(defn multi-logger
  [& logger-fns]
  #(doseq [f logger-fns]
     (f %)))

(def log (multi-logger
          (print-logger *out*)
          (file-logger "messages.log")))

(log "hello again")

(defn timestamped-logger [logger]
  #(logger (format "[%1$tY-%1$tm-%1$te %1$tH:%1$tM:%1$tS] %2$s" (java.util.Date.) %)))

(def log-timestamped (timestamped-logger
                      (multi-logger
                       (print-logger *out*)
                       (file-logger "messages.log"))))

(log-timestamped "good by now")

;; ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

;; Att page 140
(def orders
  [{:product "Clock", :customer "Wile Coyote", :qty 6, :total 300}
   {:product "Dynamite", :customer "Wile Coyote", :qty 20, :total 5000}
   {:product "Shotgun", :customer "Elmer Fudd", :qty 2, :total 800}
   {:product "Shells", :customer "Elmer Fudd", :qty 4, :total 100}
   {:product "Hole", :customer "Wile Coyote", :qty 1, :total 1000}
   {:product "Anvil", :customer "Elmer Fudd", :qty 2, :total 300}
   {:product "Anvil", :customer "Elmer Fudd", :qty 2, :total 300}
   {:product "Anvil", :customer "Wile Coyote", :qty 6, :total 900}])

(defn reduce-by
  [key-fn f init coll]
  (reduce (fn [summaries x]
            (let [k (key-fn x)]
              (assoc summaries k (f (summaries k init) x))))
          {} coll))

(reduce-by :customer #(+ %1 (:total %2)) 0 orders)

(reduce-by :product #(conj %1 (:customer %2)) #{} orders)

(defn reduce-by-in
  [keys-fn f init coll]
  (reduce (fn [summaries x]
            (let [ks (keys-fn x)]
              (assoc-in summaries ks
                        (f (get-in summaries ks init) x))))
          {} coll))

;; juxt seems cool. takes ((juxt a b c) x) => [(a x) (b x) (c x)]
(reduce-by-in (juxt :customer :product)
              #(+ %1 (:total %2)) 0 orders)

(def flat-breakup
  {["Wile Coyote" "Anvil"] 900,
   ["Elmer Fudd" "Anvil"] 300,
   ["Wile Coyote" "Hole"] 1000,
   ["Elmer Fudd" "Shells"] 100,
   ["Elmer Fudd" "Shotgun"] 800,
   ["Wile Coyote" "Dynamite"] 5000,
   ["Wile Coyote" "Clock"]
   300})

(reduce #(apply assoc-in %1 %2) {} flat-breakup)

;; Need to pracitce reduce

;; page 151

(defn naive-into
  [coll source]
  (reduce conj coll source))

(= (into #{} (range 500))
   (naive-into #{} (range 500)))

(defn faster-into
  [coll source]
  (persistent!  (reduce conj! (transient coll) source)))

(= (into #{} (range 500))
   (naive-into #{} (range 500))
   (faster-into #{} (range 500)))


;; 155

;; meta data metadata
(def e ^{:created (System/currentTimeMillis)} [1 2 3])

(meta e)

;; page 156



