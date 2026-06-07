# UIx review and debugging checklist

## Compilation and setup

- Does the namespace require `uix.core` macros correctly?
- Is `uix.dom` used only on the browser side?
- Is `uix.dom.server` used only on JVM SSR namespaces?
- Are React and React DOM installed through npm?
- Does `shadow-cljs.edn` include `:deps true` when using deps.edn?
- Is `uix.preload` dev-only, not bundled as an intentional production behavior?

## Component shape

- `defui` components have zero or one argument only.
- Props are destructured from a single map.
- Children are read from `:children`, not positional args.
- UIx components are rendered with `$`, not called as functions.
- DOM/UI tree is static enough for UIx's macros and linters.

## Hooks

- Hooks are only top-level in `defui` or `defhook`.
- Hooks are not inside `if`, `when`, `for`, `map`, callbacks, helper functions, or conditionally returned render branches.
- `use-effect`, `use-layout-effect`, `use-memo`, `use-callback`, and `use-imperative-handle` dependencies are present and correct.
- Dependencies are vector literals, not JS arrays or dynamic lists.
- `use-memo` and `use-callback` always include deps.
- Effects do not set state every render without deps and create an infinite loop.
- Effect setup does not accidentally return a function unless it is intended cleanup.

## Rendering collections

- Every element or component returned by `for`, `map`, `keep`, etc. has a stable `:key`.
- Keys are not random values or array indexes unless the collection is truly static.
- Preact targets convert lazy sequences to arrays when required.

## Props and attributes

- DOM attrs use kebab-case: `:on-click`, `:auto-play`, `:data-test-id`.
- DOM classes use selector shorthand, `:class`, or merged class strings intentionally.
- JS React component props rely on shallow conversion only.
- Nested JS component options use `#js` or explicit conversion.
- `:&` spread is only top-level.
- Multiple spreads are ordered intentionally.

## Reagent and re-frame

- Reagent components inside UIx are wrapped with `r/as-element`.
- UIx components inside Reagent are rendered with `$`.
- Reagent ratoms/reactions are consumed with `uix.reagent/use-reaction`.
- re-frame subscriptions are consumed with `uix.re-frame/use-subscribe`.
- Direct `rf/subscribe` in `defui` is treated as a bug.

## Interop

- Use `cljs-bean/bean` or `cljs-bean/->clj` at JS callback boundaries.
- Use `uix/as-react` to expose UIx components to JS React.
- Use `uix/forward-ref` only for third-party React ref-injection cases.
- Do not over-convert props. Convert at boundaries, not throughout pure UI code.

## SSR

- Shared UI is `.cljc` when rendered on JVM and browser.
- Browser APIs are hidden behind reader conditionals.
- Server and client render deterministic markup.
- Effects are not relied on for server output.
- Hydration uses the same component tree and props as SSR.

## Performance

- Memoization is justified by stable props and render cost.
- Expensive derived data uses `use-memo` with correct deps.
- Event callbacks passed deep into memoized children use `use-callback` when needed.
- Large immutable maps are not repeatedly deep-compared without need.

## Testing

- Component behavior is tested through DOM and user interactions, not implementation details.
- Async UI uses `async` and `js-await` or promise-aware testing helpers.
- Network calls are mocked with MSW or equivalent.
- Hooks are tested with `renderHook` when a component shell would add noise.

## Useful commands

```bash
npm i
clojure -M -m shadow.cljs.devtools.cli watch app
clojure -M -m shadow.cljs.devtools.cli release app
scripts/test
clj-kondo --lint "$(clojure -Spath)" --copy-configs --skip-lint
```
