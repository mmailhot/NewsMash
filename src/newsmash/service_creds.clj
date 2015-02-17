(ns newsmash.service_creds
  (:require [cheshire.core :as json]
            [environ.core :refer [env]]))

(def CREDS_MAP
  (json/parse-string (env :vcap-services) true))

(let [watson_rel_creds (->> CREDS_MAP
                            :relationship_extraction
                            first
                            :credentials)]
  (def WATSON
    {:url (:url watson_rel_creds)
     :un (:username watson_rel_creds)
     :pw (:password watson_rel_creds)}))

(let [cloudant_creds (->> CREDS_MAP
                          :cloudantNoSQLDB
                          first
                          :credentials)]
  (def CLOUDANT
    {:url (str "https://" (:host cloudant_creds))
     :un (:username cloudant_creds)
     :pw (:password cloudant_creds)}))
