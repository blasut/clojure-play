(ns coloring-india.core
  (:require [clojure.core.logic :as logic]
            [clojure.core.logic.fd :as fd]
            [cheshire.core :as cheshire]
            [clojure.core.logic.pldb :as pldb]))

(pldb/db-rel man name)
(pldb/db-rel woman name)
(pldb/db-rel likes name object)

(def facts
  (-> (pldb/db)
      (pldb/db-fact man 'john)
      (pldb/db-fact woman 'kim)
      (pldb/db-fact likes  'john 'cats)
      (pldb/db-fact likes 'kim 'cats)))

(logic/run-db 1 facts [q]
              (logic/fresh [X Y Z]
                (man X)
                (woman Y)
                (likes X Z)
                (likes Y Z)
                (== q [X Y Z])))
