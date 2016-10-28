package com.ws.httpcap.api;

import com.ws.httpcap.model.PortType;

/**
 * Created by wschick on 10/28/16.
 */
public class PacketCapture {

    int port;
    String remoteHost;
    PortType portType;

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getRemoteHost() {
        return remoteHost;
    }

    public void setRemoteHost(String remoteHost) {
        this.remoteHost = remoteHost;
    }

    public PortType getPortType() {
        return portType;
    }

    public void setPortType(PortType portType) {
        this.portType = portType;
    }
}

