(ns healthworkers-server.db

  (:require [monger.core :as mg]
            [monger.collection :as mc]
            [monger.json]
            [monger.joda-time]
            [java-time :refer :all]
            [clj-time.core :as t]
            [clj-time.coerce :as c]
            [monger.operators :refer :all]
            [cheshire.generate :refer [add-encoder encode-str remove-encoder]]
            [environ.core :refer [env]]))

(defn connect-to-db [host] (mg/connect {:host host}))
(defn disconnect-db [conn] (mg/disconnect conn))

(defn get-called [r v]
  (->> (filter #(= v (:called (:_id  %))) r)
       (first)
       (:count)))

(defn zero-nil [v] (if (nil? v) 0 v))

(defn needed-calls [call-count per]
  (let [[called not-called] (map zero-nil (:count call-count))
        target (->> not-called
                    (+ called)
                    (* per)
                    (Math/ceil)
                    (int))
        dif (- target called)]
    (if (<= dif 0) 0 dif)))

(defn format-call-counts
  "Returns a map of workers with a 2-vec of called - not-called"
  [res]
  (->> res
       (group-by #(:worker (:_id %)))
       (map (fn [[w r]] {:worker w
                         :count [(get-called r true) (get-called r false)]}))))

(defn get-called-counts [db hrs]
  (let [groups (mc/aggregate
                db
                "messages"
                [{$match {:attempts {$not {$gte (c/to-date
                                                  (t/minus (t/now) (t/hours hrs)))}}}}
                 {$group {:_id {:worker "$worker" :called "$called"}
                          "count" {"$sum" 1}}}])]
    (format-call-counts groups)))

(defn get-needed-calls
  "Returns list of {:worker :needed}"
  [db hrs per]
  (map (fn [c] {:worker (:worker c) :needed (needed-calls c per)})
       (get-called-counts db hrs)))

(defn write-event [conn event]
  (mc/insert conn "events" event))
