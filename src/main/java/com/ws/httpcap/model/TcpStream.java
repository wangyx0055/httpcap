package com.ws.httpcap.model;

import com.ws.httpcap.Buffer;
import com.ws.httpcap.HttpPacket;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.io.DefaultHttpRequestParser;
import org.apache.http.impl.io.DefaultHttpResponseParser;

import java.io.ByteArrayOutputStream;
import java.net.InetAddress;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;
import java.util.stream.Collectors;

/**
 * Created by wschick on 3/22/17.
 */
public class TcpStream {

   final int srcPort;
   final int dstPort;

   final InetAddress srcAddr;
   final InetAddress dstAddr;

   TreeMap<Long,HttpPacket> incomingPackets = new TreeMap<>();

   boolean closed = false;

   long sequence;

   public TcpStream(int srcPort, int dstPort, InetAddress srcAddr, InetAddress dstAddr,long sequence) {
      this.srcPort = srcPort;
      this.dstPort = dstPort;
      this.srcAddr = srcAddr;
      this.dstAddr = dstAddr;
      this.sequence = sequence +1;
   }

   public boolean belongsInStream(HttpPacket httpPacket){
      if (httpPacket.getSrcAddr().equals(srcAddr) && httpPacket.getSrcPort() == srcPort
            && httpPacket.getDstAddr().equals(dstAddr) && httpPacket.getDstPort() == dstPort)
         return true;

      return false;
   }

   public List<HttpPacket> drawFromStream(){
      long seq = sequence;

      for (HttpPacket packet:incomingPackets.values()){
         if (seq != packet.getSquence()) {
            System.out.println("uh oh, expected: " + seq +", got: " + packet.getSquence() + " diff: " + ( packet.getSquence() - seq));
            //seq = packet.getSquence();
         }

         seq+=packet.getContent().length;

      }


      return new ArrayList<>(incomingPackets.values());
   }


   public void close(){
      closed = true;
   }

   public boolean isClosed() {
      return closed;
   }


   public void addPacket(HttpPacket httpPacket){
      incomingPackets.put(httpPacket.getSquence(),httpPacket);
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
