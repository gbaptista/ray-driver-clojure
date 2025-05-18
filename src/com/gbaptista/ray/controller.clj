(ns com.gbaptista.ray.controller
  (:require [clojure.string]))

(defn control!
  "Sets a property in the controller's state and sends it to matching connections."
  [relay controller-atom property value]
  (let [id (:id @controller-atom)
        identity-filter {:controller id}
        now (str (java.time.Instant/now))
        message {:meta (merge {:at now} identity-filter)
                 :data {:method "from-driver/as-controller/set-state"
                        :params {:property property
                                 :value    value}}}]

    (swap! controller-atom update :state assoc property value)

    ((:register-controller! relay) controller-atom)
    ((:send-to-if-matches! relay) identity-filter message)

    controller-atom))

(defn generate-url
  "Given a renderer URL template, it generates a URL for the desired expression."
  [renderer relay controller transporter expression]
  (let [replacements {"{transporter}" (:id transporter)
                      "{controller}"  (:id controller)
                      "{expression}"  expression
                      "{relay}"       (java.net.URLEncoder/encode (str (:url relay)) "UTF-8")}
        filled-url (reduce (fn [url [k v]]
                             (clojure.string/replace url k v))
                           renderer
                           replacements)]
    (java.net.URL. filled-url)))
