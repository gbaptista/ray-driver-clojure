(ns com.gbaptista.ray.helpers)

(defn uuid
  "Generates and returns a new random UUID."
  []
  (java.util.UUID/randomUUID))

(defn synthesize
  "Generates a vector of `n` synthetic data points.

  Each data point is a map containing:
  - `:at`: a timestamp string in 'yyyy-MM-dd HH:mm:ss.SSS' format (local time zone).
  - `:value`: a random float between 0.0 and 1.0.

  Useful for simulating time-series or streaming data."
  [n]
  (let [formatter (-> (java.time.format.DateTimeFormatter/ofPattern "yyyy-MM-dd HH:mm:ss.SSS")
                      (.withZone (java.time.ZoneId/systemDefault)))]
    (mapv (fn [_]
            {:at (.format formatter (java.time.Instant/now))
             :value (rand)})
          (range n))))
