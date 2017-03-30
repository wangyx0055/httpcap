package com.ws.httpcap.model.tcp;

import org.pcap4j.packet.Packet;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

/**
 * Created by wschick on 3/22/17.
 */
public class TcpStream {

   final int srcPort;
   final int dstPort;

   final InetAddress srcAddr;
   final InetAddress dstAddr;

   TreeMap<Long,TcpPacketWrapper> incomingPackets = new TreeMap<>();

   boolean closed = false;

   long sequence;

   public TcpStream(int srcPort, int dstPort, InetAddress srcAddr, InetAddress dstAddr,long sequence) {
      this.srcPort = srcPort;
      this.dstPort = dstPort;
      this.srcAddr = srcAddr;
      this.dstAddr = dstAddr;
      this.sequence = sequence +1;
   }

   public boolean belongsInStream(TcpPacketWrapper tcpPacketWrapper){
      if (tcpPacketWrapper.getSrcAddr().equals(srcAddr) && tcpPacketWrapper.getSrcPort() == srcPort
            && tcpPacketWrapper.getDstAddr().equals(dstAddr) && tcpPacketWrapper.getDstPort() == dstPort)
         return true;

      return false;
   }

   public List<TcpPacketWrapper> drawFromStream(){

      List<TcpPacketWrapper> tcpPacketWrappers = new ArrayList<>();

      for (TcpPacketWrapper packet:incomingPackets.values()){
         if (sequence != packet.getSequence()) {
            System.out.println("Out of sequence");
            break;
         }

         sequence+=packet.getContent().length;

         tcpPacketWrappers.add(packet);
      }

      for (TcpPacketWrapper packetWrapper: tcpPacketWrappers)
         incomingPackets.remove(packetWrapper.getSequence());


      return tcpPacketWrappers;
   }


   public void close(){
      closed = true;
   }

   public boolean isClosed() {
      return closed;
   }


   public void addPacket(TcpPacketWrapper tcpPacketWrapper){
      incomingPackets.put(tcpPacketWrapper.getSequence(), tcpPacketWrapper);
   }

   public boolean isPartner(TcpStream tcpStream){
      return tcpStream.srcPort == dstPort &&
            tcpStream.dstPort == srcPort &&
            tcpStream.srcAddr.equals(dstAddr) &&
            tcpStream.dstAddr.equals(srcAddr);
   }

   @Override
   public String toString() {
      return "TcpStream{" +
            "dstAddr=" + dstAddr +
            ", srcPort=" + srcPort +
            ", dstPort=" + dstPort +
            ", srcAddr=" + srcAddr +
            '}';
   }


}
