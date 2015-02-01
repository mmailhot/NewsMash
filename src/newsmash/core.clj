(ns newsmash.core
  (:require
    [newsmash.handler :refer [app]]
    [ring.middleware.reload :as reload]
    [org.httpkit.server :as http-kit]
    [taoensso.timbre :as timbre]
    [environ.core :refer [env]])
  (:gen-class))

;contains function that can be used to stop http-kit server
(defonce server
  (atom nil))

(defn dev? [args] (some #{"-dev"} args))

(defn parse-port [args]
  (if (env :port) (Integer. (env :port)) (if (env :vcap_app_port) (Integer. (env :vcap_app_port)) 3000)))

(defn- start-server [port args]
  (reset! server
          (http-kit/run-server
           (if (dev? args) (reload/wrap-reload app) app)
           {:port port})))

(defn- stop-server []
  (@server))

(defn -main [& args]
  (let [port (parse-port args)]
    (start-server port args)
    (timbre/info "server started on port:" port)))
