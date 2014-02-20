(ns serial.test.core
  (:require [clojure.test :refer :all]
            [serial.core :refer :all])
  (:import [purejavacomm CommPortIdentifier]
           [java.util Enumeration]
           [java.io ByteArrayOutputStream]
           [java.nio ByteBuffer]))


(defn- mock-port
  []
  {:out-stream (ByteArrayOutputStream.)})

(defn- byte-at
  [port index]
  (let [bees (-> port
                 :out-stream
                 .toByteArray
                 )]
    (if (< index (alength bees))
      (aget bees index)
      nil)))

(deftest write-test
  (testing "Bytes should be written to the Port's output stream"
    (let [port (mock-port)]
      (write port (byte-array (.getBytes "Hello" "UTF-8")))
      (is (= "Hello"
             (.toString (:out-stream port) "UTF-8")))))

  (testing "An int should be written to the Port's output stream"
    (let [port (mock-port)]
      (write port 12)
      (is (= (byte 12) (byte-at port 0)))))

  (testing "A list should be written to the port's output stream"
    (let [port (mock-port)]
      (write port '(12 13 14))
      (is (= (byte 12) (byte-at port 0)))
      (is (= (byte 13) (byte-at port 1)))
      (is (= (byte 14) (byte-at port 2)))))

  (testing "A vector should be written to the port's output stream"
    (let [port (mock-port)]
      (write port [12 13 14])
      (is (= (byte 12) (byte-at port 0)))
      (is (= (byte 13) (byte-at port 1)))
      (is (= (byte 14) (byte-at port 2)))))

  (testing "arbitrary values to the port"
    (let [port (mock-port)]
      (write port 12 [13 14] (.getBytes "H" "ASCII"))

      (is (= (byte 12) (byte-at port 0)))
      (is (= (byte 13) (byte-at port 1)))
      (is (= (byte 14) (byte-at port 2)))
      (is (= (byte \H) (byte-at port 3)))))

  )

(run-tests)
