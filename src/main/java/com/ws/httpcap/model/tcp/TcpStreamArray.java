package com.ws.httpcap.model.tcp;

import com.ws.httpcap.model.*;
import com.ws.httpcap.model.http.HttpInteraction;
import com.ws.httpcap.model.http.HttpParser;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.TreeMap;

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

   int connectionCounter = 0;


   public void addPacket(TcpPacketWrapper tcpPacketWrapper){




      if (tcpPacketWrapper.isSyn()){
         System.out.println("Its a syn: " + tcpPacketWrapper);
         System.out.println("Adding stream: " );
         TcpStream tcpStream = new TcpStream(
               tcpPacketWrapper.getSrcPort(),
               tcpPacketWrapper.getDstPort(),
               tcpPacketWrapper.getSrcAddr(),
               tcpPacketWrapper.getDstAddr(),
               tcpPacketWrapper.getSequence()
         );
         streamCollection.add(
               tcpStream
         );

         if (tcpPacketWrapper.getDstPort() == serverPort){
            tcpConnections.add(new TcpConnection(connectionCounter++,tcpStream));
         } else {
            tcpConnections
                  .stream()
                  .filter(c -> c.getServerInputStream().isPartner(tcpStream))
                  .findAny().ifPresent(c -> c.setClientInputStream(tcpStream));
         }

      }

      if (!streamCollection.stream()
            .filter(s -> s.belongsInStream(tcpPacketWrapper)).findAny().isPresent()){
         System.out.println("Adding stream: " );
         TcpStream tcpStream = new TcpStream(
               tcpPacketWrapper.getSrcPort(),
               tcpPacketWrapper.getDstPort(),
               tcpPacketWrapper.getSrcAddr(),
               tcpPacketWrapper.getDstAddr(),
               tcpPacketWrapper.getSequence() -1
         );
         streamCollection.add(
               tcpStream
         );

         if (tcpPacketWrapper.getDstPort() == serverPort){
            tcpConnections.add(new TcpConnection(connectionCounter++,tcpStream));
         } else {
            tcpConnections
                  .stream()
                  .filter(c -> c.getServerInputStream().isPartner(tcpStream))
                  .findAny().ifPresent(c -> c.setClientInputStream(tcpStream));
         }
      }

      TcpStream stream = streamCollection.stream()
            .filter(s -> s.belongsInStream(tcpPacketWrapper)).findAny().orElse( null);


      if (tcpPacketWrapper.isAck()){
         if (tcpPacketWrapper.isSyn()){
            System.out.println("Its a syn-ack: " + tcpPacketWrapper);
         } else
            System.out.println("Its a ack: " + tcpPacketWrapper);
      }

      if (stream == null) {
         System.err.println("No streeem");
         return;

      }



      if (tcpPacketWrapper.hasData()) {
         stream.addPacket(tcpPacketWrapper);
         System.out.println("Adding packet to stream: " + stream + " " + tcpPacketWrapper);
      }
      if (tcpPacketWrapper.isFin())
         stream.close();

   }

   public Collection<TcpConnection> getTcpConnections() {
      return tcpConnections;
   }

  /* public Collection<HttpInteraction> getInteractions(){

      Collection<HttpInteraction> interactions = new ArrayList<>();

      for (TcpConnection tcpConnection:tcpConnections) {

         TreeMap<Long, MessageHolder> messages = new TreeMap<>();


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
   }*/
}
