package com.ws.httpcap.model.tcp;

import org.pcap4j.packet.TcpPacket;

import java.net.InetAddress;
import java.sql.Timestamp;

/**
 * Created by wschick on 3/30/17.
 */
public interface TcpPacketWrapper {
   boolean hasData();

   boolean isSyn();
   boolean isAck();
   boolean isFin();

   //TcpPacket getTcpPacket();

   int getDstPort();

   int getSrcPort();

   InetAddress getSrcAddr();

   InetAddress getDstAddr();

   long getSequence();

   byte[] getContent();

   Long getTimestamp();
}
