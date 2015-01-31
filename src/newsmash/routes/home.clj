(ns newsmash.routes.home
  (:require [compojure.core :refer :all]
            [newsmash.layout :as layout]
            [newsmash.util :as util]))

(defn home-page []
  (layout/render
    "home.html" ))

(defn about-page []
  (layout/render "about.html"))

(defroutes home-routes
  (GET "/" [] (home-page))
  (GET "/about" [] (about-page)))
