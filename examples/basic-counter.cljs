(ns app.core
  (:require
    [uix.core :as uix :refer [defui $]]
    [uix.dom :as uix.dom]))

(defui button [{:keys [on-click children]}]
  ($ :button.btn {:type :button
                  :on-click on-click}
    children))

(defui app []
  (let [[n set-n!] (uix/use-state 0)]
    ($ :main.container
      ($ :h1 "UIx counter")
      ($ :div.counter
        ($ button {:on-click #(set-n! dec)} "-")
        ($ :span.value n)
        ($ button {:on-click #(set-n! inc)} "+")))))

(defonce root
  (uix.dom/create-root (js/document.getElementById "root")))

(defn start []
  (uix.dom/render-root ($ app) root))
