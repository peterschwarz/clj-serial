(ns serial.core
  (:import [purejavacomm CommPortIdentifier
                         SerialPort
                         SerialPortEventListener
                         SerialPortEvent]
           [java.io OutputStream
                    InputStream]))

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

(defn port-identifiers
  "Returns a seq representing all port identifiers visible to the system"
  []
  (enumeration-seq (raw-port-ids)))

(def ^{:deprecated "2.0.3" :doc "Deprecated; use `port-identifiers`"}
  port-ids port-identifiers)

(defn port-identifier
  ^CommPortIdentifier [^String path]
  (CommPortIdentifier/getPortIdentifier path))

(defn close! 
  "Closes an open port."
  [^Port port]
  (let [raw-port ^SerialPort (.raw-port port)]
    (.removeEventListener raw-port)
    (.close raw-port)))

(def ^{:deprecated "2.0.3" :doc "Deprecated; use `close!` instead"}
  close close!)

(defn open
  "Returns an opened serial port. Allows you to specify the

  * :baud-rate (defaults to 115200)
  * :stopbits (defaults to STOPBITS_1)
  * :databits (defaults to DATABITS_8)
  * :parity (defaults to PARITY_NONE).
  * :timeout in milliseconds (defaults to 2000)

  Additionally, setting the value of :

  (open \"/dev/ttyUSB0\")
  (open \"/dev/ttyUSB0\" :baud-rate 9200)"

  ([path & {:keys [baud-rate databits stopbits parity timeout]
             :or {baud-rate 115200, databits DATABITS_8, stopbits STOPBITS_1, parity PARITY_NONE, timeout 2000}}]
     (try
       (let [uuid     (.toString (java.util.UUID/randomUUID))
             port-id  (port-identifier path)
             raw-port ^SerialPort (.open port-id uuid timeout)
             out      (.getOutputStream raw-port)
             in       (.getInputStream  raw-port)
             _        (.setSerialPortParams raw-port baud-rate
                                            databits
                                            stopbits
                                            parity)]

         (assert (not (nil? port-id)) (str "Port specified by path " path " is not available"))
         (Port. path raw-port out in))
       (catch Exception e
         (throw (Exception. (str "Sorry, couldn't connect to the port with path " path ) e))))))

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
  [^Port port bytes]
  (let [out (.out-stream port)]
    (.write ^OutputStream out ^bytes bytes)
    (.flush ^OutputStream out)))

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

(defn skip-input!
  "Skips a specified amount of buffered input data."
  ([^Port port] (skip-input! port (.available ^InputStream (.in-stream port))))
  ([^Port port ^long to-drop]
    (.skip ^InputStream (.in-stream port) to-drop)))

(defn listen!
  "Register a function to be called for every byte received on the specified port.
  
  Only one listener is allowed at a time."
  ([^Port port handler] (listen! port handler true))
  ([^Port port handler skip-buffered?]
     (let [raw-port  ^SerialPort (.raw-port port)
           in-stream ^InputStream (.in-stream port)
           listener  (reify SerialPortEventListener
                       (serialEvent [_ event] (when (= SerialPortEvent/DATA_AVAILABLE
                                                       (.getEventType event))
                                                (handler in-stream))))]

       (when skip-buffered?
         (skip-input! port))

       (.addEventListener raw-port listener)
       (.notifyOnDataAvailable raw-port true))))

(def ^{:deprecated "2.0.3" :doc "Deprecated; use `listen!` instead"}
  listen listen!)

(defn unlisten!
  "De-register the listening fn for the specified port"
  [^Port port]
  (.removeEventListener ^SerialPort (.raw-port port)))

(def ^{:deprecated "2.0.3" :doc "Deprecated; use `unlisten!` instead"}
  remove-listener unlisten!)
