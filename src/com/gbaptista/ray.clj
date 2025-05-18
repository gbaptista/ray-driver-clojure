(ns com.gbaptista.ray
  (:require [com.gbaptista.ray.relay :as ray-relay]
            [com.gbaptista.ray.transporter :as ray-transporter]
            [com.gbaptista.ray.controller :as ray-controller]
            [com.gbaptista.ray.helpers :as ray-helpers]))

; Relay
(defn create-relay! [port] (ray-relay/create! port))

; Transporter
(defn transport!
  [relay transporter-atom chunck params]
  (ray-transporter/transport! relay transporter-atom chunck params))

; Controller
(defn control! [relay controller-atom property value]
  (ray-controller/control! relay controller-atom property value))

(defn url-for [renderer relay controller transporter expression]
  (ray-controller/generate-url renderer relay controller transporter expression))

; Helpers
(defn uuid [] (ray-helpers/uuid))
(defn synthesize [n] (ray-helpers/synthesize n))
