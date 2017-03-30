package com.ws.httpcap.api;

import com.ws.httpcap.model.PortType;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Created by wschick on 10/28/16.
 */
public class PacketCapture {

    int id;
    int port;
    List<String> interfaces = new ArrayList<>();



    String remoteHost;
    PortType portType;
    Collection<HttpConversation> httpInteractions  = new ArrayList<>();

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

    public Collection<HttpConversation> getHttpInteractions() {
        return httpInteractions;
    }

    public void setHttpInteractions(Collection<HttpConversation> httpInteractions) {
        this.httpInteractions = httpInteractions;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public List<String> getInterfaces() {
        return interfaces;
    }

    public void setInterfaces(List<String> interfaces) {
        this.interfaces = interfaces;
    }
}

