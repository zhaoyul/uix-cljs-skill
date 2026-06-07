# UIx interop reference

## JS React components in UIx

You can use JS React components directly in `$`.

```clojure
(ns app.ui
  (:require
    [uix.core :as uix :refer [defui $]]
    ["@mui/material/Button" :default Button]))

(defui save-button [{:keys [on-save]}]
  ($ Button {:variant :contained
             :color :primary
             :on-click on-save}
    "Save"))
```

For JS components, UIx shallowly converts the top-level props map:

- `:some-prop` becomes `someProp`.
- `:class` becomes `className`.
- `:for` becomes `htmlFor`.
- `:charset` becomes `charSet`.
- `:style` maps are converted to JS objects.
- Keyword values become strings.
- Nested maps and collections are not deeply converted.

When a nested object or array is expected, build it explicitly:

```clojure
($ Chart {:options #js {:responsive true
                        :plugins #js {:legend #js {:position "bottom"}}}
          :data #js {:labels #js ["A" "B"]
                     :datasets #js []}})
```

## Render props

Render-prop callbacks often receive JS objects. Convert at the boundary.

```clojure
(ns app.geo
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
  ($ Geographies {:geography "/assets/features.json"}
    (fn [js-props]
      ($ geography-list (bean/->clj js-props)))))
```

## UIx components exposed to React

Use `uix/as-react` for a JS-facing component. It receives React props as a shallow bean, so map camelCase names manually.

```clojure
(defui button [{:keys [on-click children]}]
  ($ :button {:on-click on-click} children))

(def Button
  (uix/as-react
    (fn [{:keys [onClick children]}]
      ($ button {:on-click onClick}
        children))))
```

## Ref forwarding

Use `uix/forward-ref` only when a third-party React component injects a ref into your UIx component.

```clojure
(defui menu-button [{:keys [ref children onMouseDown on-click]}]
  ($ :button {:ref ref
              :on-mouse-down onMouseDown
              :on-click on-click}
    children))

(def menu-button-forwarded
  (uix/forward-ref menu-button))
```

React 19 reduces many forward-ref use cases, but third-party libraries may still require a bridge.

## Reagent inside UIx

```clojure
(ns app.bridge
  (:require
    [reagent.core :as r]
    [uix.core :refer [defui $]]))

(defn reagent-component []
  [:div "Hello from Reagent"])

(defui uix-shell []
  ($ :section
    (r/as-element [reagent-component])))
```

## UIx inside Reagent

```clojure
(defui uix-component []
  ($ :div "Hello from UIx"))

(defn reagent-shell []
  [:section
   ($ uix-component)])
```

## Reagent ratoms and re-frame

Do not do this in UIx:

```clojure
(defui bad []
  (let [value @(rf/subscribe [:value])]
    ($ :div value)))
```

Use bridge hooks:

```clojure
(ns app.bridge
  (:require
    [reagent.core :as r]
    [uix.core :as uix :refer [defui $]]
    [uix.reagent :as ur]
    [uix.re-frame :as urf]))

(def local-counter (r/atom 0))

(defui title-bar []
  (let [n (ur/use-reaction local-counter)
        title (urf/use-subscribe [:app/title])]
    ($ :div
      ($ :span title)
      ($ :button {:on-click #(swap! local-counter inc)} n))))
```
