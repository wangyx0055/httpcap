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

