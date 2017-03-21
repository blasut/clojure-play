(ns deep-walking-macro.core)

(comment
  (only-ints 1 2 3 4) => [1 2 3 4])

(defmacro only-ints [& args]
  (assert (every? integer? args))
  (vec args))

(defn only-ints-fn [& args]
  (assert (every? integer? args))
  (vec args))

(defn test-fn [x]
  (only-ints-fn x))

(only-ints 1 2 3)

(defmacro when [test & body]
  (println &form)
  (println &env)
  `(if ~test
     (do ~@body)
     nil))


;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defmulti parse-item (fn [form ctx]
                       (cond
                         (seq? form) :seq
                         (integer? form) :int
                         (symbol? form) :symbol
                         (nil? form) :nil)))

(defmulti parse-sexpr (fn [[sym & rest] ctx]
                        sym))

(defmethod parse-sexpr 'if
  [[_ test then else] ctx]
  {:type :if
   :test (parse-item test ctx)
   :then (parse-item then ctx)
   :else (parse-item else ctx)})

(defmethod parse-sexpr 'do
  [[_ & body] ctx]
  {:type :do
   :body (doall (map (fn [x] (parse-item x ctx))
                     body))})

(defmethod parse-sexpr :default
  [[f & body] ctx]
  {:type :call
   :fn (parse-item f ctx)
   :args (doall (map (fn [x] (parse-item x ctx))
                     body))})

(defmethod parse-item :seq
  [form ctx]
  (let [form (macroexpand form)]
    (parse-sexpr form ctx)))

(defmethod parse-item :int
  [form ctx]
  (swap! ctx inc)
  {:type :int
   :value form})

(defmethod parse-item :symbol
  [form ctx]
  {:type :symbol
   :value form})

(defmethod parse-item :nil
  [form ctx]
  {:type nil})

(defmacro to-ast [form]
  (parse-item form (atom 0)))

(def incer (partial + 1))

(incer 42)

(defn r-assoc [k v m]
  (assoc m k v))

(def add-me (partial r-assoc :name "Tim"))

(add-me (add-me {}))

(add-me {})

(defn thread-it [& fns]
  (fn [initial]
    (reduce
     (fn [acc f]
       (f acc))
     initial
     fns)))

(defn add-personal-info []
 (thread-it (partial r-assoc :name "tim")
            (partial r-assoc :last-name "bal")
            (partial r-assoc :state "CO")))

(defn add-job-info []
  (thread-it (partial r-assoc :job "deve")))

((thread-it (add-personal-info)
            (add-job-info))
 {})


