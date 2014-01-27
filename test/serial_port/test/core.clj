(ns serial-port.test.core
  (:require [clojure.test :refer :all]
            [serial-port.core :refer :all]))

(deftest port-ids-test
  (let [ports (port-ids)]
    (is (not (empty? ports)))))

(run-tests)

