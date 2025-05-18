(ns com.gbaptista.ray.handlers
  (:import [java.time Instant]))

(defn relay-register!
  "Registers a remote renderer connection, tying it to a controller and transporter."
  [connections connection {:keys [controller transporter] :as identity} send-to-connection!]
  (swap! connections assoc connection {:wire connection :identity identity})
  (send-to-connection!
   connection
   {:meta {:at          (str (Instant/now))
           :transporter transporter
           :controller  controller}
    :data {:method "from-driver/as-relay/acknowledged"
           :params {:acknowledged true}}}))

(defn transporter-synchronize!
  "Replays all transporter data chunks to remote renderers attached to that transporter."
  [transporters connection {:keys [transporter]} send-to-connection!]
  (when-let [transporter-atom (get @transporters transporter)]
    (doseq [[channel-id chunks] (:channels @transporter-atom)]
      (doseq [{:keys [id data]} chunks]
        (send-to-connection! connection
                             {:meta {:at (str (Instant/now)) :transporter transporter}
                              :data {:method "from-driver/as-transporter/accumulate-chunk"
                                     :params {:channel channel-id
                                              :chunck  id
                                              :data    data}}})))))

(defn controller-synchronize!
  "Send all controller current states to remote renderers attached to that controller."
  [controllers connection {:keys [controller]} send-to-connection!]
  (when-let [controller-atom (get @controllers controller)]
    (let [state (:state @controller-atom)]
      (doseq [[property value] state]
        (send-to-connection! connection
                             {:meta {:at (str (Instant/now))
                                     :controller controller}
                              :data {:method "from-driver/as-controller/set-state"
                                     :params {:property property
                                              :value    value}}})))))
