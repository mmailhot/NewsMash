(ns newsmash.crawl.utils
  (:require [clj-http.client :as http]
            [boilerpipe-clj.core :as parser]
            [clojure.string :as string]
            [environ.core :refer [env]]
            [ring.util.codec :refer [form-encode]]
            [newsmash.service_creds :refer [WATSON]]
            )
  (:use [clj-xpath.core]))

(defn slurp_and_parse [url]
  (parser/get-text (slurp url)))

(def SERVICE_URL (:url WATSON))

(def SERVICE_UN (:un WATSON))

(def SERVICE_PW (:pw WATSON))

(def STOP_WORDS
  ["a" "able" "about" "across" "after" "all" "almost" "also" "am" "among" "an" "and" "any" "are" "as" "at" "be" "because" "been" "but" "by" "can" "cannot" "could" "dear" "did" "do" "does" "either" "else" "ever" "every" "for" "from" "get" "got" "had" "has" "have" "he" "her" "hers" "him" "his" "how" "however" "i" "if" "in" "into" "is" "it" "its" "just" "least" "let" "like" "likely" "may" "me" "might" "most" "must" "my" "neither" "no" "nor" "not" "of" "off" "often" "on" "only" "or" "other" "our" "own" "rather" "said" "say" "says" "she" "should" "since" "so" "some" "than" "that" "the" "their" "them" "then" "there" "these" "they" "this" "tis" "to" "too" "twas" "us" "wants" "was" "we" "were" "what" "when" "where" "which" "while" "who" "whom" "why" "will" "with" "would" "yet" "you" "your"])

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
