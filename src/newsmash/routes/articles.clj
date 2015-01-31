(ns newsmash.routes.articles
  (:require [compojure.core :refer :all]
            [newsmash.layout :as layout]
            [newsmash.util :as util]
            [newsmash.db :as db]))

(defn show-article [id]
  (layout/render
   "article_single.html"
   {:article (db/get-article id)
    :similar (db/get-similar id)}))

(defn articles-list []
  (layout/render
   "articles.html"
   {:articles (db/first-articles 20)}))

(defroutes articles-routes
  (GET "/articles" [] (articles-list))
  (GET "/articles/:id" [id] (show-article id)))
