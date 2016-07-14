;; Example node.js client from David Martin (https://github.com/DaveWM)

(ns example.node
  (:require
   [taoensso.sente :as sente]
   [taoensso.timbre :as timbre :refer-macros (tracef debugf infof warnf errorf)]
   [cljs.core.async :refer [<! timeout]])
  (:require-macros
   [cljs.core.async.macros :refer [go-loop go]]))

(def sente-timeout (* 60 1000))
(def send-request-time (* 10 1000))

(enable-console-print!)

(defn event-msg-handler [ev-msg]
  (debugf "Message from server: %s" (:event ev-msg)))

(defn example-reply [reply]
  (debugf "Reply from server: %s" reply))

(defn example-request [send!]
  (let [msg [:example/node {:had-a-callback? "yep"}]]
    (debugf "Sending: %s" msg)
    (send! msg sente-timeout example-reply)))

(defn send-requests [send!]
  (go-loop []
    (<! (timeout send-request-time))
    (example-request send!)
    (recur)))

(defn start []
  (println "Hello nodejs!")
  (let [;; Serializtion format, must use same val for client + server:
        packer :edn ; Default packer, a good choice in most cases
        {:keys [chsk ch-recv send-fn state]}
        (sente/make-channel-socket-client!
         "/chsk" ; Must match server Ring routing URL
         {:type   :auto
          :packer packer
          :host   "localhost:4000"})]

    (def chsk       chsk)
    (def ch-chsk    ch-recv) ; ChannelSocket's receive channel
    (def chsk-send! send-fn) ; ChannelSocket's send API fn
    (def chsk-state state)   ; Watchable, read-only atom

    (sente/start-client-chsk-router! ch-chsk event-msg-handler)

    (send-requests chsk-send!)))

(set! *main-cli-fn* start)
