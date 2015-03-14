(ns {{name}}.stores.pg
  (:require [{{name}}.stores :as stores])
  (:import com.mchange.v2.c3p0.ComboPooledDataSource))

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

(defn create-store [spec]
  (let [pooled-db (delay (pool spec))
        db-connection (fn [] @pooled-db)]
    (stores/->JdbcStore (db-connection))))
