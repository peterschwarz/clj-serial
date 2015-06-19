(ns serial.util
  (:require [serial.core :refer [port-identifiers]]))

(defn list-ports
  "Print out the available ports.
   The names printed may be passed to `serial.core/open` as printed."
  []
  (doall (map #(println (.getName %)) (port-identifiers))))
