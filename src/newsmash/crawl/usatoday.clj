(ns newsmash.crawl.usatoday
  (:require [clj-http.client :as http]
            [newsmash.crawl.utils :as utils]
            [newsmash.db :as db]))

(defn load-articles [count page]
  (->> (http/get (str "http://api.usatoday.com/open/articles/topnews/news?count=" count "&days=0&encoding=json&api_key=cmfuzf8rqak75vcnt5bcbr66&page=" page) {:as :json})
       :body
       :stories
       (map
        (fn [article]
          (let [body (utils/slurp_and_parse (:link article))]
            (db/upload-article
             {:title (:title article)
              :url (:link article)
              :publication "USA Today"
              :summary (utils/strip-html-tags (:description article))
              :body body
              :tags (utils/analyse_text body)}))))))
