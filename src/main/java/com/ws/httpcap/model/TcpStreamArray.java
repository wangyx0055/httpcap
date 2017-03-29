package com.ws.httpcap.model;

import com.ws.httpcap.HttpPacket;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.TreeMap;
import java.util.function.Supplier;

/**
 * Created by wschick on 3/22/17.
 */
public class TcpStreamArray {

   HttpParser httpParser = new HttpParser();


   final int serverPort;

   Collection<TcpStream> streamCollection = new ArrayList<>();

   Collection<TcpConnection> tcpConnections = new ArrayList<>();


   public TcpStreamArray(int serverPort) {
      this.serverPort = serverPort;
   }


   public void addPacket(HttpPacket httpPacket){




      if (httpPacket.getTcpPacket().getHeader().getSyn()){
         System.out.println("Its a syn: " + httpPacket);
         System.out.println("Adding stream: " );
         TcpStream tcpStream = new TcpStream(
               httpPacket.getSrcPort(),
               httpPacket.getDstPort(),
               httpPacket.getSrcAddr(),
               httpPacket.getDstAddr(),
               httpPacket.getSquence()
         );
         streamCollection.add(
               tcpStream
         );

         if (httpPacket.getDstPort() == serverPort){
            tcpConnections.add(new TcpConnection(tcpStream));
         } else {
            tcpConnections
                  .stream()
                  .filter(c -> c.getServerInputStream().isPartner(tcpStream))
                  .findAny().ifPresent(c -> c.setClientInputStream(tcpStream));
         }

      }

      TcpStream stream = streamCollection.stream()
            .filter(s -> s.belongsInStream(httpPacket )).findAny().orElse( null);


      if (httpPacket.getTcpPacket().getHeader().getAck()){
         if (httpPacket.getTcpPacket().getHeader().getSyn()){
            System.out.println("Its a syn-ack: " + httpPacket);
         } else
            System.out.println("Its a ack: " + httpPacket);
      }

      if (stream == null) {
         System.err.println("No streeem");
         return;

      }



      if (httpPacket.hasData()) {
         stream.addPacket(httpPacket);
         System.out.println("Adding packet to stream: " + stream + " " + httpPacket);
      }
      if (httpPacket.isFin())
         stream.close();

   }

   public Collection<HttpInteraction> getInteractions(){

      Collection<HttpInteraction> interactions = new ArrayList<>();

      for (TcpConnection tcpConnection:tcpConnections) {

         TreeMap<Timestamp, MessageHolder> messages = new TreeMap<>();


         for (RequestHolder httpRequest : httpParser.getClientRequests(tcpConnection.getServerInputStream().drawFromStream())) {
            //System.out.println(httpRequest.getHttpRequest().getRequestLine());

            messages.put(httpRequest.getSequence(), httpRequest);
         }

         for (ResponseHolder httpResponse : httpParser.getServerResponses(tcpConnection.getClientInputStream().drawFromStream())) {
            //System.out.println(httpResponse.getHttpResponse().getEntity());
            messages.put(httpResponse.getSequence(), httpResponse);
         }


         Iterator<MessageHolder> messageHolderIterator = messages.values().iterator();



         ResponseHolder responseHolder = null;
         RequestHolder requestHolder = null;


         while (messageHolderIterator.hasNext()) {
            MessageHolder messageHolder = messageHolderIterator.next();

            if (messageHolder instanceof RequestHolder)
               requestHolder = (RequestHolder) messageHolder;

            if (messageHolder instanceof ResponseHolder) {
               interactions.add(new HttpInteraction(requestHolder, (ResponseHolder) messageHolder));

            }

         }

         interactions.forEach(System.out::println);
      }

      return interactions;
   }
}
