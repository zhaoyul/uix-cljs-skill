# uix-clojurescript skill

A skill for writing, reviewing, migrating, and testing [UIx](https://github.com/pitch-io/uix) ClojureScript code. Compatible with Pi agent, Claude Code, Codex, Cursor, Gemini CLI, and other [Agent Skills](https://agentskills.io)-compliant coding assistants.

## What is included

- `SKILL.md` — primary skill instructions (loaded on-demand).
- `reference/` — deeper notes for syntax, hooks, interop, migration, testing, SSR, and review.
- `examples/` — copyable UIx examples.
- `templates/` — minimal starter files for a shadow-cljs browser app.

## Install

### Via npx skills (recommended)

```bash
npx skills@latest add zhaoyul/uix-clojurescript
```

This installs the skill automatically for Pi, Claude Code, Codex, Cursor, Gemini CLI, and other supported agents.

### Via git clone

```bash
git clone https://github.com/zhaoyul/uix-clojurescript.git ~/.pi/agent/skills/uix-clojurescript
```

Or into a project directory:

```bash
git clone https://github.com/zhaoyul/uix-clojurescript.git .pi/skills/uix-clojurescript
```

### Manual copy

```bash
cp -R uix-clojurescript ~/.pi/agent/skills/
```

## Usage

The skill loads automatically when the agent detects tasks involving UIx ClojureScript. You can also force-load it:

```
/skill:uix-clojurescript
```

## Freshness note

The dependency versions in `templates/` mirror the public repository README when this skill was generated. Check upstream before creating a production project or upgrading an existing one.
