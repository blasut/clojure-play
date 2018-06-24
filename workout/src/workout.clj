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


(defn- parse-s-r-w [e]
  (let [split-into-s-r-w #(str/split % #"/|-")]
    (->
     e
     (update ,,, :set-reps-weight #(str/split % #"\n"))
     (update ,,, :set-reps-weight #(mapv split-into-s-r-w %)))))

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

      parse-pass (fn [raw-pass]
                   (let [s-r-w (mapv parse-s-r-w (:exercises raw-pass))]
                     (assoc raw-pass :exercises s-r-w)))]
  (pprint/pprint input-pass)
  (pprint/pprint (:exercises input-pass))
  (pprint/pprint (parse-pass input-pass)))


(let [completed-exercises [{:name "Squat"
                            :comment "Nice form"
                            :sets-reps [5 5]
                            :sets-reps-weight [[1 5 20]
                                               [2 5 20]
                                               [1 5 25]
                                               [2 5 25]
                                               [3 5 25]
                                               [4 5 25]
                                               [5 5 25]]}]]
  completed-exercises)

;; Based on a schema, generate the next workout
(let [schema {:name "StrongLifts"
              :description "Lift strongly"
              :rules ["Alternate passes"]
              :pass [{:name "A"
                      :exercises [{:name "Squat"
                                   :sets-reps [5 5]
                                   :rules ["Increase weight by 2.5kg per workout"]}
                                  {:name "Bench"
                                   :sets-reps [5 5]
                                   :rules ["Increase weight by 2.5kg per workout"]}
                                  {:name "Barbell Row"
                                   :sets-reps [5 5]
                                   :rules ["Increase weight by 2.5kg per workout"]}]}
                     {:name "B"
                      :exercises [{:name "Squat"
                                   :sets-reps [5 5]
                                   :rules ["Increase weight by 2.5kg per workout"]}
                                  {:name "Overhead Press"
                                   :sets-reps [5 5]
                                   :rules ["Increase weight by 2.5kg per workout"]}
                                  {:name "Deadlift"
                                   :sets-reps [5 5]
                                   :rules ["Increase weight by 5kg per workout"]}]}]}
      next-workout (fn [schema starting-weights prev-pass-index]
                     (let [next-pass (get-in schema [:pass (+ prev-pass-index 1)])
                           exercises (map #(assoc % :sets-reps-weight (conj (:sets-reps %) (get starting-weights (:name %)))) (:exercises next-pass))]
                       exercises))]
  (pprint/pprint schema)
  (next-workout schema {"Squat" 20
                        "Bench" 20
                        "Barbell Row" 40
                        "Overhead Press" 20
                        "Deadlift" 50}
                0)
  )

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

  ;; StartingWeights = {Exercise-Name Weight}
  ;; StartingWeights = {:overhead-press 40}
  (generate-workout-from-schema schema [starting-weights???])

  (take 5 (generate-workouts-from-schema schema [starting-weights???]))

  (generate-next-workout-from-schema schema [workouts])

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
  ;; - Do 1 set of 2-3 reps
  ;; - Repeat until next last warmup weight
  ;; - Then do 1 set of 5 reps
  ;; - Then add 5 to 10 kg
  ;; - Then do 1 set of 1 rep
  ;; Generate-Warmup ???
  )











