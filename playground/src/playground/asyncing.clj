(ns playground.asyncing
  (:require [clojure.core.async :as async]))

;;* Syncronicity

(def my-channel (async/chan))

(def a-fut (future (println (async/<!! my-channel))))

(realized? a-fut)

(async/>!! my-channel "hello!")

(realized? a-fut)


;;** Channels

;; Channels = cordination points.
;; Waits for puts and takes;

(future (loop []
          (when-let [v (async/<!! my-channel)]
            (println v)
            (recur))))

(async/>!! my-channel "a")
(async/>!! my-channel "b")
(async/>!! my-channel "hej12303102")


;;* Buffers

;; Buffers = decouples puts and takes
;; A channel can be buffered, but the API stays the same
;; Buffers change how puts and takes are cordinated
;; In an unbuffered channel puts waits for takes and takes waits for puts. Sort of like a "staffett"

;; Different kind of buffers

;;** fixed buffers
(async/chan (async/buffer 42))
;;  this creates a buffer of the size 42
;; this is the same thing
(async/chan 42)


;; when using a buffered channel puts can continue without takes until the buffer is full
;; when the buffered channel is full, it starts blocking again
;; a full buffered channel = unbuffered channel

;; order is garanteed with fixed buffer channels

;;** dropping buffers
;; the buffer has a maximum size,
;; but when the buffer is full puts succeed, but the values are just silently dropped
;; puts to a dropping buffer will never ever block
(async/chan (async/dropping-buffer 42))

(async/unblocking-buffer? (async/chan (async/dropping-buffer 42)))

;; Can I specify an unbound buffer?
;; no: because there is a hard limit somewhere. Eventually your computer is going to run out.

;;** sliding buffers

;; almost the same as dropping buffer
;; when the buffer is full, puts succeed
;; but the OLDEST values are dropped
;; never blocks on put
(async/chan (async/sliding-buffer 42))
(async/unblocking-buffer? (async/chan (async/sliding-buffer 42)))

;; idiom: using a sliding buffer of size 1, this always gives you the latest values when taking from the buffer
;; common example: reading mouse events in ClojureScript

;;* asynchronicity

(def cooler-channel (async/chan))

;; fire and forget
(async/put! cooler-channel "Daniel jackson")

;; register a callback
(async/put! my-channel "Daniel Jackson"
            (fn [success?]
              (if success?
                (println "wow!")
                (println "oops!"))))

(async/take! cooler-channel
             (fn [value]
               (if (some? value)
                 (println "Welcome to earth, " value)
                 (println "oops"))))



