(defn inside [[n d] b]
  [(+ n b)
   (inc d)])

(reduce inside
        [0 0]
        [1 2 3 4 5])
