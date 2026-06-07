# Reagent to UIx migration guide

## Strategy

Migrate incrementally unless a full rewrite is explicitly requested.

Good migration order:

1. New components in UIx.
2. Leaf components with few dependencies.
3. Shared presentational components.
4. Components that need modern React hooks or third-party React interop.
5. State-heavy screens after bridge hooks are in place.

Keep Reagent islands alive temporarily with `r/as-element`.

## Syntax map

### Elements

Reagent:

```clojure
[:div#id.class {:on-click f}
 [:span "Hello"]]
```

UIx:

```clojure
($ :div#id.class {:on-click f}
  ($ :span "Hello"))
```

### Components

Reagent:

```clojure
(defn avatar [{:keys [src]}]
  [:img {:src src}])
```

UIx:

```clojure
(defui avatar [{:keys [src]}]
  ($ :img {:src src}))
```

### Local state

Reagent:

```clojure
(defn counter []
  (r/with-let [state (r/atom 0)]
    [:button {:on-click #(swap! state inc)} @state]))
```

UIx:

```clojure
(defui counter []
  (let [[n set-n!] (uix/use-state 0)]
    ($ :button {:on-click #(set-n! inc)} n)))
```

### Props and children

Reagent positional children:

```clojure
(defn button [{:keys [on-click]} & children]
  (into [:button {:on-click on-click}] children))

[button {:on-click save!} "Save"]
```

UIx children in props:

```clojure
(defui button [{:keys [on-click children]}]
  ($ :button {:on-click on-click} children))

($ button {:on-click save!} "Save")
```

### Keys

Reagent metadata or attr key:

```clojure
[:ul
 (for [item items]
   ^{:key (:id item)}
   [:li (:name item)])]
```

UIx key attr:

```clojure
($ :ul
  (for [item items]
    ($ :li {:key (:id item)} (:name item))))
```

### Shared state

Reagent often uses namespace-level `r/atom`. UIx and React generally share state through the UI tree via context, or through bridge hooks for external stores.

UIx context approach:

```clojure
(def state-context (uix/create-context nil))

(defui child-component []
  (let [[n set-n!] (uix/use-context state-context)]
    ($ :button {:on-click #(set-n! inc)} n)))

(defui parent-component []
  (let [[n set-n!] (uix/use-state 0)]
    ($ state-context {:value [n set-n!]}
      ($ child-component))))
```

## Translation checklist

- Replace Hiccup vectors with `$` forms.
- Replace component `defn` with `defui`.
- Convert multiple args and rest args to one props map.
- Move children into `:children`.
- Replace local `r/atom` or `r/with-let` with `use-state`, `use-ref`, or `use-effect` depending on intent.
- Add `:key` to list elements.
- Replace direct ratom/re-frame reads with bridge hooks.
- Replace lifecycle-ish code with `use-effect` or `use-layout-effect`.
- Keep external side effects out of render.
- When preserving a Reagent child temporarily, wrap it with `r/as-element`.

## Common migration mistakes

- Returning Hiccup from `defui`.
- Calling a UIx component as a normal function.
- Passing positional args to UIx components.
- Dereferencing ratoms in UIx render and expecting reactive updates.
- Forgetting that hooks must not be conditional.
- Re-rendering from the root while using react-refresh and wondering why state resets.
