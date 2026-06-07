# React to UIx translation example

Input JSX:

```jsx
function Item({ name, isPacked }) {
  return <li className="item">{name}</li>;
}

export default function PackingList() {
  return (
    <section>
      <h1>Sally Ride's Packing List</h1>
      <ul>
        <Item isPacked={true} name="Space suit" />
        <Item isPacked={true} name="Helmet with a golden leaf" />
        <Item isPacked={false} name="Photo of Tam" />
      </ul>
    </section>
  );
}
```

UIx:

```clojure
(ns packing-list.core
  (:require [uix.core :refer [$ defui]]))

(defui item [{:keys [name packed?]}]
  ($ :li.item name))

(defui packing-list []
  ($ :section
    ($ :h1 "Sally Ride's Packing List")
    ($ :ul
      ($ item {:packed? true :name "Space suit"})
      ($ item {:packed? true :name "Helmet with a golden leaf"})
      ($ item {:packed? false :name "Photo of Tam"}))))
```

Notes:

- `isPacked` became `packed?`.
- `className="item"` became `:li.item`.
- JSX tags became `$` forms.
- Props are a Clojure map.
