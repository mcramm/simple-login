(ns {{name}}.user
  (:require [clojure.java.jdbc :as jdbc]
            [{{name}}.stores])
  (:import ({{name}}.stores JdbcStore AtomStore)))

(def user-id (atom 0))

(defprotocol IUsers
  (find-user-by-email [this email])
  (find-user-by-id [this id])
  (put-user! [this user]))

(extend-protocol IUsers
  AtomStore
  (find-user-by-email [this email]
    (first (filter (fn [user] (= email (:email user)))
                   (get (deref (:data this)) :users))))
  (find-user-by-id [this id]
    (first (filter (fn [user] (= id (:id user)))
                   (get (deref (:data this)) :users))))
  (put-user! [this user]
    (let [user (assoc user :id (swap! user-id inc))]
      (swap! (:data this) update-in [:users] conj user)
      user))
  JdbcStore
  (find-user-by-email [this email]
    (first (jdbc/query (:conn this)
                       ["SELECT * FROM users WHERE email = ?" email])))
  (find-user-by-id [this id]
    (first (jdbc/query (:conn this)
                       ["SELECT * FROM users WHERE id = ?" id])))
  (put-user! [this user]
    (first (jdbc/insert! (:conn this) :users user))))

(defn build-user [email encrypted-pass]
  {:email email :encrypted_password encrypted-pass})
