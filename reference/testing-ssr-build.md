# UIx build, testing, SSR, and code splitting

## Minimal dependencies

Prefer the user's existing versions. For a fresh app, verify current upstream versions before finalizing. A typical setup includes:

```clojure
{:deps {thheller/shadow-cljs {:mvn/version "3.2.1"}
        com.pitch/uix.core {:mvn/version "1.4.9"}
        com.pitch/uix.dom  {:mvn/version "1.4.9"}}}
```

NPM dev dependencies commonly include:

```bash
npm i -D react react-dom react-refresh process
```

## shadow-cljs browser build

```clojure
{:deps true
 :builds
 {:app {:target :browser
        :modules {:main {:entries [app.core]
                         :init-fn app.core/start}}
        :output-dir "out"
        :asset-path "/out"
        :devtools {:preloads [uix.preload]}}}}
```

Development:

```bash
clojure -M -m shadow.cljs.devtools.cli watch app
```

Release:

```bash
clojure -M -m shadow.cljs.devtools.cli release app
```

## Hot reload

Use `uix.preload` in devtools preloads to enable React Refresh behavior.

```clojure
{:builds {:app {:devtools {:preloads [uix.preload]}}}}
```

Avoid root re-render after every reload if relying on React Refresh, because traditional root re-render can reset local state and hooks.

## Code splitting

Use `uix/lazy` with `shadow.lazy` and wrap in `uix/suspense`.

```clojure
(ns app.core
  (:require
    [shadow.lazy]
    [uix.core :as uix :refer [defui $]]))

(def loadable-modal
  (shadow.lazy/loadable app.ui.lib/modal))

(def modal
  (uix/lazy #(shadow.lazy/load loadable-modal)))

(defui app []
  (let [[show-modal? set-show-modal!] (uix/use-state false)]
    ($ :div
      ($ :button {:on-click #(set-show-modal! true)} "Open")
      ($ uix/suspense {:fallback ($ :div "Loading...")}
        (when show-modal?
          ($ modal {:on-close #(set-show-modal! false)}))))))
```

shadow-cljs modules:

```clojure
{:module-loader true
 :modules {:main {:entries [app.core]}
           :ui-lib {:entries [app.ui.lib]
                    :depends-on #{:main}}}}
```

If not using react-refresh and relying on traditional root reload, pass the loadable as the second arg:

```clojure
(def modal
  (uix/lazy #(shadow.lazy/load loadable-modal) loadable-modal))
```

## SSR

Use `uix.dom.server` on JVM.

```clojure
(ns app.server
  (:require
    [uix.core :refer [$]]
    [uix.dom.server :as dom.server]
    [app.ui :as ui]))

(defn render-page []
  (dom.server/render-to-string ($ ui/app)))
```

Use `render-to-static-markup` or streaming variants for non-hydrated static HTML. Use `render-to-string` or streaming variants when the client hydrates into a live app.

Shared UI should live in `.cljc` and protect browser-only APIs:

```clojure
(defui title-bar []
  ($ :button {:on-click #?(:cljs #(js/console.log "clicked")
                           :clj nil)}
    "Click"))
```

Hydrate client-side:

```clojure
(ns app.client
  (:require
    [uix.core :refer [$]]
    [uix.dom :as uix.dom]
    [app.ui :as ui]))

(uix.dom/hydrate-root
  (js/document.getElementById "root")
  ($ ui/app))
```

SSR pitfalls:

- Browser-only APIs on JVM.
- Random values, dates, locale differences, or generated IDs that make server and client markup differ.
- Data fetching inside render.
- Effects expected to run during SSR. They do not.

## Component tests

React Testing Library example:

```clojure
(ns app.form-test
  (:require
    [cljs.test :refer [async deftest is testing use-fixtures]]
    [shadow.cljs.modern :refer [js-await]]
    [uix.core :as uix :refer [defui $]]
    ["global-jsdom/register"]
    ["@testing-library/react" :as rtl]
    ["@testing-library/user-event" :default user-event]))

(defui form []
  (let [[value set-value!] (uix/use-state "")]
    ($ :form
      ($ :input {:data-testid "name"
                 :value value
                 :on-change #(set-value! (.. % -target -value))})
      ($ :p {:data-testid "echo"} value))))

(deftest form-echo-test
  (async done
    (let [screen (rtl/render ($ form))
          input (.getByTestId screen "name")]
      (.type (.setup user-event) input "Kevin")
      (js-await [_ (js/Promise.resolve)]
        (is (= "Kevin" (.-textContent (.getByTestId screen "echo"))))
        (done)))))
```

Install common test deps:

```bash
npm i -D @testing-library/react @testing-library/user-event msw jsdom global-jsdom
```

## Hook tests

```clojure
(ns app.hooks-test
  (:require
    [cljs.test :refer [deftest is testing]]
    [uix.core :as uix :refer [defhook]]
    ["global-jsdom/register"]
    ["@testing-library/react" :as rtl]))

(defhook use-encoded-uri [uri]
  (uix/use-memo #(js/encodeURI uri) [uri]))

(deftest encoded-uri-test
  (testing "encodes URI"
    (let [container (rtl/renderHook #(use-encoded-uri "https://x.test?q= 1"))]
      (is (= "https://x.test?q=%201" (.. container -result -current))))))
```
