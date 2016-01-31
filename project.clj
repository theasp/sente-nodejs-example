(defproject com.taoensso.examples/sente "1.8.0-alpha1"
  :description "Sente, reference web-app example project"
  :url "https://github.com/ptaoussanis/sente"
  :license {:name "Eclipse Public License"
            :url  "http://www.eclipse.org/legal/epl-v10.html"
            :distribution :repo
            :comments "Same as Clojure"}
  :min-lein-version "2.3.3"
  :global-vars {*warn-on-reflection* true
                *assert* true}

  :dependencies
  [;; [org.clojure/clojure    "1.7.0"]
   [org.clojure/clojure       "1.8.0"]

   [org.clojure/clojurescript "1.7.228"]
   [org.clojure/core.async    "0.2.374"]
   [org.clojure/tools.nrepl   "0.2.12"] ; Optional, for Cider

   [com.taoensso/sente        "1.8.0-alpha1"] ; <--- Sente
   [com.taoensso/timbre       "4.2.1"]

   ;;; ---> Choose (uncomment) a supported web server <---
   [org.clojars.whamtet/dogfort "0.2.0-SNAPSHOT"]

   [hiccups                   "0.3.0"] ; Optional, just for HTML

   ;;; Transit deps optional; may be used to aid perf. of larger data payloads
   ;;; (see reference example for details):
   [com.cognitect/transit-cljs "0.8.237"]]

  :npm
  {:dependencies
   [[source-map-support       "*"]

    ;; Express
    [express                  "4.13.3"]
    [express-ws               "1.0.0-rc.2"]
    [body-parser              "1.14.1"]
    [cookie-parser            "1.4.0"]
    [express-session          "1.11.3"]
    [csurf                    "1.8.3"]

    ;; ws is needed for dogfort and express
    [ws                       "0.8.0"]]}

  :plugins
  [[lein-pprint         "1.1.2"]
   [lein-ancient        "0.6.8"]
   [com.cemerick/austin "0.1.6"]
   [lein-cljsbuild      "1.1.2"]
   [lein-shell          "0.5.0"]
   [lein-npm            "0.6.1"]
   [cider/cider-nrepl   "0.10.1"] ; Optional, for use with Emacs
   ]

  :cljsbuild
  {:builds
   [{:id :cljs-server
     :source-paths ["src-server"]
     :compiler {:main "example.server"
                :output-to "target/main.js"
                :output-dir "target/out"
                :target :nodejs
                ;;:optimizations :simple
                ;;:source-map "target/main.js.map"
                :optimizations :none
                :source-map true
                :pretty-print true}}
    {:id :cljs-client
     :source-paths ["src-client"]
     :compiler {:output-to "resources/public/main.js"
                :optimizations :whitespace #_:advanced
                :pretty-print true}}]}

  ;; Call `lein start-repl` to get a (headless) development repl that you can
  ;; connect to with Cider+emacs or your IDE of choice:
  :aliases
  {"start"      ["do" "npm" "install," "cljsbuild" "once," "shell" "node" "target/main.js"]}

  :repositories
  {"sonatype-oss-public" "https://oss.sonatype.org/content/groups/public/"})
