(ns healthworkers-server.core
  (:gen-class)
  (require [compojure.core :refer :all]
           [monger.core :as mg]
           [healthworkers-server.db :as db]
           [environ.core :refer [env]]
           [healthworkers-server.server :refer [make-app]])
  (:import org.bson.types.ObjectId))

(def app (make-app
          (mg/get-db (db/connect-to-db (env :mongo-host)) "healthworkers")
          {:pool {} :spec { :uri (env :redis-host) }}))
