(defproject
  newsmash
  "0.1.0-SNAPSHOT"
  :description
  "FIXME: write description"
  :url
  "http://example.com/FIXME"
  :dependencies
  [[prone "0.8.0"]
   [selmer "0.7.7"]
   [com.taoensso/tower "3.0.2"]
   [markdown-clj "0.9.58" :exclusions [com.keminglabs/cljx]]
   [im.chit/cronj "1.4.3"]
   [com.taoensso/timbre "3.3.1"]
   [noir-exception "0.2.3"]
   [http-kit "2.1.19"]
   [lib-noir "0.9.5"]
   [org.clojure/clojure "1.6.0"]
   [environ "1.0.0"]
   [ring/ring-codec "1.0.0"]
   [ring-server "0.3.1"]
   [clj-http "1.0.1"]
   [cheshire "5.4.0"]
   [io.curtis/boilerpipe-clj "0.3.0"]
   [com.github.kyleburton/clj-xpath "1.4.4"]
   [org.clojars.scsibug/feedparser-clj "0.4.0"]]
  :repl-options
  {:init-ns newsmash.repl}
  :jvm-opts
  ["-server"]
  :plugins
  [[lein-ring "0.9.0"] [lein-environ "1.0.0"] [lein-ancient "0.5.5"]]
  :ring
  {:handler newsmash.handler/app,
   :init newsmash.handler/init,
   :destroy newsmash.handler/destroy,
   :uberwar-name "newsmash.war"}
  :profiles
  {:uberjar {:omit-source true, :env {:production true}, :aot :all},
   :production
   {:ring
    {:open-browser? false, :stacktraces? false, :auto-reload? false}},
   :dev
   {:dependencies
    [[ring-mock "0.1.5"]
     [ring/ring-devel "1.3.2"]
     [pjstadig/humane-test-output "0.6.0"]],
    :injections
    [(require 'pjstadig.humane-test-output)
     (pjstadig.humane-test-output/activate!)],
    :env {:dev true}}}
  :main
  newsmash.core
  :min-lein-version "2.0.0")
