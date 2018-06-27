(ns workout
  (:require [clojure.spec.alpha :as s]
            [clojure.string :as str]
            [clojure.pprint :as pprint]))

(def schema {:name "StrongLifts"
             :description "Lift strongly"
             :rules ["Alternate passes"]
             :pass {"A" {:name "A"
                         :exercises [{:name "Squat"
                                      :sets-reps [5 5]
                                      :rules ["Increase weight by 2.5kg per workout"]}
                                     {:name "Bench"
                                      :sets-reps [5 5]
                                      :rules ["Increase weight by 2.5kg per workout"]}
                                     {:name "Barbell Row"
                                      :sets-reps [5 5]
                                      :rules ["Increase weight by 2.5kg per workout"]}]}
                    "B" {:name "B"
                         :exercises [{:name "Squat"
                                      :sets-reps [5 5]
                                      :rules ["Increase weight by 2.5kg per workout"]}
                                     {:name "Overhead Press"
                                      :sets-reps [5 5]
                                      :rules ["Increase weight by 2.5kg per workout"]}
                                     {:name "Deadlift"
                                      :sets-reps [5 5]
                                      :rules ["Increase weight by 5kg per workout"]}]}}})

(def schemas (atom [schema]))

(defn- parse-s-r-w [e]
  (let [split-into-s-r-w #(str/split % #"/|-")]
    (->
     e
     (update ,,, :set-reps-weight #(str/split % #"\n"))
     (update ,,, :set-reps-weight #(mapv split-into-s-r-w %)))))

;; clojure.core/repeatedly might be useful

(let []
  (println "Pick schema by key:")
  (doseq [[idx s] (map-indexed vector @schemas)]
    (println (str idx ": " (:name s))))
  (let [k (Integer/parseInt (read-line))
        s (get @schemas k)]
    (println (str "You typed: " k " of type: " (type k)))
    (println "Pick pass:")
    (println (str/join " or " (keys (:pass s))))
    (let [p (get (:pass s) (read-line))]
      (pprint/pprint p)
      (println "Please enter your set/rep/weights for exercise:")
      (doseq [e (:exercises p)]
        (println (str (:name e) ":"))))))

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
(let [next-workout (fn [schema starting-weights prev-pass-index]
                     (let [next-pass (get-in schema [:pass (+ prev-pass-index 1)])
                           exercises (map #(assoc % :sets-reps-weight (conj (:sets-reps %) (get starting-weights (:name %)))) (:exercises next-pass))]
                       (assoc next-pass :exercises exercises)))

      gen-warmup (fn [exercise start-weight work-weight increment]
                   ;; TODO
                   ;; - Get lowest weight
                   ;; - Add two sets of 5 reps
                   ;; - Then add Xkg
                   ;; - Add set of 3 reps
                   ;; - Repeat until work weight
                   (let [start-sets [2 5 start-weight]
                         warmups (loop [start-weight start-weight
                                        work-weight work-weight
                                        sets-reps [start-sets]]
                                   (let [new-weight (+ start-weight increment)]
                                     (if (< new-weight work-weight)
                                       (recur new-weight
                                              work-weight
                                              (conj sets-reps [1 3 new-weight]))
                                       sets-reps)))]
                     warmups))

      gen-warmup-ascending (fn [exercise start-weight work-weight]
                             ;; Always increment by the increment, and the closer to the work-weight, lessen the reps

                             ;; Example 150kg
                             ;; Add two sets of 5 reps with start-weight (20)
                             ;; Add one set of 5 reps with 20 + 20
                             ;; Add one set of 3 reps with 40 + 20
                             ;; Add one set of 2 reps with 60 + 20
                             ;; Add one set of 2 reps with 80 + 20
                             ;; Add one set of 2 reps with 100 + 20
                             ;; Add one set of 1 reps with 120 + 20

                             ;; Example 100kg
                             ;; Add two sets of 5 reps with start-weight (20)
                             ;; Add one set of 5 reps with 20 + 20
                             ;; Add one set of 2 reps with 40 + 20
                             ;; Add one set of 1 reps with 60 + 20

                             ;; Example 50kg (if the w-w ... )
                             ;; Add two sets of 5 reps with start-weight (20)
                             ;; Add one set of 3 reps with 20 + 10

                             ;; Example 30kg (if the w-w is less than or eq to the start+inc)
                             ;; Add two sets of 5 reps with start-weight (20)
                             )

      workout->text (fn [workout]
                      (let [exercises (reduce (fn [coll exercise]
                                                (let [name (:name exercise)
                                                      [sets reps weight] (:sets-reps-weight exercise)
                                                      warmup ""]
                                                  (conj coll (str/join [name "\t" weight "*" sets "*" reps]))))
                                              []
                                              (:exercises workout))
                            pass (str "Pass: " (:name workout))]
                        (str/join "\n" (flatten [pass exercises]))))]

  (pprint/pprint schema)
  (pprint/pprint (next-workout schema {"Squat" 20
                                       "Bench" 20
                                       "Barbell Row" 40
                                       "Overhead Press" 20
                                       "Deadlift" 50}
                               0))
  (workout->text (next-workout schema {"Squat" 20
                                       "Bench" 20
                                       "Barbell Row" 40
                                       "Overhead Press" 20
                                       "Deadlift" 50}
                               0))

  (gen-warmup (get-in schema [:pass 0 :exercises 0]) 20 100 20))


;; calculate which weights to add on each side, based on the bar weight
(let [bar-weight 18.5
      goal-weight 50]
  ;; weight to add: (goal-weight - bar-weight) / 2
  ;; split into reasonable weights
  ;; bars-available: 0.5, 1.25, 2.5, 5, 10, 15, 20, 25

  ;; example: bar 20, goal 50
  ;; 50-20 = 30
  ;; 30/2 = 15
  ;; result = 15kg on each side

  ;; example bar 18.5, goal 50
  ;; 50-18.5 = 31.5
  ;; 31.5/2 = 15.75
  ;; 15kg + 0.5 + 0.5
  ;; == total is 16kg * 2 = 32kg (0.5 overweight)

  ;; example bar 18.5, goal 55
  ;; 55-18.5 = 36.5
  ;; 36.5/2 = 18.25
  ;; if the decimal is .25, ceil to number to .5
  ;; 36.5/2 ~= 18.5
  ;; 15kg + 2.5 + 0.5 + 0.5

  ;; example bar 18.5, goal 55.5
  ;; 55.5-18.5 = 36
  ;; 36/2 = 18
  ;; 15kg + 2.5 + 0.5

  ;; result: [weight-for-a-side overweight]
  ;; result: [15 0]
  ;; result: [16 0.5]
  ())

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
  ;; - Then add between 5 to 20kg
  ;; - Do 1 set of 2-3 reps
  ;; - Repeat until next last warmup weight
  ;; - Then do 1 set of 5 reps
  ;; - Then add 5 to 10 kg
  ;; - Then do 1 set of 1 rep
  ;; Generate-Warmup ???
  )











