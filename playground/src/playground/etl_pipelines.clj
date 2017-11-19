(ns playground.etl-pipelines
  (:require [cheshire.core :as json]
            [clojure.core.async :as async]
            [clojure.java.io :as io]))

;; https://tech.grammarly.com/blog/building-etl-pipelines-with-clojure

(comment
  (letfn [(rand-obj []
            (case (rand-int 3)
              0 {:type "number" :number (rand-int 1000)}
              1 {:type "string" :string (apply str (repeatedly 30 #(char (+ 33 (rand-int 90)))))}
              2 {:type "empty"}))]
    (with-open [f (io/writer "/tmp/dummy.json")]
      (binding [*out* f]
        (dotimes [_ 100000]
          (println (json/encode (rand-obj))))))))

;; fake db
(def db (atom 0))

(defn save-into-database [batch]
  (swap! db + (count batch)))

(def file-name "/tmp/dummy.json")

(defn parse-json-file-lazy [file]
  (map #(json/decode % true)
       ;; line-seq reads into a lazy seq, woop
       (line-seq (io/reader file))))

(take 10 (parse-json-file-lazy file-name))

(defn valid-entry? [log-entry]
  (not= (:type log-entry) "empty"))

(defn transform-entry-if-relevant [log-entry]
  (cond (= (:type log-entry) "number")
        (let [number (:number log-entry)]
          (when (> number 900)
            (assoc log-entry :number (Math/log number))))

        (= (:type log-entry) "string")
        (let [string (:string log-entry)]
          (when (re-find #"a" string)
            (update log-entry :string str "-improved")))))

(->> (parse-json-file-lazy file-name)
     (filter valid-entry?)
     (keep transform-entry-if-relevant)
     (take 10))

(defn process [files]
  (->> files
       (mapcat parse-json-file-lazy) ;; mapcat because on file produces many entries
       (filter valid-entry?)
       (keep transform-entry-if-relevant)
       (partition-all 1000) ;; form batches for "saving" into db
       (map save-into-database)
       doall)) ;; nice little "trick" to force eagerness

(comment
  (time (process [file-name]))

  (time (process (repeat 8 file-name))))

(comment
  (def num-coll (range 10))

  ;; the two expressions are equal, but the transducer one doesn't create any intermediate sequence between filtering and mapping
  (map inc (filter even? num-coll))
  ;; sequence returns a lazy seq... should be possible to take?
  (sequence (comp (filter even?) (map inc)) num-coll)
  (take 1 (sequence (comp (filter even?) (map inc)) num-coll))
  (type (sequence (comp (filter even?) (map inc)) num-coll))
  ;; => lazy-seq
  )

;; someting like line-seq but returns something reducible instead of a lazy seq
;; the ^-thing is a reader macro(?) that means meta-data. In this case it means a typehint
(defn lines-reducible [^java.io.BufferedReader rdr]
  (reify clojure.lang.IReduceInit
    (reduce [this f init]
      (with-open [rdr rdr]
        (loop [state init]
          (if (reduced? state)
            state
            (if-let [line (.readLine rdr)]
              (recur (f state line))
              state)))))))

(meta #'lines-reducible)
;; doens't return the type hint though. Must be in the inner form?

;; eduction = a way to call transducers on stuff, several transform-fns can be given without comp
;; it's not realizing the coll, but returns an Eduction object which knows how to perform the transforms.
;; when printing it prints as a seq, but it's really a Eduction obj.
;; ???
(defn parse-json-file-reducible [file]
  (eduction (map #(json/decode % true))
            (lines-reducible (io/reader file))))


(defn process-with-transducers [files]
  (transduce (comp (mapcat parse-json-file-reducible)
                   (filter valid-entry?)
                   (keep transform-entry-if-relevant)
                   (partition-all 1000)
                   (map save-into-database))
             (constantly nil)           ;; the accumulator fn, but we don't want to return anything
             nil                        ;; the starting value is also nil
             files))

(comment
  (time (process-with-transducers [file-name]))
  (time (process-with-transducers (repeat 8 file-name))))


;; we can use core async with transducers to parallelize thi shiet

(defn process-parallel [files]
  (async/<!!
   (async/pipeline
    (.availableProcessors (Runtime/getRuntime))
    (doto (async/chan) (async/close!)) ;; output challen = /dev/null
    (comp (mapcat parse-json-file-reducible)
          (filter valid-entry?)
          (keep transform-entry-if-relevant)
          (partition-all 1000)
          (map save-into-database))
    (async/to-chan files))))            ;; channel with input data


(comment
  ;; this was actually really fast :O
  (time (process-parallel (repeat 8 file-name))))

;; we can actually re-use this
(def do-things
  (comp (mapcat parse-json-file-reducible)
        (filter valid-entry?)
        (keep transform-entry-if-relevant)
        (partition-all 1000)
        (map save-into-database)))

(defn process-with-transducers-re-use [files]
  (transduce do-things
             (constantly nil)           ;; the accumulator fn, but we don't want to return anything
             nil                        ;; the starting value is also nil
             files))

(defn process-parallel-re-use [files]
  (async/<!!
   (async/pipeline
    (.availableProcessors (Runtime/getRuntime))
    (doto (async/chan) (async/close!)) ;; output challen = /dev/null
    do-things
    (async/to-chan files))))

(comment
  (time (process-with-transducers-re-use [file-name]))
  (time (process-parallel-re-use (repeat 8 file-name))))














