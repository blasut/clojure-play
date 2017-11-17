(ns playground.actors-tut
  (:require [clojure.core.async
             :as async
             :refer [go <! >! <!! >!! chan dropping-buffer]]))

(defn actor [f]
  (let [msgbox (chan (dropping-buffer 32))]
    (go (loop [f f]
          (let [v (<! msgbox)]
            (recur (f v)))))))
