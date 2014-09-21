(ns background-on.nyt
  (:require
    [clojure.data.json :as json]
    [clj-http.client :as client])
  (:gen-class))

(defn- load-keys
    "loads the keys for accessing the api"
    [keys-filename]
    (json/read-json (slurp (clojure.java.io/resource keys-filename))))

(def ^{:private true} api-keys
  (load-keys "keys.json"))

(defn api-call
  "makes an HTTP get request to a remote API"
  [uri params]
  (println "API call: " uri params)
  ;; Sleep for 1/5th of a second to avoid NYT API rate limits
  (Thread/sleep 200)
  (->>
    (client/get uri {:query-params params})
    (:body)
    (json/read-json)))

(defn get-most-viewed-articles
  "gets the most viewed articles from the NYT home page"
  []
  (api-call
    "http://api.nytimes.com/svc/mostpopular/v2/mostviewed/all-sections/1.json"
    {:api-key (:most_popular_key api-keys)}))

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
      {:url article-url :api-key (:times_newswire_key api-keys)}))

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
                         (map get-newswire-info))
        related (->> (doall most-viewed)
                     (map #(->> %
                                (related-urls)
                                (map get-newswire-info))))]
    (map #(merge (trim-newswire-article %1)
                 {:related (map trim-newswire-article %2)})
          most-viewed related)))
