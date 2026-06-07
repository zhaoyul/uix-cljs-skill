# uix-clojurescript skill

A [pi agent](https://github.com/badlogic/pi) skill for writing, reviewing, migrating, and testing UIx ClojureScript code.

## What is included

- `SKILL.md` — primary skill instructions (loaded on-demand).
- `reference/` — deeper notes for syntax, hooks, interop, migration, testing, SSR, and review.
- `examples/` — copyable UIx examples.
- `templates/` — minimal starter files for a shadow-cljs browser app.

## Install

### Via git clone (pi agent)

```bash
# Clone into pi's global skills directory
git clone https://github.com/zhaoyul/uix-clojurescript.git ~/.pi/agent/skills/uix-clojurescript
```

Or into your project:

```bash
git clone https://github.com/zhaoyul/uix-clojurescript.git .pi/skills/uix-clojurescript
```

### Manual copy

```bash
cp -R uix-clojurescript ~/.pi/agent/skills/
```

## Usage

The skill loads automatically when pi detects tasks involving UIx ClojureScript. You can also force-load it:

```
/skill:uix-clojurescript
```

## Freshness note

The dependency versions in `templates/` mirror the public repository README when this skill was generated. Check upstream before creating a production project or upgrading an existing one.
