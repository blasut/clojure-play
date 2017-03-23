(ns logic-tutorials.episode2
  (:refer-clojure :exclude [==]))

(defn lvar
  ([] (lvar ""))
  ([nm] (gensym (str nm "_"))))

(defn lvar? [v]
  (symbol? v))

(defn walk [s u]
  (if-let [pr (get s u)]
    (if (lvar? pr)
      (recur s pr)
      pr)
    u))

(defn unify [s u v]
  (let [u (walk s u)
        v (walk s v)]
    (cond
      (and (lvar? u)
           (lvar? v)
           (= u v)) s
      (lvar? u) (assoc s u v)
      (lvar? v) (assoc s v u)
      :else (and (= u v) s))))

(defn == [a b]
  (fn [s]
    (if-let [v (unify s a b)]
      [v]
      [])))

(defn -conj
  ([a] a)
  ([a b]
   (fn [s]
     (for [aret (a s)
           :when aret
           bret (b aret)
           :when bret]
       bret)))
  ([a b & more]
   (-conj a (apply -conj b more))))

(defn -disj [& goals]
  (fn [s]
    (mapcat (fn [goal]
              (goal s))
            goals)))

(let [a (lvar "a")
      b (lvar "b")]
  ((-conj
    (== a 42)
    (== b a)
    (== 1 1)) {}))

(let [a (lvar "a")
      b (lvar "b")]
  ((-disj (-conj
           (== b a)
           (== a 42))
          (== b 11)) {}))

(defmacro fresh [lvars & body]
  `(let [~@(mapcat (fn [x]
                     `[~x (lvar ~(name x))])
                   lvars)]
     (-conj ~@body)))

(defn reify-lvar [results lvars]
  (for [result results]
    (map (partial walk result) lvars))) 

(defmacro run [lvars & body]
  `(let [~@(mapcat (fn [x]
                     `[~x (lvar ~(name x))])
                   lvars)
         v# [~@lvars]
         result# ((-conj ~@body) {})]
     (reify-lvar result# v#)))

(defn conde [& goals]
  (apply -disj (map (partial apply -conj) goals)))

;; Innerparts of every [] is a conjuction, and every [] is a disjunction
(run [a b]
  (conde
   [(== a 1)
    (fresh [q]
      (== q a)
      (== b q))]
   [(== b 2)
    (== a b)]))

((fresh [a b]
   (== a 42)
   (== b a))
 {})

(macroexpand '(run [q a]
               (== q 1)))

(run [q]
 (== q 1))

(run [q]
  (-disj (== q 1)
         (== q 2)))
