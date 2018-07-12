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

(defn write-event [conn event]
  (mc/insert-and-return conn "events" event))
