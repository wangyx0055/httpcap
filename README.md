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
