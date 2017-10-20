(ns pcl.chapter9)

(defn report-result [result form]
  (println (format "%s: %s" (if result "pass" "FAIL") (pr-str form))))

(defn test-+ []
  (report-result (= (+ 1 2) 3) '(= (+ 1 2) 3))
  (report-result (= (+ 1 2 3) 6) '(= (+ 1 2 3) 6)))

(test-+)


(defmacro check [form]
  `(report-result ~form '~form))


(defn test-* []
  (check (= (* 1 2) 3))
  (check (= (* 1 2 3) 6)))

(test-*)




