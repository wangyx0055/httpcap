# HttpCap

Http traffic snooper.

## Installation

This application is built on top of [pcap](https://en.wikipedia.org/wiki/Pcap) features provided by the OS, and will
need the packet capture libraries installed.

Example of installation through YUM:

```bash
sudo yum install libpcap
```

The application is written in Java (min: 8), so this will also need to be installed.

To build the application, the included gradle wrapper can be used:

```bash
./gradlew build
```

Then the application can be run also using the gradle wrapper. In the example below, I am using **sudo**
to run the application with elevated privileges. This is a quick and dirty way to get access to the
packet capture facilities of the operating system.

```bash
sudo ./gradlew run
```
## Implementation

The packet capture implementation is build off of [pcap4j](https://github.com/kaitoy/pcap4j).

HTTP parsing is implemented by frankenstein-ing various pieces of the
[apache http client](https://hc.apache.org/httpcomponents-client-ga/).

The application can be controlled and queried over REST endpoints which are implemented using SpringMVC. The application
itself runs standalone as a Spring Boot application.

Push notifications (for newly captured messages) are sent via STOMP over websockets.

The UI is implemented in angularjs 1.x. Various frontend modules are used (eg codemirror for content rendering).


