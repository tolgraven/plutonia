(ns plutonia.handler
  (:require
    [plutonia.middleware :as middleware]
    [plutonia.layout :refer [error-page]]
    [plutonia.routes.home :refer [home-routes]]
    [plutonia.routes.services :refer [service-routes]]
    [reitit.swagger-ui :as swagger-ui]
    [reitit.ring :as ring]
    ; [ring.middleware.content-type :refer [wrap-content-type]]
    [plutonia.env :refer [defaults]]
    [mount.core :as mount]))

(mount/defstate init-app
  :start ((or (:init defaults) (fn [])))
  :stop  ((or (:stop defaults) (fn []))))

(mount/defstate app-routes
  :start
  (ring/ring-handler
    (ring/router
      [(home-routes)
       (service-routes)])
    (ring/routes
      (swagger-ui/create-swagger-ui-handler ; nuke this until/if need?
        {:path   "/swagger-ui"
         :url    "/api/swagger.json"
         :config {:validator-url nil}})
      (ring/create-resource-handler         ; serve resources/public at /
        {:path "/"})
      (ring/create-default-handler          ; error/default routes
        (let [get-error (fn [code message]
                          (constantly (error-page {:status code
                                                   :title (str code " - " message)})))]
          {:not-found           (get-error 404 "Page not found")
           :method-not-allowed  (get-error 405 "Not allowed")
           :not-acceptable      (get-error 406 "Not acceptable") })))))

(defn app []
  (middleware/wrap-base #'app-routes))
