(ns playground.asyncing
  (:require [clojure.core.async :as async]))

(def my-channel (async/chan))

(def a-fut (future (println (async/<!! my-channel))))

(realized? a-fut)

(async/>!! my-channel "hello!")

(realized? a-fut)

;;;;;;;;;;;;;;;;;;;;;;

(future (loop []
          (when-let [v (async/<!! my-channel)]
            (println v)
            (recur))))

(async/>!! my-channel "a")
(async/>!! my-channel "b")
(async/>!! my-channel "hej12303102")


;;;;;;;;;;;;;;;;;;;;;;;;;;


