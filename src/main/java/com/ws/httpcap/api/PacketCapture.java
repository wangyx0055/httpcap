package com.ws.httpcap.api;

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
    int bufferSize;

    boolean running;

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
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

    public void setRunning(boolean running) {
        this.running = running;
    }

    public boolean isRunning() {
        return running;
    }

    public int getBufferSize() {
        return bufferSize;
    }

    public void setBufferSize(int bufferSize) {
        this.bufferSize = bufferSize;
    }
}

