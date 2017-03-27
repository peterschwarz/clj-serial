(ns serial.util
  (:require [serial.core :refer [port-identifiers]])
  (:import [purejavacomm CommPortIdentifier]))

(defn get-port-names
  "Gets a set of the currently avilable port names"
  []
  (set (map #(.getName ^CommPortIdentifier %) (port-identifiers))))

(defn list-ports
  "Print out the available ports.
   The names printed may be passed to `serial.core/open` as printed."
  []
  (doall (map println (get-port-names))))
