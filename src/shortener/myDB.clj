(ns shortener.myDB
  (:use (korma db core))
  (:require [clojure.java.jdbc :as sql]))

(declare db users sqlite_master)

(def dbspec (sqlite3
              {:db "resources/korma.db"}))
(defdb db dbspec)

(defentity
  users
  (table :user)
  (database db)
  (entity-fields :username :password))

(defentity
  sqlite_master
  (table :sqlite_master)
  (database db))

(select sqlite_master)

;(select users)
;(insert users (values {:username "hi" }))

(defn invoke-with-connection [f]
  (sql/with-connection
    dbspec
    (sql/transaction
      (f))))

(defn addColumn []
  (do
    (sql/do-commands
      "alter table user add column password text")))

(defn selectTables
  []
  (exec-raw ["select * from sqlite_master"] :results))

(defn select-all
  []
  (select users))

(select
  users
  (modifier "distinct")
  (order :username :desc))

(def base (-> (select* users)
              (fields :username)
              (modifier "distinct")))

(-> base
    (where (= :username "hi"))
    (as-sql))

(update
  users
  (set-fields {:username "lzwhi"})
  (where {:username "lzw"}))

;(select-all)

(delete
  users
  (where {:username "hi"}))


;(invoke-with-connection addColumn)
