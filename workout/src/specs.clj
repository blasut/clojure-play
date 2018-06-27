(ns workout.specs
  (:require [clojure.spec.alpha :as s]
            [clojure.string :as str]
            [clojure.pprint :as pprint]))

(s/valid? number? 44)

(s/def ::sets (s/and int? #(> % 0)))
(s/def ::reps (s/and int? #(> % 0)))
(s/def ::number (s/and int? #(> % 0)))
(s/def ::unit #{:kg :lb})
(s/def ::unit keyword?)
(s/def ::weight (s/keys :req [::unit ::number]))
(s/def ::name string?)

(let [exercise (s/keys :req-un [::sets
                                ::reps
                                ::name])
      pass (s/coll-of exercise)
      schema (s/coll-of pass)
      completed-exercise (s/keys :req-un [::sets ::reps ::weight])
      completed-pass (s/and pass (s/coll-of completed-exercise))]
  (s/explain exercise {:sets 5 :reps 5 :name "Squat"})
  (println (s/exercise completed-pass))
  (s/exercise completed-exercise))

