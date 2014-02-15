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
  (-> port
        :out-stream
        .toByteArray
        ByteBuffer/wrap
        (.get index)))

(defn- read-last
  [port]
    (-> port
        :out-stream
        .toByteArray
        ByteBuffer/wrap
        .get))

(deftest write-test
  (testing "Bytes should be written to the Port's output stream"
    (let [port (mock-port)]
      (write port (byte-array (.getBytes "Hello" "UTF-8")))
      (is (= "Hello"
             (.toString (:out-stream port) "UTF-8")))))

  (testing "An int should be written to the Port's output stream"
    (let [port (mock-port)]
      (write port 12)
      (is (= (byte 12) (read-last port)))))

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

  )

(run-tests)
