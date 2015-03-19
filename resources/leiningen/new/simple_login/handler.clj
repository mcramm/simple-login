(ns {{name}}.handler
  (:require [compojure.core :refer :all]
            [compojure.route :as route]
            [clojure.string :as string]

            [ring.middleware.defaults :refer [wrap-defaults site-defaults]]
            [ring.util.response :refer [redirect]]

            [buddy.auth :refer [authenticated?]]
            [buddy.auth.backends.session :refer  [session-backend]]
            [buddy.auth.middleware :refer  [wrap-authentication wrap-authorization]]
            [buddy.auth.accessrules :refer [wrap-access-rules]]

            [taoensso.timbre :as log]

            [{{name}}.stores :as stores]
            [{{name}}.auth :as auth]
            [{{name}}.views :as views]))

(log/set-config! [:appenders :spit :enabled?] true)

(defn authenticated-user [request]
  (authenticated? request))

(def rules [{:uri "/"
             :handler authenticated-user}])

(defroutes app-routes
  (GET "/" [] views/home)
  (GET "/login" [] views/login)
  (POST "/login" [] auth/login-authenticate)
  (GET "/logout" [] auth/logout)
  (GET "/register" [] views/register)
  (POST "/register" [] auth/register-user)
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

(defn wrap-store [handler]
  (fn [request]
    (-> request
        (assoc :store (stores/create-atom-store))
        handler
        (dissoc :store))))

(def app
  (-> app-routes
      wrap-store
      (wrap-access-rules {:rules rules :on-error unauthorized-handler})
      (wrap-authorization auth-backend)
      (wrap-authentication auth-backend)
      (wrap-defaults site-defaults)))
