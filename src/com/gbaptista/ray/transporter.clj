(ns com.gbaptista.ray.transporter)

(defn transport!
  "It stores a chunk of data in a specific channel and sends it to matching renderers tied to that transporter."
  [relay transporter-atom chunck {:keys [channel should]}]
  (let [{:keys [id data]} chunck
        identity-filter {:transporter (:id @transporter-atom)}
        now (str (java.time.Instant/now))
        message {:meta (merge {:at now} identity-filter)
                 :data {:method "from-driver/as-transporter/receive-chunk"
                        :params {:channel channel
                                 :should  should
                                 :chunck  id
                                 :data    data}}}]
    (swap! transporter-atom update-in [:channels channel]
           (fnil conj []) (assoc chunck :should should))
    ((:register-transporter! relay) transporter-atom)
    ((:send-to-if-matches! relay) identity-filter message)
    transporter-atom))
