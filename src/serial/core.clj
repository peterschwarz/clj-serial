(ns serial.core
  (:import
   [purejavacomm CommPortIdentifier
                 SerialPortEventListener
                 SerialPortEvent]
   [java.io OutputStream
            InputStream]))

(def ^{:private true} PORT-OPEN-TIMEOUT 2000)

(def  DATABITS_5  5)
(def  DATABITS_6  6)
(def  DATABITS_7  7)
(def  DATABITS_8  8)
(def  PARITY_NONE   0)
(def  PARITY_ODD    1)
(def  PARITY_EVEN   2)
(def  PARITY_MARK   3)
(def  PARITY_SPACE  4)
(def  STOPBITS_1    1)
(def  STOPBITS_2    2)
(def  STOPBITS_1_5  3)
(def  FLOWCONTROL_NONE        0)
(def  FLOWCONTROL_RTSCTS_IN   1)
(def  FLOWCONTROL_RTSCTS_OUT  2)
(def  FLOWCONTROL_XONXOFF_IN  4)
(def  FLOWCONTROL_XONXOFF_OUT 8)


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
  "Returns an opened serial port. Allows you to specify the :baud-rate (defaults to 115200),
  :stopbits (defaults to STOPBITS_1), :databits (defaults to DATABITS_8) and :parity (defaults to PARITY_NONE).

  Additionally, setting the value of :

  (open \"/dev/ttyUSB0\")
  (open \"/dev/ttyUSB0\" :baud-rate 9200)"

  ([path [& {:keys [baud-rate databits stopbits parity]
             :or {baud-rate 115200, databits DATABITS_8, stopbits STOPBITS_1, parity PARITY_NONE}}]]
     (try
       (let [uuid     (.toString (java.util.UUID/randomUUID))
             port-id  (first (filter #(= path (.getName %)) (port-ids)))
             raw-port (.open port-id uuid PORT-OPEN-TIMEOUT)
             out      (.getOutputStream raw-port)
             in       (.getInputStream  raw-port)
             _        (.setSerialPortParams raw-port baud-rate
                                            databits
                                            stopbits
                                            parity)]

         (Port. path raw-port out in))
       (catch Exception e
         (throw (Exception. (str "Sorry, couldn't connect to the port with path " path )))))))

(defprotocol Bytable
  (to-bytes [this] "Converts the type to bytes"))

(extend-protocol Bytable
  (class (byte-array 0))
  (to-bytes [this] this)

  Number
  (to-bytes [this] (byte-array 1 (.byteValue this)))

  clojure.lang.Sequential
  (to-bytes [this] (byte-array (count this)(map #(.byteValue ^Number %) this))))

(defn- write-bytes
  "Writes a byte array to a port"
  [port bytes]
  (let [out (:out-stream port)]
    (.write ^OutputStream out ^bytes bytes)
    (.flush out)))

(defn write
  "Writes the given data to the port and returns it. All number literals are treated as bytes.
  By extending the protocol Bytable, any arbitray values can be sent to the output stream.
  For example:
     (extend-protocol Bytable
      String
      (to-bytes [this] (.getBytes this \"ASCII\")))"
  [port & data]
  (doseq [x data]
    (write-bytes port (to-bytes x)))
  port)


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

