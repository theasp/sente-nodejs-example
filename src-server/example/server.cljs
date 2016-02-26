(ns example.server
  "Official Sente reference example: server"
  {:author "Peter Taoussanis (@ptaoussanis)"}

  (:require
   [cljs.nodejs        :as nodejs]
   [clojure.string     :as str]
   [hiccups.runtime    :as hiccupsrt]
   [cljs.core.async    :as async  :refer (<! >! put! chan)]
   [taoensso.encore    :as encore :refer ()]
   [taoensso.timbre    :as timbre :refer-macros (tracef debugf infof warnf errorf)]
   [taoensso.sente     :as sente]

   ;;; TODO: choose (uncomment) a supported web server and adapter
   ;;; You will also have to comment/uncomment the appropriate section below.
   ;; Dogfort
   ;; [dogfort.middleware.defaults :as defaults]
   ;; [dogfort.middleware.routes]
   ;; [taoensso.sente.server-adapters.dogfort :refer (dogfort-adapter)]
   ;; [dogfort.http :refer (run-http)]

   ;; Express
   [taoensso.sente.server-adapters.express :as sente-express]

   ;; Optional, for Transit encoding:
   [taoensso.sente.packers.transit :as sente-transit])
  (:require-macros
   [dogfort.middleware.routes-macros :refer (defroutes GET POST)]
   [hiccups.core :as hiccups :refer [html]]
   [cljs.core.async.macros :as asyncm :refer (go go-loop)]))

(enable-console-print!)
;;(timbre/set-level! :trace) ; Uncomment for more logging

;;;; Ring handlers

