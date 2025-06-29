# Storybook CLJS

Run [storybook.js 9.0.14](https://storybook.js.org/) with ClojureScript!

## Requirements

* [Node.js](https://nodejs.org/en)
* [shadow-cljs](https://github.com/thheller/shadow-cljs)
* A ClojureScript+React project using either [HSX](https://github.com/factorhouse/hsx) or [UIx](https://github.com/pitch-io/uix)

## Getting started

See the [examples](/examples) directory in this Git repo for working examples.

### 1. Initialize project

```bash
npx @factorhouse/storybook-cljs init
```

The `init` command will install the required dependencies to your `package.json` file and initialize the required project structure in the `.storybook/` directory.

### 2. Configure shadow-cljs build

You will need to add a `:storybook` shadow-cljs build profile:

```clojure
{:builds {:storybook {:target      :npm-module
                      :entries     [todomvc.stories]
                      :output-dir  ".storybook/cljs-out/"
                      :build-hooks [(io.factorhouse.storybook.compiler/configure 
                                      {:compiler io.factorhouse.storybook.compiler.hsx})
                                    (io.factorhouse.storybook.compiler/compile)]}}}
```

* Target: `:npm-module`
* Output directory: `.storybook/cljs-out/`
* Storybook build hook:
  - Specify the `:compiler` as either `io.factorhouse.storybook.compiler.hsx` or `io.factorhouse.storybook.compiler.uix`

### 3. Add ClojureScript dependency

Add to `project.clj` or `deps.edn`:

```clojure
[io.factorhouse/storybook-cljs "0.1.0"]
```

### 4. Write your first story

```clojure
(ns example.core
  (:require [io.factorhouse.storybook.core :as storybook]
            [uix.core :refer [$ defui]]))

(defui button
  [{:keys [variant size children] :or {variant :primary size :md}}]
    ($ :button {} children))

(defmethod storybook/story "Component/Buttons/Secondary" [_]
  {:component button
   :stories {:Default  {:args {:variant :secondary :children ["Secondary Button"]}}
             :Large    {:args {:variant :secondary :size :lg :children ["Large Secondary"]}}
             :WithIcon {:args {:variant :secondary :children ["Secondary"]}}}))
```

#### Story examples

* **UIx** - see [examples/uix](/examples/uix) for details
* **HSX** - see [examples/hsx](/examples/hsx) for details

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


## Writing Stories

### Overview

Use the `io.factorhouse.storybook.core/story` multi-method to define component stories for your Storybook. This multi-method approach allows you to organize stories hierarchically and generate the appropriate JavaScript files for Storybook consumption.

### Project Structure

You can define multiple story hierarchies in the same CloujreScript namespace:
```clojure
(defmethod storybook/story "Component/Buttons/Primary" [_] ...)
(defmethod storybook/story "Component/Buttons/Secondary" [_] ...)  
(defmethod storybook/story "Component/Buttons/Variants" [_] ...)
```

Stories should be organized in a `dev-src` directory to keep development-only code separate from production source:

```
project/
├── src/                    ; Production source code
│   └── components/
├── dev-src/               ; Development-only stories
│   ├── stories.cljs       ; Central namespace for collecting stories
│   └── stories/
│       ├── buttons.cljs
│       ├── forms/
│       │   └── inputs.cljs
│       └── layout/
│           └── cards.cljs
└── shadow-cljs.edn
```

**Important**: Your `shadow-cljs.edn` `:entries` must include the central namespace (e.g., `dev-src.stories`) that requires all story namespaces to ensure multi-methods are loaded at runtime.

```clojure
;; dev-src/stories.cljs - Central collection namespace
(ns stories
  (:require [dev-src.stories.buttons.primary]
            [dev-src.stories.buttons.secondary]
            [dev-src.stories.forms.inputs]
            [dev-src.stories.layout.cards]))
```

### Basic Usage

```clojure
(ns stories.buttons
  (:require [io.factorhouse.storybook.core :as storybook]
            [your-app.components.button :refer [button]]))

(defmethod storybook/story "Component/Buttons/Primary" [_]
  {:component button
   :stories {:Default {:args {:children ["Click me"]}}
             :Large {:args {:size :lg :children ["Large button"]}}}})
```

### Dispatch Value (Story Hierarchy)

The dispatch value represents the **hierarchical path** of your story and determines the file output location:

Each story multi-method compiles to a CommonJS Story file that Storybook.js can understand. The build hooks defined in your shadow-cljs profile take care of this compilation step.

#### Format
- Use forward slashes (`/`) to separate hierarchy levels
- Follow PascalCase or camelCase conventions for readability

#### Examples
| Dispatch Value | Output File | Purpose |
|----------------|-------------|---------|
| `"Component/Buttons/Primary"` | `js-out/component/buttons/primary_story.js` | Primary button variants |
| `"Component/Forms/Input/Text"` | `js-out/component/forms/input/text_story.js` | Text input stories |
| `"Layout/Card/Basic"` | `js-out/layout/card/basic_story.js` | Basic card layouts |
| `"Feedback/Alert/Types"` | `js-out/feedback/alert/types_story.js` | Different alert types |

**Popular Organizational Approaches:**

1. **[Atomic Design](https://bradfrost.com/blog/post/atomic-design-and-storybook/)** - Groups by complexity level:
   ```
   Atoms/Button/Primary
   Molecules/Forms/SearchBox  
   Organisms/Navigation/Header
   Templates/Layout/Homepage
   ```

2. **Functional Grouping** - Groups by purpose ([used by Spotify, Monday.com](https://storybook.js.org/blog/structuring-your-storybook/)):
   ```
   Component/Forms/Input
   Component/Navigation/Menu
   Component/Feedback/Alert
   ```

3. **Feature-based** - Groups by application area:
   ```
   Dashboard/Charts/LineChart
   Profile/Settings/AccountForm
   Commerce/Product/Card
   ```

### Story definition

`storybook/story` must return a map with the following structure:

```clojure
{:component component-function   ; Required: The component function to render (either a UIx or HSX component)
 :stories   story-map}           ; Required: Map of story definitions
```

#### Component Key
- **Required**: The actual component function reference
- **Type**: Function that accepts props and returns UIx/HSX component

#### Stories Key
- **Required**: A map where keys are story names and values are story configurations
- **Type**: `{StoryName StoryConfig}`

### Story Configuration

#### Core Properties
```clojure
{:args        {}           ; Required: All component props go here
 :name        "Story Name" ; Optional: Display name (defaults to map key)
 :play        play-fn      ; Optional: Play function for interactions
 :decorators  [decorator]  ; Optional: Story decorators
 :parameters  {}           ; Optional: Story parameters}
```

#### Component Args (props)

```clojure
{:args {:variant  :primary          ; Component prop
        :size     :lg               ; Component prop  
        :disabled true              ; Component prop
        :children ["Button Text"]}} ; Component children
```

### Complete Examples

#### Simple Button Stories
```clojure
(defmethod storybook/story "Component/Buttons/Primary" [_]
  {:component button
   :stories {:Default   {:args {:children ["Primary Button"]}}
             :Large     {:args {:size :lg :children ["Large Primary"]}}
             :Disabled  {:args {:disabled true :children ["Disabled"]}}
             :WithClick {:args {:on-click #(js/alert "Clicked!") 
                               :children ["Interactive"]}}}})
```

#### Complex Modal Stories with Decorators
```clojure
(defmethod storybook/story "Component/Overlay/Modal" [_]
  {:component modal
   :stories {:Confirmation {:args {:open true
                                  :title "Confirm Action"
                                  :children ["Are you sure?"]}}
                           :decorators [(fn [story]
                                         ; Wrap story with additional context
                                         story)]
                           :parameters {:layout "fullscreen"}}
             :Warning     {:args {:open true
                                 :title "Delete Item"
                                 :children ["This cannot be undone"]
                                 :variant :danger}}}})
```

## Tagged JSON / SerDes

StorybookJS expects story `:args` as a plain JS object. In order to support rich Clojure types (keywords, sets, etc) storybook-cljs uses 'tagged JSON': 

```
:foo ;; serializes to ["kw!", "foo"]
#{1 2 3} ;; serializes to ["set!", 1, 2, 3] 
```

This format is chosen over more complex formats (such as Transit+JSON) as it allows for easier viewing/editing of Clojure types within the Storybook control UI:

![Tagged JSON](/resources/tagged_json.png)

See [tagged_json.cljc](https://github.com/factorhouse/storybook-cljs/blob/main/modules/storybook-cljs/src/io/factorhouse/storybook/tagged_json.cljc) to extend for custom types.

In the cases where Tagged JSON is too limiting for your components, simply wrap your story `:component` with another function that handles your serdes logic.

## Copyright and License

Copyright © 2025 Factor House Pty Ltd.

Distributed under the Apache-2.0 License, the same as Apache Kafka.