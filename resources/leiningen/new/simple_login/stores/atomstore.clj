(ns {{name}}.stores.atomstore
  (:require [{{name}}.stores :as stores]))

(defn create-store []
  (stores/->AtomStore (atom {:users []})))
