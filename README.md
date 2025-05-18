# ray

A [ray](https://github.com/gbaptista/ray-spec) driver implementation for Clojure.

## TL;DR and Quick Start

```clojure
(require '[com.gbaptista.ray :as ray]
         '[clojure.java.browse :refer [browse-url]])

(def relay (ray/create-relay! 9117))

(def renderer "http://localhost:6077/t/{transporter}/c/{controller}/{expression}?relay={relay}")

(def controller  (atom {:id "0196c01b-d769-7fdc-8c98-910d11b704b8" :state {}}))
(def transporter (atom {:id "0196c01b-d769-7a4d-89a6-9356ea2490af" :channels {}}))

(browse-url (ray/url-for renderer relay @controller @transporter "debug"))

(ray/control! relay controller "theme" "light")

(ray/transport!
 relay transporter
 {:id (ray/uuid)
  :data [{:at "2025-05-18 08:11:39.703", :value 0.05}
         {:at "2025-05-18 08:11:39.712", :value 0.36}]})

(browse-url (ray/url-for renderer relay @controller @transporter "table"))

(ray/transport! relay transporter {:id (ray/uuid) :data (ray/synthesize 2)})

(ray/control! relay controller "theme" "dark")
```

## Development

```clojure
(require 'com.gbaptista.ray :reload-all)
(require 'com.gbaptista.ray.controller :reload-all)
```

```bash
cljfmt fix deps.edn src/com/
clj-kondo --lint deps.edn src/com/
```
