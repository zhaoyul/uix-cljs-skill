(ns app.react-interop
  (:require
    [cljs-bean.core :as bean]
    [uix.core :as uix :refer [defui $]]
    ["react-simple-maps" :refer [Geographies Geography]]))

(defui geography-list [{:keys [geographies]}]
  ($ :<>
    (for [geo geographies]
      ($ Geography {:key (:rsm-key geo)
                    :geography geo}))))

(defui map-view []
  ($ Geographies {:geography "/assets/features.json"
                  :fill "#D6D6DA"
                  :stroke "#FFFFFF"}
    (fn [js-props]
      ($ geography-list (bean/->clj js-props)))))
