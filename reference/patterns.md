# UIx core patterns

## Namespace patterns

Minimal browser UI namespace:

```clojure
(ns app.core
  (:require
    [uix.core :as uix :refer [defui $]]
    [uix.dom :as uix.dom]))
```

Use the alias for hooks and helpers:

```clojure
(uix/use-state 0)
(uix/use-effect ...)
(uix/use-callback ...)
(uix/create-context :light)
```

Refer the macros used constantly:

```clojure
:refer [defui $ defhook]
```

## Components

UIx component:

```clojure
(defui avatar [{:keys [src alt]}]
  ($ :img.avatar {:src src :alt alt}))
```

Component with children:

```clojure
(defui panel [{:keys [title children]}]
  ($ :section.panel
    ($ :header.panel-title title)
    ($ :div.panel-body children)))
```

Invocation:

```clojure
($ panel {:title "Profile"}
  ($ avatar {:src "/me.png" :alt "Me"}))
```

## Elements

DOM nodes:

```clojure
($ :div)
($ :button {:type :button :on-click on-save} "Save")
($ :div#root.container.large {:data-test-id :root})
```

Fragment:

```clojure
($ :<>
  ($ :h1 "Title")
  ($ :p "Body"))
```

List rendering:

```clojure
(defui user-list [{:keys [users]}]
  ($ :ul
    (for [{:keys [id name]} users]
      ($ :li {:key id} name))))
```

Always include a stable `:key` for collection children.

## Props

Props are a single Clojure map. Children appear at `:children`.

```clojure
(defui button [{:keys [on-click children]}]
  ($ :button {:on-click on-click} children))
```

Rest props:

```clojure
(defui link-button [{:keys [href children] :& props}]
  ($ :a.button {:href href :& props} children))
```

Spread props:

```clojure
($ :input {:type :text :& input-props})
($ :input {:type :text :& [base-props validation-props]})
```

Spread is top-level only and behaves like shallow merge or `Object.assign`.

## Naming conventions

Translate JS/React names into idiomatic Clojure:

| JS or React | UIx/ClojureScript |
|---|---|
| `isVisible` | `visible?` |
| `isPacked` | `packed?` |
| `setCount` | `set-count!` |
| `onClick` DOM prop | `:on-click` |
| `className` DOM prop | `:class` or selector shorthand |
| `htmlFor` | `:for` |

Do not leave camelCase local names unless using JS interop or React component props that truly require them.

## Memoization

Memoize pure child components only when stable props and render cost justify it.

```clojure
(defui ^:memo row [{:keys [item on-select]}]
  ($ :li {:on-click #(on-select item)} (:title item)))
```

Or:

```clojure
(def row* (uix/memo row))
```

Avoid cargo-cult memoization. In React and UIx, comparing immutable maps can cost more than rerendering small components.
