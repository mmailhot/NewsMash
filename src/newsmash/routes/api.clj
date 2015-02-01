(ns newsmash.routes.api
  (:require [compojure.core :refer :all]
            [newsmash.layout :as layout]
            [newsmash.crawl.utils :as crawl]
            [newsmash.db :as db]
            ))

(defn find-similar [url]
  (let [tags (crawl/analyse_text (crawl/slurp_and_parse url))]
    (db/get-by-tags tags "STTSRNIESRNISRTSR"))
  )

(defroutes api-routes
  (GET "/api/similar" [url] (find-similar url)))
