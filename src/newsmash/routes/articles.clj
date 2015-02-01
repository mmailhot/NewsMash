(ns newsmash.routes.articles
  (:require [compojure.core :refer :all]
            [newsmash.layout :as layout]
            [newsmash.util :as util]
            [newsmash.db :as db]))

(defn show-article [id]
  (let [article (db/get-article id)]
    (layout/render
     "article_single.html"
     {:article article
      :similar (db/get-similar id)
      :personality (db/lookup-personality (:publication article))})))

(defn articles-list []
  (layout/render
   "articles.html"
   {:articles (db/first-articles 20)
    :publications (db/top-publishers)}))

(defroutes articles-routes
  (GET "/articles" [] (articles-list))
  (GET "/articles/:id" [id] (show-article id)))
