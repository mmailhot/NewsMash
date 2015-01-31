(ns newsmash.crawl.rss
  (:require [clj-http.client :as http]
            [newsmash.crawl.utils :as utils]
            [newsmash.db :as db]
            [feedparser-clj.core :refer [parse-feed]]))

(defn load-rss [url name]
  (->> (parse-feed url)
      :entries
      (map
       (fn [entry]
         (let [body (utils/slurp_and_parse (:link entry))]
           (db/upload-article
            {:title (:title entry)
             :url (:link entry)
             :publication name
             :summary (utils/strip-html-tags (:value (:description entry)))
             :body body
             :tags (utils/analyse_text body)}))))))

(def feeds
  [["http://rss.cnn.com/rss/cnn_topstories.rss" "CNN"]
   ["http://rss.cnn.com/rss/cnn_world.rss" "CNN"]
   ["http://rss.cnn.com/rss/cnn_us.rss" "CNN"]
   ["http://rss.cnn.com/rss/cnn_allpolitics.rss" "CNN"]
   ["http://rss.cbc.ca/lineup/world.xml" "CBC News"]
   ["http://rss.cbc.ca/lineup/canada.xml" "CBC News"]
   ["http://feeds.bbci.co.uk/news/world/rss.xml" "BBC News"]
   ["http://feeds.bbci.co.uk/news/uk/rss.xml" "BBC News"]
   ["http://feeds.bbci.co.uk/news/politics/rss.xml" "BBC News"]
   ["http://www.economist.com/sections/united-states/rss.xml" "The Economist"]
   ["http://www.economist.com/sections/international/rss.xml" "The Economist"]
   ["http://www.economist.com/sections/business-finance/rss.xml" "The Economist"]
   ["http://www.dailymail.co.uk/news/index.rss" "The Daily Mail"]
   ["http://www.dailymail.co.uk/news/worldnews/index.rss" "The Daily Mail"]
   ["http://feeds.washingtonpost.com/rss/politics" "The Washington Post"]
   ["http://feeds.washingtonpost.com/rss/world" "The Washington Post"]
   ["http://feeds.washingtonpost.com/rss/national" "The Washington Post"]
   ["http://feeds.washingtonpost.com/rss/opinions" "The Washington Post"]
   ["http://www.thestar.com/feeds.topstories.rss" "Toronto Star"]
   ["http://www.thestar.com/feeds.articles.opinion.rss" "Toronto Star"]
   ["http://feeds.foxnews.com/foxnews/latest" "Fox News"]
   ["http://feeds.foxnews.com/foxnews/politics" "Fox News"]
   ["http://www.npr.org/rss/rss.php?id=1001" "NPR News"]
   ["http://www.npr.org/rss/rss.php?id=1012" "NPR News"]
   ["http://www.npr.org/rss/rss.php?id=1012" "NPR News"]])

(defn load-all []
  (doall (pmap (fn [feed]
                 (print (str "Loading " (first feed)))
                 (doall (load-rss (first feed)
                                  (second feed)))
                 (print (str "Loaded " (first feed))))
               feeds)))

