(ns com.gbaptista.ray.transporter)

(defn transport!
  "It stores a chunk of data into a specific channel and sends it to matching renderers tied to that transporter."
  ([relay transporter-atom chunck]
   (transport! relay transporter-atom chunck {:channel "A"}))

  ([relay transporter-atom chunck {:keys [channel]}]
   (let [{:keys [id data]} chunck
         identity-filter {:transporter (:id @transporter-atom)}
         now (str (java.time.Instant/now))
         message {:meta (merge {:at now} identity-filter)
                  :data {:method "from-driver/as-transporter/accumulate-chunk"
                         :params {:channel channel
                                  :chunck  id
                                  :data    data}}}]
     (swap! transporter-atom update-in [:channels channel] (fnil conj []) chunck)
     ((:register-transporter! relay) transporter-atom)
     ((:send-to-if-matches! relay) identity-filter message)
     transporter-atom)))
