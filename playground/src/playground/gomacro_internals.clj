(ns playground.gomacro-internals)


(defn r-assoc [k v m]
  (assoc m k v))

(def add-me (partial r-assoc :name "Baby Lau"))

(add-me {})

(defn thread-it [& fns]
  (fn [initial]
    (reduce
     (fn [acc f]
       (f acc))
     initial
     fns)))

(defn add-personal-info []
  (thread-it (partial r-assoc :name "Baby Lau")
             (partial r-assoc :last-name "Laaau")
             (partial r-assoc :state "n/a")))

;; this is a state monad?
;; state-monad:: s -> (a, s)
;; where s is the state, and the return value is a new value a with the new state s
(defn add-job-info []
  (thread-it (partial r-assoc :job "Developer")))

((thread-it (add-personal-info)
            (add-job-info))
 {})




