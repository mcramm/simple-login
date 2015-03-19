(ns {{name}}.auth
  (:require [ring.util.response :refer [redirect]]
            [crypto.password.bcrypt :as bcrypt]
            [clojure.string :as string]
            [taoensso.timbre :as log]

            [{{name}}.views :as views]
            [{{name}}.user :as user]))
;; Login
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn respond-could-not-find-user
  ([request] (respond-could-not-find-user request nil))
  ([request message]
   (when message (log/info message))
   (-> request
       (assoc :flash {:type "error" :message "Could not find a user with that email/password."})
       views/login)))

(defn log-in-user [user request]
  (let [nexturl (get-in request [:form-params :next] "/")
        session (get request :session)
        session (assoc session :identity (:id user))]
    (log/info (format "Redirecting to %s" nexturl))
    (-> (redirect nexturl)
        (assoc :session session))))

(defn login-authenticate [request]
  (let [email (string/lower-case (get-in request [:form-params "email"]))
        password (get-in request [:form-params "password"])
        user (user/find-user-by-email (:store request) email)]
    (log/info (format "Authenticating for %s" email))
    (cond
      (not user) (respond-could-not-find-user request (format "Could not find user with email %s." email))
      (not (bcrypt/check password (:encrypted_password user))) (respond-could-not-find-user request
                                                                                            (format "Password miss-match for %s." email))
      :else (log-in-user user request))))

;; Registration
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn register-user [{:keys [store] :as request}]
  (let [email (string/lower-case (get-in request [:form-params "email"]))
        password (get-in request [:form-params "password"])
        password-confirmation (get-in request [:form-params "password-confirmation"])
        session (get request :session)]
    (log/info (format "Registering %s" email))
    (cond
      (not= password password-confirmation) (-> request (assoc :flash {:type "error" :message "Passwords do not match."}) views/register)
      (user/find-user-by-email store email) (-> request (assoc :flash {:type "error" :message "That email has already been taken."}) views/register)

      :else (let [encrypted-pass (bcrypt/encrypt password)
                  user (user/put-user! store (user/build-user email encrypted-pass))
                  session (assoc session :identity (:id user))]
              (-> (redirect "/")
                  (assoc :session session))))))

(defn logout [request]
  (-> (redirect "/login")
      (assoc :session {})))

(defn current-user [request]
  (user/find-user-by-id (:store request)
                        (get-in request [:session :identity])))
