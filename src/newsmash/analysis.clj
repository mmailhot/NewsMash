(ns newsmash.analysis
  (:require [cheshire.core :as json]
            [clj-http.client :as http]
            [environ.core :refer [env]]
            [newsmash.db :as db]
            ))

(def ENDPOINT "https://gateway.watsonplatform.net/systemu/service/api/v2/profile")
(def USERNAME "8df4abfa-ab94-4846-91a4-8fdb6eecf8de")
(def PASSWORD (env :personality-pw))

(defn extractFive [root]
  (->> root
       :children
       first
       :children
       first
       :children
       (map (fn [node]
              {:name (:name node)
               :value (:percentage node)}))))

(defn extractValues [root]
  (->> root
       :children
       (drop 2)
       first
       :children
       first
       :children
       (map (fn [node]
              {:name (:name node)
               :value (:percentage node)}))))


(defn analyse_text [text]
  (->> (http/post ENDPOINT
                  {:form-params
                   {:contentItems
                     [{
                       :id "Dummy"
                       :userid "DummyUID"
                       :sourceid "freetext"
                       :contenttype "text/plain"
                       :language "en"
                       :content text}]}
                   :content-type :json
                   :throw-entire-message? true
                   :basic-auth [USERNAME PASSWORD]
                   :as :json})
       :body
       :tree
       ((fn [root]
         {:main (extractFive root)
          :values (extractValues root)}))
       ))

(defn full_analyse []
  (->> (db/biggest-datasets)
       (map (fn [pub]
              {:name (:key pub)
               :results (analyse_text (:value pub))}))
       (map db/insert-personality)))
