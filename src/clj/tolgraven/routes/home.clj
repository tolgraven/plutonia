(ns tolgraven.routes.home
  (:require
   [tolgraven.layout :as layout]
   [tolgraven.db.sql :as sql]
   [tolgraven.middleware :as middleware]
   [clojure.java.io :as io]
   [clojure.pprint :refer [pprint]]
   [taoensso.timbre :as timbre]
   [ring.util.response]
   [ring.util.http-response :as response]))

(defn home-page [request]
  (layout/render-hiccup request))

(defn plain-text-header
  [resp]
  (response/header resp "Content-Type" "text/plain; charset=utf-8"))

(defn home-routes []
  [""
   {:middleware [middleware/wrap-csrf
                 middleware/wrap-formats]}
   ["/" {:get home-page}]
   ["/docs" {:get (fn [_]
                    (-> "docs/docs.md" io/resource slurp
                        response/ok
                        plain-text-header))}]
   ["/fart" {:get (fn [_]
                    (-> "butt"
                        response/ok
                        plain-text-header))}]
   ["/blog" {:get (fn [{{:keys [id]} :path-params}]
                    (-> (sql/get-blog (Integer/parseInt id)) 
                        response/ok))}]
   ["/blog/:id" {:get (fn [{{:keys [id]} :path-params}]
                    (-> (sql/get-blog (Integer/parseInt id))
                        str
                        response/ok
                        plain-text-header))}]
   ["/add-comment" {:post (fn [{{:keys [id]} :path-params}]
                            (-> "not" ;(db/add-blog id)
                                str
                                response/ok
                                plain-text-header))}]
   ["/user/:id" {:get (fn [{{:keys [id]} :path-params}]
                        (let [user "none" #_(db/get-user id)]
                          (timbre/debug user)
                          (-> (or user {})
                              str
                              response/ok
                              plain-text-header)))}]])

