(ns serial.test.core
  (:require [clojure.test :refer :all]
            [serial.core :refer :all])
  (:import [purejavacomm CommPortIdentifier]
           [java.util Enumeration]))

(deftest port-ids-test
  (testing "Empty port identifiers"
    (with-redefs [raw-port-ids (fn [] (proxy [Enumeration] []
                                        (hasMoreElements [] false)
                                        (nextElement [] nil)))]
      (let [ports (port-ids)]
        (is (nil? ports ))))
    )
  )

    

