(ns workout
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


(let [pass {:date "2018-19-6" :name "A" :week 1
            :exercises [{:name "Squat"
                         :expected [5 5 50]
                         :set-reps-weight [[1 5 20]
                                           [2 5 40]
                                           [3 5 30]
                                           [4 5 30]
                                           [5 5 30]]}]}
      input-pass {:date "2018-19-6" :name "A" :week 1
                  :exercises [{:name "Squat"
                               :expected "50*5*5"
                               :set-reps-weight "1/5-20\n2/5-40\n3/5-30\n4/5-30\n5/5-30\n"}]}

      split-into-s-r-w #(str/split % #"/|-")
      parse-pass (fn [raw-pass]
                   (let [s-r-w
                         (mapv (fn [e]
                                 (->
                                  e
                                  (update ,,, :set-reps-weight #(str/split % #"\n"))
                                  (update ,,, :set-reps-weight #(mapv split-into-s-r-w %))))
                               (:exercises raw-pass))
                         pass (assoc raw-pass :exercises s-r-w)]
                     pass))]
  (pprint/pprint input-pass)
  (pprint/pprint (:exercises input-pass))
  (pprint/pprint (parse-pass input-pass)))

(map #(str "Hello " % "!") ["Ford" "Arthur" "Tricia"])

(comment
  ;; I would like to have the following interface:
  ;; Schema   = Name Description [Pass] [Rules]
  ;; Pass     = Name [Exercises] [Rules] ;; Rules specific to that Pass, can override the Schema???
  ;; Exercise = Name Set/Rep [Rules] || Exercise = Name Description [Rules] ;; For example if you have an HiT exercise on the bike
  ;; Set/Rep = (Sets, Reps)
  ;; Rules = ??? ;; Maybe keys that point to hashmap-dispatcher?
  ;; CompletedPass/Exercise-Occasion/Workout = Pass [CompletedExercises] Date BodyWeight
  ;; CompletedExercise = Exercise [Set/Rep/Weight] Comment Video
  (add-schema schema)

  (add-workout completed-pass)

  (generate-workout-from schema)

  (generate-next-workout-from schema [workouts])

  (workouts->progression-graph [workouts])

  ;; Rules
  ;; Examples of rules:
  ;; Increase weight by 2.5kg per workout, based on the previous workout
  ;; Increase weight by 5kg per workout, until 100kg then 2.5kg, based on the previous workout
  ;; Alternative pass each week, week1: A,B,A, week2: B,A,B
  ;; Sprint for 10 seconds, reaching 90-95% of max pulse
  ;; Do workout X times per week

  ;; Warmup
  ;; Can warmup be described as a "rule"?
  ;; The warmup should be generated when generating a workout
  ;; Warmup:
  ;; - Start with lowest weight for that exercise,
  ;; - Do 2 sets of 5 reps each
  ;; - Then add between 5 to 10kg
  ;; - Do 2 sets of 5 reps each
  ;; - Repeat until next last warmup weight
  ;; - Then do 1 set of 5 reps
  ;; - Then add 5 to 10 kg
  ;; - Then do 1 set of 1 rep
  ;; Generate-Warmup ???
  )











