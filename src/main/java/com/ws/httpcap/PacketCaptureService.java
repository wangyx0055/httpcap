package com.ws.httpcap;

import com.ws.httpcap.api.NetworkInterface;
import com.ws.httpcap.api.PacketCapture;

import java.util.Collection;
import java.util.List;

/**
 * Created by wschick on 10/28/16.
 */
public interface PacketCaptureService {


    int startCapture(PacketCapture packetCapture);

    void stopCapture(int id);

    PacketCapture getCapture(int id);

    Collection<PacketCapture> getCaptures();

    List<NetworkInterface> getNetworkInterfaceNames();


}
