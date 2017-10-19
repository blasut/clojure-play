(ns pcl.chapter5)

(defn foo [a b & [c d]]
  (list a b c d))

(foo 1 2)

(foo 1 2 3 4)

(defn bar [{:keys [a b] :or {b 10}}]
  (list a b))

(bar {:a 1})

(bar {:a 1 :b 2})


(defn pair-with-product-greater-than [n]
  (take 1 (for [i (range 10) j (range 10) :when (> (* i j) n)] [i j])))

(pair-with-product-greater-than 30)

(pair-with-product-greater-than 50)


(defn plot [f min max step]
  (doseq [i (range min max step)]
    (dotimes [_ (apply f [i])] (print "*"))
    (println)))

(plot #(Math/pow % 2) 1 5 1)

(plot identity 2 10 2)
