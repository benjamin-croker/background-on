(ns background-on.nyt
  (:require
    [clj-http.client :as client]
    [cheshire.core :refer [generate-string parse-string]])
  (:gen-class))

;; magic strings
(def KEYS_FILENAME "keys.json")
(def SNAPSHOT_FILENAME "snapshot.json")

(def ^{:private true} api-keys
  {
    :most-popular-key (System/getenv "MOST_POPULAR")
    :times-newswire-key (System/getenv "TIMES_NEWSWIRE")
  })

(defn html-unescape
  "Xml decodes the given string."
  [s]
  (let [unesc (fn [escape-str] (->> escape-str
                                    (re-matches #"\&\#(\d+);")
                                    (second)
                                    (Integer/parseInt)
                                    (char)
                                    (String/valueOf)))]
    (clojure.string/replace s "&amp;" "&")
    (clojure.string/replace s "&gt;" ">")
    (clojure.string/replace s "&lt;" "<")
    (clojure.string/replace s "&quot;" "\"")
    (clojure.string/replace s "&apos;" "'")
    (clojure.string/replace s #"\&\#?[a-zA-Z0-9]+;" #(unesc %1))))

(defn api-call
  "makes an HTTP get request to a remote API"
  [uri params]
  (println "API call: " uri params)
  ;; Sleep for 1/5th of a second to avoid NYT API rate limits
  (Thread/sleep 200)
  (let [response (-> (client/get uri {:query-params params :throw-exceptions false})
                     (:body)
                     (parse-string true))]
    ;; only return the response for successful queries
    (if (= (:status response) "OK") response nil)))

(defn get-most-viewed-articles
  "gets the most viewed articles from the NYT home page"
  []
  (api-call
    "http://api.nytimes.com/svc/mostpopular/v2/mostviewed/all-sections/1.json"
    {:api-key (:most-popular-key api-keys)}))

(defn urls-most-viewed
  "returns the URLs of the NYT articles from a Most Popular API response"
  [most-viewed-articles]
  (->> most-viewed-articles
       (:results)
       (map :url)))

(defn get-newswire-info
  "gets information about a specific NYT article, specified by URL"
  [article-url]
    (api-call
      "http://api.nytimes.com/svc/news/v3/content.json"
      {:url article-url :api-key (:times-newswire-key api-keys)}))

(defn related-urls
  "returns the list of related URLs from a Newswire API response"
  [newswire-article]
  (->> newswire-article
       (:results)
       ;; note that there will only be one result
       (first)
       (:related_urls)
       (map :url)))

(defn trim-newswire-article
  "selects a subset of fields from a Newswire API response"
  [newswire-article]
  (-> newswire-article
      (:results)
      ;; note that there will only be one result
      (first)
      (select-keys [:title :abstract :url])))

(defn build-daily-snapshot
  "collects data about the top stories and all the relevant related articles"
  []
  (let [most-viewed (->> (get-most-viewed-articles)
                         (urls-most-viewed)
                         (map get-newswire-info)
                         (doall))
        related (->> most-viewed
                     (map #(->> %
                                (related-urls)
                                (map get-newswire-info)
                                (doall)))
                     (doall))]
    (map #(merge (trim-newswire-article %1)
                 {:related (map trim-newswire-article %2)})
          most-viewed related)))

(defn write-daily-snapshot
  "writes the daily NYT article data to disc"
  [daily-snapshot]
  ;; assume that the data returned by the NYT is safe to be unescaped.
  (->> (generate-string daily-snapshot {:pretty true})
       (html-unescape)
       (spit (clojure.java.io/resource SNAPSHOT_FILENAME))))

(defn -main []
  (write-daily-snapshot (build-daily-snapshot)))
