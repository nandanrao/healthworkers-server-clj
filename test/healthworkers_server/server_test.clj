(ns healthworkers-server.server-test
  (:import org.bson.types.ObjectId)
  (:require [clojure.test :refer :all]
            [ring.mock.request :as mock]
            [monger.core :as mg]
            [monger.collection :as mc]
            [cheshire.core :refer :all]
            [clj-time.core :as t]
            [clj-time.coerce :as c]
            [healthworkers-server.server :refer :all]))

(def ^:dynamic *mongo*)
(def ^:dynamic *redis*)

(defn clear-messages
  "Drop messages collection"
  [test]
  (try
    (test)
    (finally
      (mc/remove *mongo* "messages"))))

(defn connect
  "Drop messages collection"
  [tests]
  (let [conn  (mg/connect {:host "localhost"})]
    (binding [*mongo* (mg/get-db conn "test-hw")
              *redis* (str "foo" "bar")]
      (try
        (tests)
        (finally
          (mg/disconnect conn))))))

(use-fixtures :each clear-messages)
(use-fixtures :once connect)

(deftest test-call-attempt
  (let [res ((make-app *mongo* *redis* ) (-> (mock/request :post "/messages/bar/attempt")))
        parsed (parse-string (:body res))
        id (get parsed "_id")]
    (is (= (-> res
            (:status))
           201))
    (is (= (:messageId (mc/find-map-by-id *mongo* "events" (ObjectId. id))) (get parsed "messageId")))))
