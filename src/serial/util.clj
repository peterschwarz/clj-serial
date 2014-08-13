(ns serial.util
  (:require [serial.core :refer :all]))

(defn list-ports
  "Print out the available ports. The names are printed exactly as they should be passed to open."
  []
  (doall (map #(println (.getName %)) (port-ids))))
