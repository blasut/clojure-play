(ns pcl.chapter6)

(def counter
  (let [count (ref 0)]
    #(dosync (alter count inc))))


(counter)


