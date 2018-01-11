(defproject clj-serial "2.0.4-SNAPSHOT"
  :description "Simple serial port library. Wraps PureJavaComm."
  :url "https://github.com/peterschwarz/clj-serial"
  :license {:name "Eclipse Public License - v 1.0"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [com.github.purejavacomm/purejavacomm "1.0.2.RELEASE"]]
  :profiles {:1.6 {:dependencies [[org.clojure/clojure "1.6.0"]]}
             :1.7 {:dependencies [[org.clojure/clojure "1.7.0"]]}}
  :scm {:name "git"
        :url "https://github.com/peterschwarz/clj-serial"})
