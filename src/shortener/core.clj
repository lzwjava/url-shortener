(ns shortener.core
  (:use [compojure.core :only (GET PUT POST defroutes)])
  (:require (compojure handler route)
            [ring.util.response :as response])
  (:require [shortener.db :as db]))

(declare app* request)
(def counter (atom 0))

(defn addHttp
  [url]
  (if (.startsWith url "http://")
    url
    (str "http://" url)))

(defn redirect
  [id]
  (let [url (db/get-url-by-id id)]
    (if (seq url)
      (response/redirect url)
      {:status 404
       :body   (format "haven't assign %s" id)})))

(defn shorten!
  [url id]
  (when-not (db/get-url-by-id id)
    (db/insert-db id url)
    id))

(defn retain
  ([url id]
   (if-let [id (shorten! url id)]
     {:status  201
      :headers {"Location" id}
      :body    (format "url %s assigned to %s" url id)}
     {:status 409
      :body   (format "%s is already taken" id)}))
  ([url]
   (let [id (swap! counter inc)
         id (Long/toString id)]
     (or (shorten! url id)
         (recur url)))))

(defn listUrl []
  (flatten (interpose "<br>" (map (fn [m]
                                    (let [{:keys [id url]} m]
                                      (list id "&nbsp;&nbsp;" url)))
                                  (db/select-all)))))

(defroutes
  app*
  (GET "/" request "it's ")
  (GET "/list" [] (listUrl))
  (GET "/assign/:id" [id url]
       (let [url (addHttp url)]
         (retain url id)))
  (GET "/:id" [id] (redirect id))
  (POST "/" [url]
        (if (empty? url)
          {:status 400 :body "no 'url' parameter provided"}
          (retain url)))
  (compojure.route/not-found "Sorry,there's nothing here!"))

(def app (compojure.handler/api app*))

(use '[ring.adapter.jetty :only (run-jetty)])
;(.stop server)
;(.stop @server)
(def server (ref 'a))
(defn run-server []
  ;(use 'ring.adapter.jetty)
  (dosync (ref-set server (run-jetty #'app
                                     {:host "127.0.0.1"
                                      :port 8084 :join? false}))))

(defn init-counter []
  (swap! counter (fn [_] (db/count-db))))

(defn -main [& args]
  (init-counter)
  (run-server))

;(-main)
;(empty? nil)
