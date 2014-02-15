(ns serial.core
  (:import
   [purejavacomm CommPortIdentifier
                 SerialPort
                 SerialPortEventListener
                 SerialPortEvent]
   [java.io OutputStream
            InputStream]))

(def PORT-OPEN-TIMEOUT 2000)
(defrecord Port [path raw-port out-stream in-stream])

(defn- raw-port-ids
  "Returns the raw java Enumeration of port identifiers"
  []
  (CommPortIdentifier/getPortIdentifiers))

(defn port-ids
  "Returns a seq representing all port identifiers visible to the system"
  []
  (enumeration-seq (raw-port-ids)))


(defn close
  "Closes an open port."
  [port]
  (let [raw-port (:raw-port port)]
    (.removeEventListener raw-port)
    (.close raw-port)))

(defn open
  "Returns an opened serial port. Allows you to specify the baud-rate (defaults to 115200).
  (open \"/dev/ttyUSB0\")
  (open \"/dev/ttyUSB0\" 9200)"
  ([path] (open path 115200))
  ([path baud-rate]
     (try
       (let [uuid     (.toString (java.util.UUID/randomUUID))
             port-id  (first (filter #(= path (.getName %)) (port-ids)))
             raw-port (.open port-id uuid PORT-OPEN-TIMEOUT)
             out      (.getOutputStream raw-port)
             in       (.getInputStream  raw-port)
             _        (.setSerialPortParams raw-port baud-rate
                                            SerialPort/DATABITS_8
                                            SerialPort/STOPBITS_1
                                            SerialPort/PARITY_NONE)]

         (Port. path raw-port out in))
       (catch Exception e
         (throw (Exception. (str "Sorry, couldn't connect to the port with path " path )))))))

(defn- write-bytes
  "Writes a byte array to a port"
  [port bytes]
  (let [out (:out-stream port)]
    (.write ^OutputStream out ^bytes bytes)
    (.flush out)))

(defn- write-byte
  "Writes a byte to a port"
  [port b]
  (write-bytes port (byte-array 1 b)))

(defn- compose-byte-array [bytes]
  (byte-array (count bytes) (map #(.byteValue ^Number %) bytes)))

(defmulti write
  "Write a value to the port."
  (fn [_ obj] (class obj)))

(defmethod write (class (byte-array 0))
  [port bytes]
  (write-bytes port bytes))

(defmethod write Number
  [port value]
  (write-byte port (.byteValue value)))

(defmethod write clojure.lang.Sequential
  [port values]
  (write-bytes port (compose-byte-array values)))

(defn listen
  "Register a function to be called for every byte received on the specified port."
  ([port handler] (listen port handler true))
  ([port handler skip-buffered?]
     (let [raw-port  (:raw-port port)
           in-stream (:in-stream port)
           listener  (reify SerialPortEventListener
                       (serialEvent [_ event] (when (= SerialPortEvent/DATA_AVAILABLE (.getEventType event))
                                                (handler in-stream))))]

       (if skip-buffered?
         (let [to-drop (.available in-stream)]
           (.skip in-stream to-drop)))

       (.addEventListener raw-port listener)
       (.notifyOnDataAvailable raw-port true))))

(defn remove-listener
  "De-register the listening fn for the specified port"
  [port]
  (.removeEventListener (:raw-port port)))
