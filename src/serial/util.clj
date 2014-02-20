(ns serial.util
  (:require [serial.core :refer :all]))

(defn list-ports
  "Print out the available ports. The names are printed exactly as they should be passed to open."
  []
  (loop [ports (port-ids)]
    (when ports
      (println (.getName (first ports)))
      (recur (next ports)))))
