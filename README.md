# clj-serial

[![Build Status](https://travis-ci.org/peterschwarz/clj-serial.png?branch=master)](https://travis-ci.org/peterschwarz/clj-serial)

A simple library for serial port communication with Clojure. Although serial communciation may be considered old tech, it's useful for a communicating with a plethora of devices including exciting new hardware such as the [Monome](http://monome.org) and the [Arduino](http://arduino.cc).  It's powerd by [PureJavaComm] (https://github.com/nyholku/purejavacomm) for serial communication


## Installation

Add the following to your `project.clj` dependencies:

    [clj-serial "2.0.2"]

## Usage

### Using the library

Just make sure you pull in the `serial.core` namespace using something like:

    (use 'serial.core)

### Finding your port identifier

In order to connect to your serial device you need to know the path of the file it presents itself on. `serial.util` provides a simple function to list these paths out:

    => (use 'serial.util)

    => (list-ports)

    /dev/tty.usbmodemfa141
    /dev/cu.usbmodemfa141
    /dev/tty.Bluetooth-PDA-Sync
    /dev/cu.Bluetooth-PDA-Sync
    /dev/tty.Bluetooth-Modem
    /dev/cu.Bluetooth-Modem

In this case, we have an Arduino connected to `/dev/tty.usbmodemfa141`.

### Connecting with a port identifier

When you know the path to the serial port, connecting is just as simple as:

    (open "/dev/tty.usbmodemfa141")

However, you'll want to bind the result so you can use it later:

    (def port (open "/dev/tty.usbmodemfa141"))

### Reading bytes

If you wish to get raw access to the `InputStream` this is possible with the function `listen`. This allows you to specify a handler that will get called every time there is data available on the port and will pass your handler the `InputStream` to allow you to directly `.read` bytes from it.

When the handler is first registered, the bytes that have been buffered on the serial port are dropped by default. This can be changed by passing false to `on-byte`, `on-n-bytes` or `listen` as an optional last argument.

Only one listener may be registered at a time. If you want to fork the incoming datastream to a series of streams, you might want to consider using lamina. You can then register a handler which simply enqueues the incoming serial data to a lamina channel which you may then fork and map according to your whim.

Finally, you may remove your listener with `remove-listener`.

### Writing bytes

The simplest way to write bytes is by passing a byte array to `write`:

    (write port my-byte-array)

This also works with any `Number`

    (write port 20)

As well as any `Sequential`

    (write port [0xf0 0x79 0xf7])

### Closing the port

Simply use the `close` function:

    (close port)

## Contributors

* Peter Schwarz

Forked from [samaaron/serial-port](https://github.com/samaaron/serial-port), by

* Sam Aaron
* Jeff Rose

## License

Distributed under the Eclipse Public License, the same as Clojure.
