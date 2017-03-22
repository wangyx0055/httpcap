package com.ws.httpcap.model;

import com.ws.httpcap.HttpPacket;

import java.util.ArrayList;
import java.util.Collection;
import java.util.function.Supplier;

/**
 * Created by wschick on 3/22/17.
 */
public class TcpStreamArray {



   final int serverPort;

   Collection<TcpStream> streamCollection = new ArrayList<>();

   public TcpStreamArray(int serverPort) {
      this.serverPort = serverPort;
   }


   public void addPacket(HttpPacket httpPacket){


      if (httpPacket.getDstPort() != serverPort &&
            httpPacket.getSrcPort() != serverPort)
         return;

      Supplier<TcpStream> createStream = () -> {
         if (httpPacket.getDstPort() == serverPort)
            return new TcpStream(
                  httpPacket.getSrcPort(),httpPacket.getDstPort(),httpPacket.getSrcAddr(),httpPacket.getDstAddr()
            );
         else
            return new TcpStream(
                  httpPacket.getDstPort(),httpPacket.getSrcPort(),httpPacket.getDstAddr(),httpPacket.getSrcAddr()
            );
      };

      TcpStream stream = streamCollection.stream()
            .filter(s -> s.belongsInStream(httpPacket )&& !s.isClosed()).findAny().orElse( createStream.get());

      if (httpPacket.getTcpPacket().getHeader().getSyn()){
         System.out.println("Its a syn!");
      }

      if (httpPacket.getTcpPacket().getHeader().getAck()){
         System.out.println("Its a ack!" + httpPacket);
      }



      if (httpPacket.hasData()) {
         stream.addPacket(httpPacket);
         System.out.println("Adding packet to stream: " + stream + " " + httpPacket);
      }
      if (httpPacket.isFin())
         stream.close();


      if (!streamCollection.contains(stream)) {
         System.out.println("Adding stream: " + stream);
         streamCollection.add(stream);
      }
   }

   public Collection<TcpStream> getStreams() {
      return streamCollection;
   }
}
