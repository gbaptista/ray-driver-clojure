(ns com.gbaptista.ray.relay
  (:require [org.httpkit.server :as http]
            [cheshire.core :as json]
            [com.gbaptista.ray.handlers :as ray-handlers]))

(defn send-to-connection!
  "Sends a message to a specific connection as JSON."
  [connection message]
  (http/send! connection (json/generate-string message)))

(defn send-to-if-matches!
  "Sends a message to all connections whose identity (:controller and/or :transporter) match the given filter."
  [connections filter message]
  (doseq [[_ connection] @connections]
    (when (every? (fn [[k v]] (= (get (:identity connection) k) v)) filter)
      (send-to-connection! (:wire connection) message))))

(defn handle-message!
  "Handles and routes all messages received from a remote renderer."
  [connections transporters controllers connection message user-handler]

  (case (:method message)
    "from-renderer/to-relay/register"
    (ray-handlers/relay-register! connections connection (:params message) send-to-connection!)

    "from-renderer/to-transporter/synchronize"
    (ray-handlers/transporter-synchronize! transporters connection (:params message) send-to-connection!)

    "from-renderer/to-controller/synchronize"
    (ray-handlers/controller-synchronize! controllers connection (:params message) send-to-connection!)

    nil)

  (user-handler connection message))

(defn relay-handler
  "Create a WebSocket handler that tracks connection identity and forwards all messages."
  [connections transporters controllers user-handler]
  (fn [request]
    (if (:websocket? request)
      (http/as-channel
       request
       {:on-open    (fn [channel]
                      (swap! connections assoc channel {:wire channel :identity nil}))
        :on-receive (fn [channel raw]
                      (when-let [message (try (json/parse-string raw true)
                                              (catch Exception _ nil))]
                        (handle-message! connections transporters controllers channel message user-handler)))})
      {:status 400 :body "WebSocket requests only."})))

(defn register-atom!
  "Ensure the item is registered in the atom, assuming it has an :id key."
  [atoms-map atom]
  (swap! atoms-map assoc (:id @atom) atom))

(defn start!
  "Starts a WebSocket server on the given port."
  [port user-handler]
  (let [connections  (atom {})
        controllers  (atom {})
        transporters (atom {})]
    {:url                   (str "ws://localhost:" port)
     :connections           connections
     :transporters          transporters
     :controllers           controllers
     :stop!                 (http/run-server (relay-handler connections transporters controllers user-handler) {:port port})
     :send-to-if-matches!   (fn [filter message] (send-to-if-matches! connections filter message))
     :register-transporter! (fn [transporter]    (register-atom! transporters transporter))
     :register-controller!  (fn [controller]     (register-atom! controllers controller))}))

(defn create!
  "Starts the WebSocket server with a default handler that prints incoming messages."
  [port]
  (start! port (fn [_connection _message] nil)))
