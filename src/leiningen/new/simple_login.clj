(ns leiningen.new.simple-login
  (:require [leiningen.new.templates :refer [renderer name-to-path ->files]]
            [leiningen.core.main :as main]))

(def render (renderer "simple-login"))

(defn simple-login
  "Generate the minimal projcet structure for a login/registration page."
  [name]
  (let [data {:name name
              :sanitized (name-to-path name)}]
    (main/info "Generating fresh 'lein new' simple-login project.")
    (->files data
             ["project.clj" (render "project.clj" data)]
             ["README.md" (render "README.md" data)]
             ["LICENSE" (render "LICENSE" data)]
             [".gitignore" (render ".gitignore" data)]
             ["src/{{sanitized}}/handler.clj" (render "handler.clj" data)]
             ["src/{{sanitized}}/views.clj" (render "views.clj" data)]
             ["src/{{sanitized}}/stores.clj" (render "stores.clj" data)]
             ["src/{{sanitized}}/stores/pg.clj" (render "stores/pg.clj" data)]
             ["src/{{sanitized}}/stores/atomstore.clj" (render "stores/atomstore.clj" data)])))
