(ns app.ui
  (:require [uix.core :refer [defui $]]))

(defui title-bar []
  ($ :div.title-bar
    ($ :h1 "Hello from UIx SSR")
    ($ :button {:type :button
                :on-click #?(:cljs #(js/console.log "clicked")
                             :clj nil)}
      "+")))
