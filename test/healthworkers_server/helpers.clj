(ns healthworkers-server.helpers
    (:require [clojure.test :refer :all]
              [monger.core :as mg]
              [monger.collection :as mc]))

(defn make-clear-db-fix [db coll]
  (fn [test]
    (try
      (test)
      (finally
        (mc/remove db coll)))
    ))
