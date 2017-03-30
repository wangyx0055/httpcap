package com.ws.httpcap.model.tcp;

import org.pcap4j.packet.*;

import java.net.InetAddress;
import java.sql.Timestamp;

/**
 * Created by wschick on 11/17/16.
 */
public class TcpPacketWrapperImpl implements TcpPacketWrapper {

   //EthernetPacket ethernetPacket;
   IpPacket ipPacket;
   TcpPacket tcpPacket;
   Timestamp timestamp;
   byte[] content;

   public TcpPacketWrapperImpl(Packet packet, Timestamp timestamp){

      this.timestamp = timestamp;
      ipPacket = (IpPacket)packet.getPayload();
      tcpPacket = (TcpPacket)ipPacket.getPayload();
      if (tcpPacket.getPayload() != null)
         content = tcpPacket.getPayload().getRawData();
   }

   @Override
   public boolean hasData(){
      return content != null;
   }

   @Override
   public boolean isSyn() {
      return tcpPacket.getHeader().getSyn();
   }

   @Override
   public boolean isAck() {
      return tcpPacket.getHeader().getAck();
   }

   @Override
   public boolean isFin() {
      return tcpPacket.getHeader().getFin();
   }

   @Override
   public int getDstPort(){
      return tcpPacket.getHeader().getDstPort().valueAsInt();
   }

   @Override
   public int getSrcPort(){
      return tcpPacket.getHeader().getSrcPort().valueAsInt();
   }

   @Override
   public InetAddress getSrcAddr(){
      return ipPacket.getHeader().getSrcAddr();
   }

   @Override
   public InetAddress getDstAddr(){
      return ipPacket.getHeader().getDstAddr();
   }

   @Override
   public long getSequence(){
      return tcpPacket.getHeader().getSequenceNumberAsLong();
   }

   @Override
   public byte[] getContent() {
      return content;
   }


   @Override
   public Long getTimestamp() {
      return timestamp.getTime();
   }

   @Override
   public String toString() {
      return "Packet{" +
            "seq=" + getSequence() +
            ",src=" + getSrcPort() +
            ",dst=" + getDstPort() +
            ",len=" + (getContent() == null?"na":getContent().length) + "} " ;
   }
}
