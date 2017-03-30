package com.ws.httpcap.model.tcp;

import org.pcap4j.packet.TcpPacket;

import java.net.InetAddress;
import java.sql.Timestamp;

/**
 * Created by wschick on 3/30/17.
 */
public class MockTcpPacketWrapper implements TcpPacketWrapper {

   boolean hasData;
   boolean syn;
   boolean ack;
   boolean fin;
   int dstPort;
   int srcPort;
   InetAddress srcAddr;
   InetAddress dstAddr;
   long sequence;
   byte[] content;
   Long timestamp;



   @Override
   public boolean hasData() {
      return hasData;
   }

   @Override
   public boolean isSyn() {
      return syn;
   }

   @Override
   public boolean isAck() {
      return ack;
   }

   @Override
   public boolean isFin() {
      return fin;
   }

   @Override
   public int getDstPort() {
      return dstPort;
   }

   @Override
   public int getSrcPort() {
      return srcPort;
   }

   @Override
   public InetAddress getSrcAddr() {
      return srcAddr;
   }

   @Override
   public InetAddress getDstAddr() {
      return dstAddr;
   }

   @Override
   public long getSequence() {
      return sequence;
   }

   @Override
   public byte[] getContent() {
      return content;
   }

   @Override
   public Long getTimestamp() {
      return timestamp;
   }
}