(defn landing-pg-handler [ring-req]
  (hiccups/html
   [:h1 "Sente reference example"]
   [:p "An Ajax/WebSocket" [:strong " (random choice!)"] " has been configured for this example"]
   [:hr]
   [:p [:strong "Step 1: "] " try hitting the buttons:"]
   [:button#btn1 {:type "button"} "chsk-send! (w/o reply)"]
   [:button#btn2 {:type "button"} "chsk-send! (with reply)"]
   ;;
   [:p [:strong "Step 2: "] " observe std-out (for server output) and below (for client output):"]
   [:textarea#output {:style "width: 100%; height: 200px;"}]
   ;;
   [:hr]
   [:h2 "Step 3: try login with a user-id"]
   [:p  "The server can use this id to send events to *you* specifically."]
   [:p
    [:input#input-login {:type :text :placeholder "User-id"}]
    [:button#btn-login {:type "button"} "Secure login!"]]
   ;;
   [:hr]
   [:h2 "Step 4: want to re-randomize Ajax/WebSocket connection type?"]
   [:p "Hit your browser's reload/refresh button"]
   [:script {:src "main.js"}] ; Include our cljs target
   ))

(defn login-handler
  "Here's where you'll add your server-side login/auth procedure (Friend, etc.).
  In our simplified example we'll just always successfully authenticate the user
  with whatever user-id they provided in the auth request."
  [ring-req]
  (let [{:keys [session params]} ring-req
        {:keys [user-id]}        params]
    (debugf "Login request: %s" params)
    {:status 200 :session (assoc session :uid user-id)}))


;; *************************************************************************
;; vvvv  UNCOMMENT FROM HERE FOR DOGFORT                                vvvv

;; (let [;; Serializtion format, must use same val for client + server:
;;       packer :edn ; Default packer, a good choice in most cases
;;       ;; (sente-transit/get-flexi-packer :edn) ; Experimental, needs Transit dep

;;       {:keys [ch-recv send-fn ajax-post-fn ajax-get-or-ws-handshake-fn
;;               connected-uids]}
;;       (sente/make-channel-socket-server! dogfort-adapter
;;                                          {:packer packer})]

;;   (def ring-ajax-post                ajax-post-fn)
;;   (def ring-ajax-get-or-ws-handshake ajax-get-or-ws-handshake-fn)
;;   (def ch-chsk                       ch-recv) ; ChannelSocket's receive channel
;;   (def chsk-send!                    send-fn) ; ChannelSocket's send API fn
;;   (def connected-uids                connected-uids) ; Watchable, read-only atom
;;   )

;; (defn login!
;;   "Here's where you'll add your server-side login/auth procedure (Friend, etc.).
;;   In our simplified example we'll just always successfully authenticate the user
;;   with whatever user-id they provided in the auth request."
;;   [req res]
;;   (let [req-session (aget req "session")
;;         params (aget req "params")
;;         user-id (aget params "user-id")]
;;     (debugf "Login request: %s" params)
;;     (aset req-session "uid" user-id)
;;     (.send res "Success")))


;; (defroutes ring-routes
;;   (GET  "/"      ring-req (landing-pg-handler            ring-req))
;;   (GET  "/chsk"  ring-req (ring-ajax-get-or-ws-handshake ring-req))
;;   (POST "/chsk"  ring-req (ring-ajax-post                ring-req))
;;   (POST "/login" ring-req (login-handler                 ring-req)))

;; (def main-ring-handler
;;   (defaults/wrap-defaults ring-routes {:wrap-file "resources/public"}))

;; (defn start-selected-web-server! [ring-handler port]
;;   (println "Starting dogfort...")
;;   (run-http ring-handler {:port port})
;;   {:stop-fn #(errorf "One does not simply stop dogfort...")
;;    :port port})

;; ^^^^  UNCOMMENT TO HERE FOR DOGFORT                                  ^^^^
;; *************************************************************************


;; *************************************************************************
;; vvvv  UNCOMMENT FROM HERE FOR EXPRESS                                vvvv

(def http (nodejs/require "http"))
(def express (nodejs/require "express"))
(def express-ws (nodejs/require "express-ws"))
(def ws (nodejs/require "ws"))
(def cookie-parser (nodejs/require "cookie-parser"))
(def body-parser (nodejs/require "body-parser"))
(def csurf (nodejs/require "csurf"))
(def session (nodejs/require "express-session"))

(let [;; Serializtion format, must use same val for client + server:
      packer :edn ; Default packer, a good choice in most cases
      ;; (sente-transit/get-flexi-packer :edn) ; Experimental, needs Transit dep
      {:keys [ch-recv send-fn ajax-post-fn ajax-get-or-ws-handshake-fn
              connected-uids]}
      (sente-express/make-express-channel-socket-server! {:packer packer})]
  (def ajax-post                ajax-post-fn)
  (def ajax-get-or-ws-handshake ajax-get-or-ws-handshake-fn)
  (def ch-chsk                  ch-recv) ; ChannelSocket's receive channel
  (def chsk-send!               send-fn) ; ChannelSocket's send API fn
  (def connected-uids           connected-uids) ; Watchable, read-only atom
  )

(defn express-login-handler
  "Here's where you'll add your server-side login/auth procedure (Friend, etc.).
  In our simplified example we'll just always successfully authenticate the user
  with whatever user-id they provided in the auth request."
  [req res]
  (let [req-session (aget req "session")
        body        (aget req "body")
        user-id     (aget body "user-id")]
    (debugf "Login request: %s" user-id)
    (aset req-session "uid" user-id)
    (.send res "Success")))

(defn routes [express-app]
  (doto express-app
    (.get "/" (fn [req res] (.send res (landing-pg-handler req))))

    (.ws "/chsk"
         (fn [ws req next]
           (ajax-get-or-ws-handshake req nil nil
                                     {:websocket? true
                                      :websocket  ws})))

    (.get "/chsk" ajax-get-or-ws-handshake)
    (.post "/chsk" ajax-post)
    (.post "/login" express-login-handler)
    (.use (.static express "resources/public"))
    (.use (fn [req res next]
            (warnf "Unhandled request: %s" (.-originalUrl req))
            (next)))))

(defn wrap-defaults [express-app routes]
  (let [cookie-secret "the shiz"]
    (doto express-app
      (.use (fn [req res next]
              (tracef "Request: %s" (.-originalUrl req))
              (next)))
      (.use (session
             #js {:secret            cookie-secret
                  :resave            true
                  :cookie            {}
                  :store             (.MemoryStore session)
                  :saveUninitialized true}))
      (.use (.urlencoded body-parser
                         #js {:extended false}))
      (.use (cookie-parser cookie-secret))
      (.use (csurf
             #js {:cookie false}))
      (routes))))

(defn main-ring-handler [express-app]
  ;; Can we even call this a ring handler?
  (wrap-defaults express-app routes))

(defn start-selected-web-server! [ring-handler port]
  (println "Starting express...")
  (let [express-app       (express)
        express-ws-server (express-ws express-app)]

    (ring-handler express-app)

    (let [http-server (.listen express-app port)]
      {:express-app express-app
       :ws-server   express-ws-server
       :http-server http-server
       :stop-fn     #(.close http-server)
       :port        port})))

;; ^^^^  UNCOMMENT TO HERE FOR EXPRESS                                  ^^^^
;; *************************************************************************


;;;; Sente event handlers

(defmulti -event-msg-handler
  "Multimethod to handle Sente `event-msg`s"
  :id ; Dispatch on event-id
  )

(defn event-msg-handler
  "Wraps `-event-msg-handler` with logging, error catching, etc."
  [{:as ev-msg :keys [id ?data event]}]
  (-event-msg-handler ev-msg))

(defmethod -event-msg-handler
  :default ; Default/fallback case (no other matching handler)
  [{:as ev-msg :keys [event id ?data ring-req ?reply-fn send-fn]}]
  (let [session (:session ring-req)
        uid     (:uid     session)]
    (debugf "Unhandled event: %s" event)
    (when ?reply-fn
      (?reply-fn {:umatched-event-as-echoed-from-from-server event}))))

;; TODO Add your (defmethod -event-msg-handler <event-id> [ev-msg] <body>)s here...

;;;; Sente event router (our `event-msg-handler` loop)

(defonce router_ (atom nil))
(defn  stop-router! [] (when-let [stop-f @router_] (stop-f)))
(defn start-router! []
  (stop-router!)
  (reset! router_
          (sente/start-server-chsk-router!
           ch-chsk event-msg-handler)))

;;;; Some server>user async push examples

(defn start-example-broadcaster!
  "As an example of server>user async pushes, setup a loop to broadcast an
  event to all connected users every 10 seconds"
  []
  (let [broadcast!
        (fn [i]
          (debugf "Broadcasting server>user: %s" @connected-uids)
          (doseq [uid (:any @connected-uids)]
            (chsk-send! uid
                        [:some/broadcast
                         {:what-is-this "An async broadcast pushed from server"
                          :how-often    "Every 10 seconds"
                          :to-whom      uid
                          :i            i}])))]

    (go-loop [i 0]
      (<! (async/timeout 10000))
      (broadcast! i)
      (recur (inc i)))))

(defn test-fast-server>user-pushes
  "Quickly pushes 100 events to all connected users. Note that this'll be
  fast+reliable even over Ajax!"
  []
  (doseq [uid (:any @connected-uids)]
    (doseq [i (range 100)]
      (chsk-send! uid [:fast-push/is-fast (str "hello " i "!!")]))))

(comment (test-fast-server>user-pushes))

;;;; Init stuff

(defonce    web-server_ (atom nil)) ; {:server _ :port _ :stop-fn (fn [])}
(defn  stop-web-server! [] (when-let [m @web-server_] ((:stop-fn m))))
(defn start-web-server! [& [port]]
  (stop-web-server!)
  (let [{:keys [stop-fn port] :as server-map}
        (start-selected-web-server! (var main-ring-handler) (or port 4000))
        uri (str "http://localhost:" port "/")]
    (infof "Web server is running at `%s`" uri)
    (reset! web-server_ server-map)))

(defn stop!  []  (stop-router!)  (stop-web-server!))
(defn start! [] (start-router!) (start-web-server!) (start-example-broadcaster!))
;; (defonce _start-once (start!))

(defn -main [& _]
  (start!))

(set! *main-cli-fn* -main) ;; this is required

(comment
  (start!)
  (test-fast-server>user-pushes))
