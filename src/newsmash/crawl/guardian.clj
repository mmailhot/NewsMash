(ns newsmash.crawl.guardian
  (:require [clj-http.client :as http]
            [newsmash.crawl.utils :as utils]
            [newsmash.db :as db]))

(defn load-articles [count page]
  (->> (http/get (str "http://content.guardianapis.com/search?section=commentisfree%7Cus-news%7Cworld%7Cuk-news%7Cbusiness&page-size=" count "&api-key=5mcasfd4fbvb4yr5bum4muhd&show-fields=trailText&page=" page) {:as :json})
       :body
       :response
       :results
       (map
        (fn [article]
          (let [body (utils/slurp_and_parse (:webUrl article))]
            (db/upload-article
             {:title (:webTitle article)
              :url (:webUrl article)
              :publication "The Guardian"
              :summary (utils/strip-html-tags (:trailText (:fields article)))
              :body body
              :tags (utils/analyse_text body)}))))))
