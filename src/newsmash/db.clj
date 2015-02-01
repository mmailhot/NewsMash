(ns newsmash.db
  (:require [cheshire.core :as json]
            [clj-http.client :as http]
            [environ.core :refer [env]]
            [clojure.string :as string]))

(def ENDPOINT "https://b8e1e2e8-97c5-4587-8a15-6adeb2bd1b32-bluemix.cloudant.com/articles_new/")

(def USERNAME "b8e1e2e8-97c5-4587-8a15-6adeb2bd1b32-bluemix")

(def PASSWORD (env :database-pw))

(defn first-articles [count]
  (->> (http/get (str ENDPOINT "_all_docs?include_docs=true&limit=" (* count 50))
                {:basic-auth [USERNAME PASSWORD]
                 :as :json})
      (:body)
      (:rows)
      (shuffle)
      (take count)))

(defn get-article [id]
  (->> (http/get (str ENDPOINT "/" id)
                 {:basic-auth [USERNAME PASSWORD]
                  :as :json})
       :body))

(defn generate-search-query [tags pub]
  (str (->> tags
            (map #(str "tag:\"" % "\""))
            (string/join " OR ")
            )
       " AND NOT pub:\"" pub "\""))

(defn get-similar [id]
  (let
      [art (->> (http/get (str ENDPOINT "/" id)
                       {:basic-auth [USERNAME PASSWORD]
                        :as :json})
                :body)
       tags (:tags art)
       pub (:publication art)
       candidates (->> (http/get (str ENDPOINT "_design/search/_search/search?include_docs=true&limit=10&query="(generate-search-query tags pub))
                                 {:basic-auth [USERNAME PASSWORD]
                                  :as :json})
                       :body)]
    (->> (:rows candidates)
         (filter #(> (first (:order %)) 0.2)))))
(defn top-publishers []
  (->> (http/get (str ENDPOINT "_design/_stats/_view/count_publishers_new?reduce=true&group=true")
                 {:basic-auth [USERNAME PASSWORD]
                  :as :json})
       (:body)
       (:rows)
       (sort-by :value)
       (reverse)
       (take 10)
       )
  )

(defn upload-article [article]
  (http/post ENDPOINT
             {:basic-auth [USERNAME PASSWORD]
              :form-params article
              :content-type :json}))

