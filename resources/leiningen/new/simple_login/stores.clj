(ns {{name}}.stores
  (:require [clojure.java.jdbc :as jdbc]))

(defprotocol IUsers
  (find-user-by-email [this email])
  (find-user-by-id [this id])
  (put-user! [this user]))

(def user-id (atom 0))

(defrecord AtomStore [data]
  IUsers
  (find-user-by-email [this email]
    (first (filter (fn [user] (= email (:email user)))
                   (get (deref (:data this)) :users))))
  (find-user-by-id [this id]
    (first (filter (fn [user] (= id (:id user)))
                   (get (deref (:data this)) :users))))
  (put-user! [this user]
    (let [user (assoc user :id (swap! user-id inc))]
      (swap! (:data this) update-in [:users] conj user)
      user)))

(defrecord JdbcStore [conn]
  IUsers
  (find-user-by-email [this email]
    (first (jdbc/query (:conn this)
                       ["SELECT * FROM users WHERE email = ?" email])))
  (find-user-by-id [this id]
    (first (jdbc/query (:conn this)
                       ["SELECT * FROM users WHERE id = ?" id])))
  (put-user! [this user]
    (first (jdbc/insert! (:conn this) :users user))))
