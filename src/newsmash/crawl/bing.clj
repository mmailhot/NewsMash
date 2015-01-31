(ns newsmash.crawl.bing
  (:require [clj-http.client :as http]
            [newsmash.crawl.utils :as utils]
            [newsmash.db :as db]
            [environ.core :refer [env]]
            ))

(def bing-key (env :bing-key))

(defn load-articles [term count page]
  (->> (http/get (str "https://api.datamarket.azure.com/Bing/Search/v1/News"
                      "?$top=" count
                      "&$format=json"
                      "&$skip=" (* count (- page 1))
                      "&Query='" term "'")
                 {:basic-auth [bing-key bing-key]
                  :as :json
                  :throw-entire-message? true})
       :body
       :d
       :results
       (map (fn [article]
              (let [body (utils/slurp_and_parse (:Url article))]
                (try
                  (db/upload-article
                   {:title (:Title article)
                    :url (:Url article)
                    :publication (:Source article)
                    :summary (:Description article)
                    :body body
                    :tags (utils/analyse_text body)})
                  (catch Exception e nil)))))))

(def terms ["Tony Abbot" "Super Bowl" "Palestine" "Syriza" "Putin"])

(defn load-all []
  (doseq [term terms
          i [1 2 3 4]]
    (print (str "Loading page " i " for term " term))
    (doall (load-articles term 15 i)))
  )
