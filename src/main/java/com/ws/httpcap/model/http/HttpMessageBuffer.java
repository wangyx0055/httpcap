package com.ws.httpcap.model.http;

import com.ws.httpcap.model.tcp.TcpConnection;
import com.ws.httpcap.model.tcp.TcpPacketWrapper;
import com.ws.httpcap.model.tcp.TcpStreamArray;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by wschick on 3/30/17.
 */
public class HttpMessageBuffer {

   final TcpStreamArray tcpStreamArray;
   final HttpParser httpParser;



   final Map<Integer,List<TcpPacketWrapper>> clientBuffers = new HashMap<>();
   final Map<Integer,List<TcpPacketWrapper>> serverBuffers = new HashMap<>();
   final Map<Integer,List<HttpTimedMessage>> messages = new TreeMap<>();

   public HttpMessageBuffer(TcpStreamArray tcpStreamArray, HttpParser httpParser) {
      this.tcpStreamArray = tcpStreamArray;
      this.httpParser = httpParser;
   }

   public Collection<HttpInteraction> getHttpInteractions(){

      for (TcpConnection tcpConnection:tcpStreamArray.getTcpConnections()){
         if (!clientBuffers.containsKey(tcpConnection.getId()))
            clientBuffers.put(tcpConnection.getId(),new ArrayList<>());

         if (!serverBuffers.containsKey(tcpConnection.getId()))
            serverBuffers.put(tcpConnection.getId(),new ArrayList<>());

         if (!messages.containsKey(tcpConnection.getId()))
            messages.put(tcpConnection.getId(),new ArrayList<>());

         if (tcpConnection.getClientInputStream() != null)
            clientBuffers.get(tcpConnection.getId()).addAll(tcpConnection.getClientInputStream().drawFromStream());
         serverBuffers.get(tcpConnection.getId()).addAll(tcpConnection.getServerInputStream().drawFromStream());
      }

      List<HttpInteraction> interactions = new ArrayList<>();

      for (int connectionId: messages.keySet()){
         List<HttpTimedMessage> messageForStream = messages.get(connectionId);

         messageForStream.addAll(
               httpParser.getClientRequests(
                     serverBuffers.get(connectionId)).stream().collect(Collectors.toList()
               )
         );

         messageForStream.addAll(
               httpParser.getServerResponses(
                     clientBuffers.get(connectionId)).stream().collect(Collectors.toList()
               )
         );

         Collections.sort(messageForStream,(a,b) ->{

            if (a.getTimestamp() > b.getTimestamp())
               return 1;
            else if (a.getTimestamp() < b.getTimestamp())
               return -1;
            else {
               if (a instanceof HttpTimedRequest){
                  return -1;
               }

               return 1;
            }

         });

         Iterator<HttpTimedMessage> messageHolderIterator = messageForStream.iterator();


         HttpTimedRequest httpTimedRequest = null;

         List<HttpTimedMessage> messagesToRemove = new ArrayList<>();

         while (messageHolderIterator.hasNext()) {
            HttpTimedMessage httpTimedMessage = messageHolderIterator.next();

            if (httpTimedMessage instanceof HttpTimedRequest)
               httpTimedRequest = (HttpTimedRequest) httpTimedMessage;

            if (httpTimedMessage instanceof HttpTimedResponse) {
               interactions.add(new HttpInteraction("a,", httpTimedRequest, (HttpTimedResponse) httpTimedMessage));
               if (httpTimedRequest != null)
                  messagesToRemove.add(httpTimedRequest);

               messagesToRemove.add(httpTimedMessage);

            }

         }

         messageForStream.removeAll(messagesToRemove);

      }

      return interactions;



   }
}
