---
name: uix-clojurescript
description: Use this skill when building, reviewing, debugging, testing, or translating code for pitch-io/UIx, the idiomatic ClojureScript interface to modern React. Covers defui, $, hooks, props, React/Reagent interop, SSR, testing, code splitting, linting, and Reagent-to-UIx migration.
compatibility: pi agent, Claude Code, Codex
---

# UIx ClojureScript Skill

## Mission

Help the user produce idiomatic, compile-ready UIx ClojureScript code. UIx is close to React's mental model, but it uses Clojure data, macros, kebab-case APIs, dependency vectors, and compile-time guard rails. Your job is to keep those worlds stitched together without dropping JSX crumbs on the floor.

When answering code requests:

- Prefer complete namespaces and complete modified files, not fragments, unless the user explicitly asks for a small patch.
- Use UIx primitives first: `defui`, `$`, `defhook`, `uix/use-state`, `uix/use-effect`, `uix/use-memo`, `uix/use-callback`, `uix/use-ref`, `uix/use-context`.
- Do not emit JSX, Reagent Hiccup, or raw `React.createElement` unless the task explicitly asks for interop or comparison.
- Use kebab-case for component names and prop names, and idiomatic predicate names such as `visible?`, `packed?`, `disabled?`.
- Preserve React's rendering model: pure render functions, hooks at top level, stable keys for lists, explicit dependencies for memo/effect hooks.
- When dependency versions matter, verify the current README, Clojars, npm, or the user's project files. The included defaults reflect the repository snapshot this skill was authored from.

## Load the right reference

Use these companion files when the task goes beyond a tiny answer:

- `reference/patterns.md`: core UIx syntax and design rules.
- `reference/hooks.md`: hooks, deps vectors, refs, effects, custom hooks.
- `reference/interop.md`: React, JS libraries, Reagent, re-frame, refs, props conversion.
- `reference/migration.md`: Reagent-to-UIx conversion map.
- `reference/testing-ssr-build.md`: setup, testing, SSR, code splitting, hot reload.
- `reference/review-checklist.md`: review and debugging checklist.
- `examples/`: copyable mini examples.
- `templates/`: starter files for a browser app.

## UIx baseline

Canonical namespace shape:

```clojure
(ns app.core
  (:require
    [uix.core :as uix :refer [defui $ defhook]]
    [uix.dom :as uix.dom]))
```

Basic component:

```clojure
(defui button [{:keys [on-click children]}]
  ($ :button.btn {:on-click on-click}
    children))
```

Basic app root:

```clojure
(defonce root
  (uix.dom/create-root (js/document.getElementById "root")))

(defn start []
  (uix.dom/render-root ($ app) root))
```

## Essential UIx rules

### Components

- Create UIx components with `defui`.
- A `defui` component receives zero or one argument. When present, it is one props map.
- Child elements are available at `:children` in the props map.
- Component invocation uses `($ component {:prop value} child1 child2)`.
- Memoize only when it matters: `^:memo` or `uix/memo` for stable pure child components.

```clojure
(defui card [{:keys [title children]}]
  ($ :section.card
    ($ :h2 title)
    ($ :div.card-body children)))
```

### Elements

Use `$` for DOM nodes, UIx components, and JS React components.

```clojure
($ :button#save.btn.primary {:disabled false} "Save")
($ card {:title "Settings"} ($ :p "Ready"))
($ SomeJsComponent {:some-prop 1})
```

DOM tag keywords support CSS-selector-like shorthand:

- `:div#root.container` means tag `div`, id `root`, class `container`.
- Props map is optional.
- DOM attrs use kebab-case keywords, such as `:on-click`, `:data-test-id`, `:auto-play`.

### Props rest and spread

Use `:&` for rest props and top-level spread. This is especially useful when combining CLJ maps with props returned as JS objects by third-party libraries.

```clojure
(defui text-input [{:keys [label] :& props}]
  ($ :label
    ($ :span label)
    ($ :input {:class :field :& props})))
```

Multiple spreads:

```clojure
($ :input {:type :text :& [base-props extra-props]})
```

### Hooks

- Call hooks only at the top level of `defui` or `defhook` bodies.
- Use Clojure vectors for dependencies, never JS arrays.
- `use-memo` and `use-callback` require a deps vector.
- UIx compares deps with `clojure.core/=`, so immutable maps and vectors are safe deps.
- `use-effect` normalizes non-function returns to `js/undefined`, but an accidentally returned function will be treated as cleanup.
- `use-ref` is atom-like: read with `@ref`, write with `reset!`, update with `swap!`.

```clojure
(defui counter []
  (let [[n set-n!] (uix/use-state 0)
        inc! (uix/use-callback #(set-n! inc) [])]
    (uix/use-effect
      (fn []
        (set! (.-title js/document) (str "Count: " n)))
      [n])
    ($ :button {:on-click inc!} n)))
```

Custom hook:

```clojure
(defhook use-window-title [title]
  (uix/use-effect
    (fn []
      (set! (.-title js/document) title))
    [title]))
```

### Context

Use `uix/create-context` in CLJS-only code. Use `defcontext` for cross-platform `.cljc` UI shared between JVM SSR and browser hydration.

```clojure
(def theme-context (uix/create-context :light))

(defui top-bar []
  (let [theme (uix/use-context theme-context)]
    ($ :header {:data-theme theme} "Top")))
```

### React interop

When using JS React components in `$`:

