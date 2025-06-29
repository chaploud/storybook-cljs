# Storybook CLJS

Run [storybook.js 9.0.14](https://storybook.js.org/) with ClojureScript!

## Requirements

* [Node.js](https://nodejs.org/en)
* [shadow-cljs](https://github.com/thheller/shadow-cljs)
* A ClojureScript+React project using either [HSX](https://github.com/factorhouse/hsx) or [Uix](https://github.com/pitch-io/uix)

## Getting started

See the [examples](/examples) directory in this Git repo for working examples.

### 1. Initialize project

```bash
npx @factorhouse/storybook-cljs init
```

The `init` command will install the required dependencies to your `package.json` file and initialize the required project structure in the `.storybook/` directory.

### 2. Setup shadow-cljs build profile

You will need to add a `:storybook` shadow-cljs build profile:

```clojure
{:builds {:storybook {:target      :npm-module
                      :entries     [todomvc.stories]
                      :output-dir  ".storybook/cljs-out/"
                      :build-hooks [(io.factorhouse.storybook.compiler/configure {:compiler io.factorhouse.storybook.compiler.hsx})
                                    (io.factorhouse.storybook.compiler/compile)]}}}
```

* Target: `:npm-module`
* Output directory: `.storybook/cljs-out/`
* Storybook build hook: Specify the `:compiler` as either `io.factorhouse.storybook.compiler.hsx` or `io.factorhouse.storybook.compiler.uix`

### 3. Install ClojureScript dependency

```clojure
[io.factorhouse/storybook-cljs "0.1.0"]
```

### 4. Write your first story

```clojure
(ns todomvc.stories
  (:require [io.factorhouse.storybook.core :as storybook]))

(defmethod storybook/story "Core/Components/Button" [_]
  {})
```

### 5. Watch for changes

```bash
npx shadow-cljs watch storybook
npx storybook dev
```

### 6. Compile your Storybook project

```bash
npx shadow-cljs release storybook
npx storybook build 
```

Your Storybook project will get compiled to `./storybook-static/`