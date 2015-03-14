(ns {{name}}.handler
  (:require [compojure.core :refer :all]
            [compojure.route :as route]
            [clojure.string :as string]

            [ring.middleware.defaults :refer [wrap-defaults site-defaults]]
            [ring.util.response :refer [redirect]]

            [buddy.auth :refer [authenticated?]]
            [buddy.auth.backends.session :refer  [session-backend]]
            [buddy.auth.middleware :refer  [wrap-authentication wrap-authorization]]
            [buddy.auth.accessrules :refer [wrap-access-rules error]]

            [environ.core :refer [env]]
            [taoensso.timbre :as log]
            [crypto.password.bcrypt :as bcrypt]

            [{{name}}.stores :as stores]
            [{{name}}.stores.atomstore :as atomstore]
            [{{name}}.stores.pg :as pg]
            [{{name}}.views :as views]))

(log/set-config! [:appenders :spit :enabled?] true)

(def store (atomstore/create-store))

;; Uncomment below to use a postgrs-backed store instead
; (defonce db-spec {:subprotocol "postgresql"
;                   :subname     (env :database-url)
;                   :user        (env :database-user)
;                   :password    (env :database-password)})

; (def store (pg/create-store db-spec))


(defn authenticated-user [request]
  (authenticated? request))

(def rules [{:uri "/"
             :handler authenticated-user}])

(defn logout [request]
  (-> (redirect "/login")
      (assoc :session {})))

(defn build-user [email encrypted-pass]
  {:email email :encrypted_password encrypted-pass})

(defn login-authenticate [request]
  (let [email (string/lower-case (get-in request [:form-params "email"]))
        password (get-in request [:form-params "password"])
        session (get request :session)]
    (log/info (format "Authenticating for %s" email))
    (if-let [user (stores/find-user-by-email store email)]
      (if (bcrypt/check password (:encrypted_password user))
        (let [nexturl (get-in request [:form-params :next] "/")
              session (assoc session :identity (:id user))]
          (log/info (format "Redirecting to %s" nexturl))
          (-> (redirect nexturl)
              (assoc :session session)))
        (do (log/info (format "Password miss-match for %s, redirecting to login" email))
            (-> request
                (assoc :flash {:type "error" :message "Could not find a user with that email/password."})
                views/login)))
      (do (log/info (format "Could not find user with email %s, redirecting to login" email))
            (-> request
                (assoc :flash {:type "error" :message "Could not find a user with that email/password."})
                views/login)))))

(defn register-user [request]
  (let [email (string/lower-case (get-in request [:form-params "email"]))
        password (get-in request [:form-params "password"])
        password-confirmation (get-in request [:form-params "password-confirmation"])
        session (get request :session)]
    (log/info (format "Registering %s" email))
    (if-not (= password password-confirmation)
      (do (log/info "Passwords do not match")
          (-> request
              (assoc :flash {:type "error" :message "Passwords do not match."})
              views/register))
      (if-let [user (stores/find-user-by-email store email)]
        (do (log/info (format "Existing user found for %s" email))
            (-> request
                (assoc :flash {:type "error" :message "That email has already been taken."})
                views/register))
        (let [encrypted-pass (bcrypt/encrypt password)
              user (stores/put-user! store (build-user email encrypted-pass))
              session (assoc session :identity (:id user))]
          (-> (redirect "/")
              (assoc :session session)))))))

(defn current-user [request]
  (stores/find-user-by-id store
                          (get-in request [:session :identity])))

(defroutes app-routes
  (GET "/" [] views/home)
  (GET "/login" [] views/login)
  (POST "/login" [] login-authenticate)
  (GET "/logout" [] logout)
  (GET "/register" [] views/register)
  (POST "/register" [] register-user)
  (route/not-found "Not Found"))

(defn unauthorized-handler
  [request metadata]
  (cond
    (authenticated? request)
    (-> (assoc request :status 403) (views/error))

    :else
    (let [current-url (:uri request)]
      (redirect (format "/login?next=%s" current-url)))))

(def auth-backend
  (session-backend {:unauthorized-handler unauthorized-handler}))

(def app
  (-> app-routes
      (wrap-access-rules {:rules rules :on-error unauthorized-handler})
      (wrap-authorization auth-backend)
      (wrap-authentication auth-backend)
      (wrap-defaults site-defaults)))
