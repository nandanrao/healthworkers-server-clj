(ns healthworkers-server.db-test
  (:require [clojure.test :refer :all]
            [monger.core :as mg]
            [monger.collection :as mc]
            [clj-time.core :as t]
            [clj-time.coerce :as c]
            [healthworkers-server.db :refer :all]))


(def ^:dynamic *db*)

(defn clear-messages
  "Drop messages collection"
  [test]
  (try
    (test)
    (finally
      (mc/remove *db* "messages"))))

(defn connect
  "Drop messages collection"
  [tests]
  (let [conn  (mg/connect {:host "localhost"})]
    (binding [*db* (mg/get-db conn "test-hw")]
      (try
        (tests)
        (finally
          (mg/disconnect conn))))))

(defn fill-db [db]
  (mc/insert-batch db "messages" [
                                  {:phone "1" :name "foo" :worker "bar" :called false :attempts [(c/to-date (t/minus (t/nown) (t/hours 0.5))) (c/to-date (t/minus (t/now) (t/hours 2)))]}
                                  {:phone "1" :name "foo" :worker "bar" :called false :attempts [(c/to-date (t/minus (t/now) (t/hours 1.5))) ]}
                                  {:phone "2" :name "foo" :worker "bar" :called false}
                                  {:phone "3" :name "foo" :worker "bar" :called true}
                                  {:phone "4" :name "foo" :worker "foo" :called true}
                                  {:phone "4" :name "foo" :worker "foo" :called true}])
  )

(use-fixtures :each clear-messages)
(use-fixtures :once connect)

(deftest test-needed-calls
  (testing "Gets calls still needed"
    (is (= (needed-calls {:worker "bar" :count [2 5]} 0.5) 2))
    (is (= (needed-calls {:worker "bar" :count [2 6]} 0.5) 2))
    (is (= (needed-calls {:worker "bar" :count [2 7]} 0.5) 3))))

(deftest test-needed-calls-nil
  (testing "Gets reasonable results even with nils"
    (is (= (needed-calls {:worker "bar" :count [2 nil]} 0.5) 0))
    (is (= (needed-calls {:worker "bar" :count [nil 7]} 0.5) 4))))

(deftest test-get-called-counts
  (testing "Gets call counts from db and ignores recently called"
    (fill-db *db*)
    (is (= (get-called-counts *db* 1.0) '({:worker "bar", :count [1 2]} {:worker "foo", :count [2 nil]})))
    (is (= (get-called-counts *db* 2.0) '({:worker "bar", :count [1 1]} {:worker "foo", :count [2 nil]})))))

(deftest test-get-needed-calls
  (testing "Gets the right number of needed calls for the percentage goal"
    (fill-db *db*)
    (is (= (get-needed-calls *db* 1.0 0.5) '({:worker "bar", :needed 1} {:worker "foo", :needed 0})))
    (is (= (get-needed-calls *db* 1.0 1.0) '({:worker "bar", :needed 2} {:worker "foo", :needed 0})))))
