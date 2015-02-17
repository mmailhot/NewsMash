(ns newsmash.db
  (:require [cheshire.core :as json]
            [clj-http.client :as http]
            [environ.core :refer [env]]
            [clojure.string :as string]
            [newsmash.service_creds :refer [CLOUDANT]]))

(def ENDPOINT (:url CLOUDANT))

(def USERNAME (:un CLOUDANT))

(def PASSWORD (:pw CLOUDANT))

(defn first-articles [count]
  (->> (http/get (str ENDPOINT "/articles_new/_all_docs?include_docs=true&limit=" (* count 50))
                {:basic-auth [USERNAME PASSWORD]
                 :as :json})
      (:body)
      (:rows)
      (shuffle)
      (take count)))

(defn get-article [id]
  (->> (http/get (str ENDPOINT "/articles_new/" id)
                 {:basic-auth [USERNAME PASSWORD]
                  :as :json})
       :body))

(defn generate-search-query [tags pub]
  (str (->> tags
            (map #(str "tag:\"" % "\""))
            (string/join " OR ")
            )
       " AND NOT pub:\"" pub "\""))

(defn get-by-tags [tags pub]
  (let [candidates (->> (http/get (str ENDPOINT "/articles_new/_design/search/_search/search?include_docs=true&limit=10&query="(generate-search-query tags pub))
                                 {:basic-auth [USERNAME PASSWORD]
                                  :as :json})
                        :body)]
    (->> (:rows candidates)
         (filter #(> (first (:order %)) 0.2)))))

(defn get-similar [id]
  (let
      [art (->> (http/get (str ENDPOINT "/arcticles_new/" id)
                       {:basic-auth [USERNAME PASSWORD]
                        :as :json})
                :body)
       tags (:tags art)
       pub (:publication art)
       ]
    (get-by-tags tags pub)))

(defn search [term]
  (->> (http/get (str ENDPOINT "/articles_new/_design/search/_search/search?include_docs=true&limit=15&query=" term)
                 {:basic-auth [USERNAME PASSWORD]
                  :as :json})
       :body
       :rows))
(defn top-publishers []
  (->> (http/get (str ENDPOINT "/articles_new/_design/_stats/_view/count_publishers_new?reduce=true&group=true")
                 {:basic-auth [USERNAME PASSWORD]
                  :as :json})
       (:body)
       (:rows)
       (sort-by :value)
       (reverse)
       (take 10)
       )
  )

(defn biggest-datasets []
  (->> (http/get (str ENDPOINT "/articles_new/_design/_stats/_view/_concat_text?reduce=true&group=true")
                 {:basic-auth [USERNAME PASSWORD]
                  :as :json})
       (:body)
       (:rows)
       (sort-by #(count (:value %)))
       (reverse)
       (take 20)))

(defn insert-personality [site-data]
  (->> (http/post (str ENDPOINT "/pub_data/")
                  {:basic-auth [USERNAME PASSWORD]
                   :form-params {:_id (:name site-data)
                                 :results (:results site-data)}
                   :content-type :json
                   })
       ))

(defn lookup-personality [name]
  (try
    (->> (http/get (str ENDPOINT "/pub_data/" name)
                   {:basic-auth [USERNAME PASSWORD]
                    :as :json})
         :body)
    (catch Exception e nil)))

(defn delete-article [id]
  (let [article (get-article id)]
    (http/delete (str ENDPOINT "/articles_new/" id "?rev=" (:_rev article))
                 {:basic-auth [USERNAME PASSWORD]})))

(defn dedup-db []
  (let [rows(->> (http/get (str ENDPOINT "/articles_new/_design/_stats/_view/dedup?reduce=true&group=true")
                           {:basic-auth [USERNAME PASSWORD]
                            :as :json})
                 :body
                 :rows)]
    (doseq [row rows]
      (when (> (count (:value row)) 1)
        (doseq [id (drop 1 (:value row))]
          (delete-article id))))))

(defn upload-article [article]
  (http/post (str ENDPOINT "/articles_new/")
             {:basic-auth [USERNAME PASSWORD]
              :form-params article
              :content-type :json}))

