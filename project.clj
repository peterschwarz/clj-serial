(defproject clj-serial "2.0.0"
  :description "Simple serial port library. Wraps PureJavaComm."
  :url "https://github.com/peterschwarz/clj-serial"
  :license {:name "Eclipse Public License - v 1.0"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.5.1"]
                 [com.sparetimelabs/purejavacomm "0.0.21"]]
  :repositories [["javacomm" "http://www.sparetimelabs.com/maven2"]]
  :scm {:name "git"
        :url "https://github.com/peterschwarz/clj-serial"})
