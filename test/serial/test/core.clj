(ns serial.test.core
  (:require [clojure.test :refer :all]
            [serial.core :refer :all]))

(deftest port-ids-test
  (let [ports (port-ids)]
    (is (not (nil? ports)))))

