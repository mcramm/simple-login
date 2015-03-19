(ns {{name}}.stores
  (:require [clojure.java.jdbc :as jdbc]
            [environ.core :refer [env]])
  (:import com.mchange.v2.c3p0.ComboPooledDataSource))

;; Atom Store
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defrecord AtomStore [data])

(defn create-atom-store []
  (->AtomStore (atom {:users []})) )

;; JDBC Store
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defrecord JdbcStore [conn])

(defn pool [spec]
  (let [cpds (doto (ComboPooledDataSource.)
               (.setDriverClass  (:classname spec))
               (.setJdbcUrl  (str "jdbc:"  (:subprotocol spec) ":"  (:subname spec)))
               (.setUser  (:user spec))
               (.setPassword  (:password spec))
               ;; expire excess connections after 30 minutes of inactivity:
               (.setMaxIdleTimeExcessConnections  (* 30 60))
               ;; expire connections after 3 hours of inactivity:
               (.setMaxIdleTime  (* 3 60 60)))]
    {:datasource cpds}))

(defn create-pg-store [spec]
  (let [pooled-db (delay (pool spec))
        db-connection (fn [] @pooled-db)]
    (->JdbcStore (db-connection))))

(defonce db-spec {:subprotocol "postgresql"
                  :subname     (env :database-url)
                  :user        (env :database-user)
                  :password    (env :database-password)})
