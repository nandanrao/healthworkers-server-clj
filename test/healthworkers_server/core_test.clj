(ns healthworkers-server.core-test
  (:require [clojure.test :refer :all]
            [ring.mock.request :as mock]
            [monger.core :as mg]
            [monger.collection :as mc]
            [clj-time.core :as t]
            [clj-time.coerce :as c]
            [healthworkers-server.core :refer :all]))

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
              *redis* (connect-redis)]
      (try
        (tests)
        (finally
          (mg/disconnect conn))))))

(use-fixtures :each clear-messages)
(use-fixtures :once connect)

(deftest test-call-attempt
  (is (= ((make-app *mongo* *redis* ) (-> (mock/request :post "/messages/bar/attempt")))
         {:status  201
          :headers {"content-type" "application/json"}
          :body {:_id "bar" :phone "2" :name "foo" :worker "bar" :called false :attempts [1511123085094]}}))
  (is (= (mc/find-map-by-id *db* "messages" "bar") {:_id "bar" :phone "2" :name "foo" :worker "bar" :called false :attempts [1511123085094]})))