- Pass a CLJ map at the top level. UIx converts it shallowly to JS.
- Kebab-case keys become camelCase for JS components.
- `:class`, `:for`, and `:charset` become React's `className`, `htmlFor`, and `charSet`.
- Nested maps and collections are not converted, except common `:style` maps and keyword values.
- Use JS objects or arrays explicitly for nested objects or arrays expected by JS libraries.
- Use `uix/as-react` when exposing a UIx component to JS/React.
- Use `uix/forward-ref` only when a third-party React component injects a ref into a UIx component.

```clojure
(ns app.map
  (:require
    [cljs-bean.core :as bean]
    [uix.core :as uix :refer [defui $]]
    ["react-simple-maps" :refer [Geographies Geography]]))

(defui geographies [{:keys [geographies]}]
  ($ :<>
    (for [geo geographies]
      ($ Geography {:key (:rsm-key geo)
                    :geography geo}))))

(defui map-view []
  ($ Geographies {:geography "/assets/features.json"}
    (fn [js-props]
      ($ geographies (bean/->clj js-props)))))
```

### Reagent interop

- Render Reagent inside UIx with `reagent.core/as-element`.
- Render UIx inside Reagent using the `$` macro.
- Do not call `re-frame.core/subscribe` directly inside UIx components. Use `uix.re-frame/use-subscribe`.
- Use `uix.reagent/use-reaction` for Reagent ratoms/reactions.

```clojure
(ns app.bridge
  (:require
    [reagent.core :as r]
    [uix.core :as uix :refer [defui $]]
    [uix.reagent :as ur]
    [uix.re-frame :as urf]))

(def counter (r/atom 0))

(defui title-bar []
  (let [n (ur/use-reaction counter)
        title (urf/use-subscribe [:app/title])]
    ($ :div
      ($ :span title)
      ($ :button {:on-click #(swap! counter inc)} n))))
```

## Workflows

### Translate React/JSX to UIx

When translating React code:

1. Convert each React function component to `defui`.
2. Convert destructured props to a single props map destructuring form.
3. Convert JSX to nested `$` forms.
4. Convert `className` to selector shorthand or `:class`.
5. Convert `onClick`, `onChange`, etc. to kebab-case DOM props such as `:on-click`.
6. Convert `isX` props to Clojure predicates where sensible, for example `isPacked` to `packed?`.
7. Convert arrays rendered by `.map` to `for` or `map`, and add stable `:key` props.
8. Convert React hooks to UIx hooks with vector deps.
9. Replace object spread with `:&` spread.
10. Preserve semantics before polishing style.

### Build a new UIx app

Use the templates in `templates/` as a minimal shadow-cljs browser app. For current versions, prefer the user's project lock files or the latest upstream README.

Typical commands:

```bash
npm i -D react react-dom react-refresh process
clojure -M -m shadow.cljs.devtools.cli watch app
clojure -M -m shadow.cljs.devtools.cli release app
```

### Review or debug UIx code

Look first for the usual dragons hiding under UI leaves:

- Hooks called under `when`, `if`, `for`, `map`, callbacks, or nested functions.
- Missing deps in `use-effect`, `use-memo`, `use-callback`, or `use-layout-effect`.
- Dependencies passed as JS arrays or dynamic collections instead of vector literals.
- Missing `:key` in rendered collections.
- Reagent `subscribe` or ratom deref used directly in UIx render without `use-subscribe` or `use-reaction`.
- Accidental function return from an effect setup.
- Mixed JS objects and CLJ maps in nested props without explicit conversion.
- Components taking multiple positional args instead of one props map.
- Root re-rendering fighting react-refresh and resetting local state.

### Migrate from Reagent

Use incremental migration unless the user asks for a full rewrite:

- New or leaf components first.
- Reagent Hiccup vector forms become `$` forms.
- `defn` components become `defui`.
- Reagent positional component args become a single props map.
- Reagent children become `:children`.
- `r/atom` local component state becomes `uix/use-state`.
- Shared app state should move to React context, re-frame hooks, or an external store bridge.
- Use `r/as-element` for temporary Reagent islands.

### SSR and hydration

For SSR, keep shared UI in `.cljc`, use reader conditionals for platform-specific code, and render on JVM with `uix.dom.server`. Hydrate on the client with `uix.dom/hydrate-root`.

```clojure
;; shared app/ui.cljc
(ns app.ui
  (:require [uix.core :refer [defui $]]))

(defui title-bar []
  ($ :div.title-bar
    ($ :h1 "Hello")
    ($ :button {:on-click #?(:cljs #(js/console.log %) :clj nil)} "+")))
```

### Testing

Prefer React Testing Library for component and hook tests. Use JS ecosystem tools when they reduce ceremony: `global-jsdom/register`, `@testing-library/react`, `@testing-library/user-event`, `msw`, and `shadow.cljs.modern/js-await`.

## Output style for code answers

- Provide complete files when modifying existing code.
- Keep namespace requires sorted and explicit.
- Include install or config snippets when new libraries are introduced.
- Prefer simple Clojure data and pure functions.
- Avoid clever macro gymnastics unless the user is working inside UIx internals.
- Explain tradeoffs briefly after the code, especially for interop, memoization, SSR, and state management.

## Sources and freshness

This skill was built from the public `pitch-io/uix` repository and docs. Core facts captured here include the README quick start, docs for components/elements/hooks/effects/interop/migration/testing/SSR/linting, and the project's own ChatGPT translation prompt. Dependency versions and React compatibility can change, so verify them for greenfield setup or production upgrades.
