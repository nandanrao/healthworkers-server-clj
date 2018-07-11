(ns healthworkers-server.core
  (:gen-class)
  (require [ring.middleware.cors :refer [wrap-cors]]
           [compojure.core :refer :all]
           [monger.core :as mg]
           [monger.collection :as mc]
           [clojure.tools.logging :as log]
           [ring.middleware.defaults :refer [wrap-defaults api-defaults]]
           [ring.middleware.json :refer [wrap-json-response wrap-json-body]]
           [compojure.route :as route]
           [taoensso.carmine :as car :refer [wcar]]
           [healthworkers-server.db :as db :refer [write-event]])
  (:import org.bson.types.ObjectId))


;; gets current not-recently-called messages
(defn handle-get-messages [redis-conn req]
  (wcar redis-conn (car/rpop (get-in req [:params :district]))))

(def foo "foo ")

;; Add id and timestamp
(defn base-event [req event]
  {:_id (get-in req [:params :id])
   :event event
   :timestamp (java.util.Date.)})

;; handle called
(defn handle-called [conn req]
  (as-> (base-event req "called") v
    (assoc v :code (get-in req [:body :code]))
    (write-event conn v)))


(defn handle-call-attempt [conn req]
  (write-event conn (base-event req "attempt")))

(defn handle-no-consent [conn req]
  (write-event conn (base-event req "noConsent")))


(defn app-routes [mongo redis]
  (routes

   ;; sends JSON with { id: string}
   (POST "/messages/:id/no-consent" req (handle-no-consent mongo req))

   ;; sends JSON with { id: string}
   (POST "/messages/:id/attempt" req (handle-call-attempt mongo req))

   ;; sends JSON with { id: string, code: string }
   (POST "/messages/:id/called" req (handle-call-attempt mongo req))

   ;; get next message from
   (GET "/messages/:district" req {:body (handle-get-messages redis req)})

   (route/not-found "Page not found")))

;; add both mongo and redis db's here!
(defn make-app [mongo redis]
  (-> (app-routes mongo redis)
      (wrap-cors :access-control-allow-origin [#".*"]
                 :access-control-allow-methods [:get :put :post :delete])
      (wrap-json-response)
      (wrap-json-body {:keywords? true})
      (wrap-defaults api-defaults)))

(def app (make-app
          (mg/get-db (db/connect-to-db "localhost") "healthworkers")
          {:pool {} :spec { :uri "redis://localhost:6379" }}))
