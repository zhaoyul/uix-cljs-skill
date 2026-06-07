# UIx hooks reference

## State

```clojure
(defui counter []
  (let [[n set-n!] (uix/use-state 0)]
    ($ :button {:on-click #(set-n! inc)} n)))
```

The setter accepts either a value or an updater function.

Lazy initialization:

```clojure
(defn calculate-initial-value []
  ;; expensive work
  42)

(defui widget []
  (let [[value set-value!] (uix/use-state calculate-initial-value)]
    ($ :div value)))
```

## Effects

Use effects for external work: DOM APIs, timers, subscriptions, network calls, logging, imperative APIs.

```clojure
(uix/use-effect
  (fn []
    (set! (.-title js/document) title))
  [title])
```

Cleanup:

```clojure
(uix/use-effect
  (fn []
    (let [handler #(js/console.log "resize")]
      (.addEventListener js/window "resize" handler)
      #(.removeEventListener js/window "resize" handler)))
  [])
```

UIx converts non-function effect returns to `js/undefined`. Still avoid accidentally returning a function unless it is intentionally a cleanup.

Danger pattern:

```clojure
(uix/use-effect
  (fn []
    (map inc))
  [])
```

`(map inc)` returns a function, so React treats it as cleanup.

## Dependencies

Use vector literals, not JS arrays and not dynamic collections.

```clojure
(uix/use-effect #(load-user! id) [id])
(uix/use-memo #(expensive items filters) [items filters])
(uix/use-callback #(dispatch! [:save id]) [dispatch! id])
```

UIx compares dependency values with `clojure.core/=`, so immutable maps and vectors are acceptable deps.

## Rules of hooks

Bad:

```clojure
(defui component [{:keys [active?]}]
  (when active?
    (uix/use-effect #(js/console.log "active") []))
  ($ :div))
```

Good:

```clojure
(defui component [{:keys [active?]}]
  (uix/use-effect
    (fn []
      (when active?
        (js/console.log "active")))
    [active?])
  ($ :div))
```

Bad:

```clojure
(for [item items]
  ($ row {:on-click (uix/use-callback #(select! item) [item])}))
```

Good:

```clojure
(defui row [{:keys [item select!]}]
  (let [on-click (uix/use-callback #(select! item) [select! item])]
    ($ :button {:on-click on-click} (:name item))))
```

## Refs

`use-ref` returns an atom-like object.

```clojure
(defui focus-input []
  (let [ref (uix/use-ref)]
    ($ :form
      ($ :input {:ref ref})
      ($ :button {:type :button
                  :on-click #(.focus @ref)}
        "Focus"))))
```

Read with `@ref`, write with `reset!`, update with `swap!`.

```clojure
(reset! ref node)
(swap! ref update :count inc)
```

Do not use `(.-current ref)` for UIx refs unless you are deliberately writing low-level interop.

## Custom hooks

Use `defhook` so UIx can enforce hook naming and lint hook bodies.

```clojure
(defhook use-event-listener [target type handler]
  (uix/use-effect
    (fn []
      (.addEventListener target type handler)
      #(.removeEventListener target type handler))
    [target type handler]))
```

Hook names must start with `use-`.

## External state

For quick shared state:

```clojure
(defonce state (atom 0))

(defui value []
  (let [n (uix/use-atom state)]
    ($ :span n)))

(defui increment []
  ($ :button {:on-click #(swap! state inc)} "+"))
```

For app-scale shared state, prefer React context, re-frame bridge hooks, or a dedicated external store approach.
