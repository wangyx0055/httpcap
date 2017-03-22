package com.ws.httpcap;

import org.pcap4j.packet.*;

import java.net.InetAddress;
import java.sql.Timestamp;

/**
 * Created by wschick on 11/17/16.
 */
public class HttpPacket {

   //EthernetPacket ethernetPacket;
   IpPacket ipPacket;
   TcpPacket tcpPacket;
   Timestamp timestamp;
   byte[] content;

   public HttpPacket(Packet packet, Timestamp timestamp){

      this.timestamp = timestamp;
      ipPacket = (IpPacket)packet.getPayload();
      tcpPacket = (TcpPacket)ipPacket.getPayload();
      if (tcpPacket.getPayload() != null)
         content = tcpPacket.getPayload().getRawData();
   }

   public boolean hasData(){
      return content != null;
   }


   public boolean isDataPacket(){
      return tcpPacket.getHeader().getPsh();
   }

   public boolean isFin(){
      return tcpPacket.getHeader().getFin() || tcpPacket.getHeader().getRst();
   }

   public TcpPacket getTcpPacket() {
      return tcpPacket;
   }

   public int getDstPort(){
      return tcpPacket.getHeader().getDstPort().valueAsInt();
   }

   public int getSrcPort(){
      return tcpPacket.getHeader().getSrcPort().valueAsInt();
   }

   public InetAddress getSrcAddr(){
      return ipPacket.getHeader().getSrcAddr();
   }

   public InetAddress getDstAddr(){
      return ipPacket.getHeader().getDstAddr();
   }

   public long getSquence(){
      return tcpPacket.getHeader().getSequenceNumberAsLong();
   }

   public byte[] getContent() {
      return content;
   }


   public Timestamp getTimestamp() {
      return timestamp;
   }

   @Override
   public String toString() {
      return "Packet{" +
            "seq=" + getSquence() +
            ",src=" + getSrcPort() +
            ",dst=" + getDstPort() +
            ",len=" + (getContent() == null?"na":getContent().length) + "} " ;
   }
}
