(ns playground.quickcheck
  (:require  [clojure.test :as t]
             [clojure.test.check :as tc]
             [clojure.test.check.generators :as gen]
             [clojure.test.check.properties :as prop]))

;;* introduction to testcheck

(defn ascending? [coll]
  (every? (fn [[a b]] (<= a b))
          (partition 2 1 coll)))

(def property
  (prop/for-all [v (gen/vector gen/int)]
                (let [s (sort v)]
                  (and (= (count v) (count s))
                       (ascending? s)))))

(def bad-property
  (prop/for-all [v (gen/vector gen/int)]
                (ascending? v)))

(comment
  (tc/quick-check 100 property)
  ;; {:result true, :num-tests 100, :seed 1511091368010}
  (tc/quick-check 100 bad-property)
  ;; {:result false,:result-data {},:seed 1511091441862,:failing-size 3,:num-tests 4,:fail [[0 -3 -3]],
  ;; :shrunk {:total-nodes-visited 14,:depth 3,:result false,:result-data {},:smallest [[0 -1]]}}
  (tc/quick-check 50 bad-property :seed 1511091441862))

;;** generators

(gen/sample (gen/map gen/simple-type-printable gen/simple-type-printable))

(gen/sample (gen/fmap set (gen/vector gen/nat)))

;; generate a vector of keywords, then choose a random element from it, and return both the vector and the random element
(def keyword-vector (gen/such-that not-empty (gen/vector gen/keyword)))
(def vec-and-elem
  (gen/bind keyword-vector
            (fn [v] (gen/tuple (gen/elements v) (gen/return v)))))

(gen/sample keyword-vector 4)
(gen/sample vec-and-elem 4)

(gen/sample (gen/return 42))

;;** recursive stuff
(def nested-vector-of-boolean (gen/recursive-gen gen/vector gen/boolean))
(last (gen/sample nested-vector-of-boolean 20))
(gen/sample nested-vector-of-boolean 8)

(def compound (fn [inner-gen]
                (gen/one-of [(gen/list inner-gen)
                             (gen/map inner-gen inner-gen)])))
(def scalars (gen/one-of [gen/int gen/boolean]))
(def my-json-like-thing (gen/recursive-gen compound scalars))

(last (gen/sample my-json-like-thing 20))


;;* Generator examples

;;** integers lower through upper inclusive
(def five-through-nine (gen/choose 5 9))
(gen/sample five-through-nine)


;;** a random element from a vector
(def languages (gen/elements ["clojure" "haskell" "erlang" "scala" "python"]))
(gen/sample languages)

;;** an integer or nil
(def int-or-nil (gen/one-of [gen/int (gen/return nil)]))
(gen/sample int-or-nil)

;;** an integer 90%  of the time, nil 10%
(def mostly-ints (gen/frequency [[9 gen/int] [1 (gen/return nil)]]))
(gen/sample mostly-ints)

(def mostly-ints-too-high-freq (gen/frequency [[8 gen/int] [5 (gen/return nil)]]))
(gen/sample mostly-ints-too-high-freq)  ;; doens't error, just returns weird results

;;** even positive integers
(def even-and-positive (gen/fmap #(* 2 %) gen/pos-int))
(gen/sample even-and-positive 20)

;;** powers of two
(def powers-of-two (gen/fmap #(int (Math/pow 2 %)) gen/s-pos-int))
(gen/sample powers-of-two)

;;** sorted vector of ints
(def sorted-vec (gen/fmap #(vec (sort %)) (gen/vector gen/int)))
(gen/sample sorted-vec)

;;** an integer and a boolean
(def int-and-boolean (gen/tuple gen/int gen/boolean))
(gen/sample int-and-boolean)

;;** any number but five
(def anything-but-five (gen/such-that #(not= % 5) gen/int))
(gen/sample anything-but-five)
;; It’s important to note that such-that should only be used for predicates that are very likely to match





