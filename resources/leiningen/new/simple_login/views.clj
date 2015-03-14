(ns {{name}}.views
  (:require [hiccup.core :refer [html]]
            [hiccup.form :refer [form-to password-field email-field submit-button hidden-field label]]
            [hiccup.element :refer [link-to]]
            [ring.middleware.anti-forgery :refer :all]
            [ring.util.response :refer [redirect]]))

(defn unauthorized-template [request & body]
  (html
    [:header
     (when-let [flash (:flash request)]
       [:flash-messages {:class (:type flash)} (:message flash)])
     [:navigation
      (link-to "/login" "Login")
      (link-to "/register" "Register")]]
    [:content body]))

(defn authorized-template [request & body]
  (html
    [:header
     (when-let [flash (:flash request)]
       [:flash-messages {:class (:type flash)} (:message flash)])
     [:navigation
      (link-to "/logout" "Logout")]]
    [:content body]))

(defn home [request]
  (authorized-template
    request
    [:div (get-in request [:session :identity])]))

(defn login [request]
  (unauthorized-template request
            (form-to [:post "/login"]
                     (hidden-field "__anti-forgery-token" *anti-forgery-token*)
                     (hidden-field "next" (get-in request [:query-params :next] "/"))
                     [:div.form-line
                      (label "email" "Email")
                      (email-field "email" (get-in request [:form-params "email"]))]
                     [:div.form-line
                      (label "password" "Password")
                      (password-field "password")]
                     [:div.form-line
                      (submit-button "submit")])))

(defn register [request]
  (unauthorized-template request
            (form-to [:post "/register"]
                     (hidden-field "__anti-forgery-token" *anti-forgery-token*)
                     [:div.form-line
                      (label "email" "Email")
                      (email-field "email" (get-in request [:form-params "email"]))]
                     [:div.form-line
                      (label "password" "Password")
                      (password-field "password")]
                     [:div.form-line
                      (label "password-confirmation" "Password Confirmation")
                      (password-field "password-confirmation")]
                     [:div.form-line
                      (submit-button "submit")])))

(defn error [request]
  (html [:div "There was an error."]))
