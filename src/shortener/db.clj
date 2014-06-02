(ns shortener.db
  (:use (korma db core)))

(declare urls)

(def dbspec
  (sqlite3
    {:db "resources/korma.db"}))

(def db dbspec)

(defentity
  urls
  (pk :id)
  (database db)
  (table :urls))

(defn insert-db [id url]
  (insert urls (values {:id id :url url})))

(defn get-url-by-id [id]
  (:url
   (first
     (select
       urls
       (where {:id id})
       (limit 1)))))

(defn select-all
  []
  (select urls))

(defn count-db []
  (:cnt (first (select urls (aggregate (count :id) :cnt)))))

;(get-url-by-id "hi")
;(insert-db "hi" "http://www.baidu.com")
(select-all)
