(ns background-on.web
  (:require [compojure.core :refer [defroutes GET]]
            [ring.adapter.jetty :as ring]
            [ring.util.response :as resp]
            [compojure.handler :as handler]
            [compojure.route :as route]
            [background-on.nyt :as nyt])
  (:gen-class))

;; point selmer to the HTML templates
; (selmer/set-resource-path! "resouces")

(defn index-page
  "renders and returns the index page"
  []
  (resp/file-response "index.html" {:root "resouces"}))

(defroutes app-routes
  (GET "/snapshot.json" [] (resp/file-response "snapshot.json" {:root "resources"}))
  (GET "/" [] (resp/file-response "index.html" {:root "resources"}))
  (route/files "/" {:root "resources"})
  (route/not-found "not found"))

(defn init []
  (nyt/write-daily-snapshot (nyt/build-daily-snapshot)))

(defn destroy []
  )

(def app
  (handler/site app-routes))

(defn -main
  ([] (-main 80))
  ([port]
      (ring/run-jetty app {:port (Integer. port) :join? false})))
