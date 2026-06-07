(ns app.bridge
  (:require
    [reagent.core :as r]
    [uix.core :as uix :refer [defui $]]
    [uix.reagent :as ur]))

(def counter (r/atom 0))

(defn old-reagent-label []
  [:strong "Reagent island"])

(defui new-uix-counter []
  (let [n (ur/use-reaction counter)]
    ($ :section
      (r/as-element [old-reagent-label])
      ($ :button {:type :button
                  :on-click #(swap! counter inc)}
        (str "Clicked " n " times")))))
