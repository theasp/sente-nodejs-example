(defproject com.taoensso.examples/sente "1.10.0-SNAPSHOT"
  :description "Sente, node.js web-app example project"
  :url "https://github.com/ptaoussanis/sente"
  :license {:name         "Eclipse Public License"
            :url          "http://www.eclipse.org/legal/epl-v10.html"
            :distribution :repo
            :comments     "Same as Clojure"}
  :min-lein-version "2.3.3"
  :global-vars {*warn-on-reflection* true
                *assert*             true}

  :dependencies
  [[org.clojure/clojure       "1.8.0"]

   [org.clojure/clojurescript "1.9.671"]
   [org.clojure/core.async    "0.3.443"]
   [org.clojure/tools.nrepl   "0.2.13"] ; Optional, for Cider

   [com.taoensso/timbre       "4.10.0"]
   [com.taoensso/sente        "1.11.0"] ; <--- Sente

   [hiccups                   "0.3.0"] ; Optional, just for HTML

   ;;; ---> Choose (uncomment) a supported web server <---
   [org.clojars.whamtet/dogfort "0.2.0-SNAPSHOT"]

   ;; Macchiato
   [bidi               "2.1.1"]
   [macchiato/core     "0.2.1"]
   [macchiato/env      "0.0.6"]

   ;;; Transit deps optional; may be used to aid perf. of larger data payloads
   ;;; (see reference example for details):
   [com.cognitect/transit-cljs "0.8.239"]]

  :npm
  {:dependencies
   [[source-map-support "*"]

    ;; Express
    [express            "*"]
    [express-ws         "*"]
    [body-parser        "*"]
    [cookie-parser      "*"]
    [express-session    "*"]
    [csurf              "*"]

    ;; ws is needed for dogfort and express
    [ws                 "*"]

    ;; websocket is needed for the node.js client
    [websocket          "*"]]}

  :plugins
  [[lein-pprint         "1.1.2"]
   [lein-ancient        "0.6.10"]
   [com.cemerick/austin "0.1.6"]
   [lein-cljsbuild      "1.1.3"]
   [lein-shell          "0.5.0"]
   [lein-npm            "0.6.2"]
   [cider/cider-nrepl   "0.12.0"] ; Optional, for use with Emacs
   ]

  :cljsbuild
  {:builds
   [{:id           :cljs-server
     :source-paths ["src-server"]
     :compiler     {:main          example.server
                    :output-to     "target/main.js"
                    :output-dir    "target/out"
                    :target        :nodejs
                    ;;:optimizations :simple
                    ;;:source-map "target/main.js.map"
                    :optimizations :none
                    :source-map    true
                    :pretty-print  true}}
    {:id           :cljs-client
     :source-paths ["src-client"]
     :compiler     {:main          example.client
                    :output-to     "resources/public/main.js"
                    :optimizations :whitespace #_:advanced
                    :pretty-print  true}}
    {:id           :node-client
     :source-paths ["src-client"]
     :compiler     {:main          example.node
                    :output-to     "target/node-client.js"
                    :output-dir    "target/node-out"
                    :target        :nodejs
                    ;;:optimizations simple
                    ;;:source-map    "target/node.map.js"
                    :optimizations :none
                    :source-map    true
                    :pretty-print  true}}]}

  ;; Call `lein start-repl` to get a (headless) development repl that you can
  ;; connect to with Cider+emacs or your IDE of choice:
  :aliases
  {"start"        ["do" "clean," "npm" "install," "cljsbuild" "once," "shell" "node" "target/main.js"]
   "start-client" ["do" "clean," "npm" "install," "cljsbuild" "once," "shell" "node" "target/node-client.js"]}

  :repositories
  {"sonatype-oss-public" "https://oss.sonatype.org/content/groups/public/"})
