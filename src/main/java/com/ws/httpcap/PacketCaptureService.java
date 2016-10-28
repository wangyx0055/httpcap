package com.ws.httpcap;

import com.ws.httpcap.model.PortType;

/**
 * Created by wschick on 10/28/16.
 */
public interface PacketCaptureService {


    int startCapture(int port, PortType portType);

    void stopCapture(int id);


}
