(ns app.core
  (:require
    [uix.core :as uix :refer [defui $]]
    [uix.dom :as uix.dom]))

(defui app []
  (let [[n set-n!] (uix/use-state 0)]
    ($ :main
      ($ :h1 "Hello UIx")
      ($ :button {:type :button
                  :on-click #(set-n! inc)}
        (str "Count: " n)))))

(defonce root
  (uix.dom/create-root (js/document.getElementById "root")))

(defn start []
  (uix.dom/render-root ($ app) root))
