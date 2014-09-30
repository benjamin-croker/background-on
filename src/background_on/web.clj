(ns background-on.web
  (:require [compojure.core :refer [defroutes GET]]
            [ring.adapter.jetty :as ring]
            [ring.util.response :as resp]
            [compojure.handler :as handler]
            [compojure.route :as route]
            [background-on.nyt :as nyt])
  (:gen-class))

(def snapshot-json-atom (atom "[]"))

(defroutes app-routes
  (GET "/snapshot" [] @snapshot-json-atom)
  (GET "/" [] (resp/file-response "index.html" {:root "resources"}))
  (route/files "/" {:root "resources"})
  (route/not-found "not found"))

(defn init []
  (reset! snapshot-json-atom
    (nyt/write-daily-snapshot (nyt/build-daily-snapshot))))

(defn destroy []
  )

(def app
  (handler/site app-routes))

(defn -main
  ([] (-main 80))
  ([port]
      (ring/run-jetty app {:port (Integer. port) :join? false})))
