(ns newsmash.crawl.utils
  (:require [clj-http.client :as http]
            [boilerpipe-clj.core :as parser]
            [clojure.string :as string]
            [environ.core :refer [env]]
            [ring.util.codec :refer [form-encode]]
            )
  (:use [clj-xpath.core]))

(defn slurp_and_parse [url]
  (parser/get-text (slurp url)))

(def SERVICE_URL "https://gateway.watsonplatform.net/laser/service/api/v1/sire/241817e3-dd6e-44c4-9c77-38b8ecbadfd7")

(def SERVICE_UN "f44f32cf-6d63-42d5-8742-d5d79091b1ee")

(def SERVICE_PW (env :watson-pw))

(def STOP_WORDS (string/split (slurp "./stop-words.txt") #","))

(defn is_stop_word? [word]
  (some #(= (string/trim (string/lower-case word)) %) STOP_WORDS))

(defn analyse_text [text]
  (if (string/blank? text) []
  (let [analysis (http/post SERVICE_URL
                            {:accept :plain
                             :basic-auth [SERVICE_UN SERVICE_PW]
                             :throw-entire-message? true
                             :content-type "application/x-www-form-urlencoded"
                             :body  (form-encode {:sid "ie-en-news"
                                       :txt text
                                       :rt "xml"})})]
    (->> ($x "//entity" (:body analysis))
         (filter #(let [type (-> % :attrs :type)]
                    (or (= type "GPE") (= type "PERSON") (and (.startsWith type "EVENT") (not (= type "EVENT_COMMUNICATION"))))))
         (map (fn [entity]
                (let [type (-> entity :attrs :type)]
                  (->> @(:children entity)
                       (map :text)
                       (filter #(not (= "" (string/trim %))))
                       (filter #(not (is_stop_word? %)))
                       (map #(str type "--" (string/lower-case %)))))))
         flatten)
  )))

(defn strip-html-tags [str]
    (.replaceAll (.replaceAll str "<.*?>" " ") "[\t\n\r ]+" " "))
