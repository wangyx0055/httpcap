package com.ws.httpcap.model.http;

import com.ws.httpcap.model.MessageHolder;
import com.ws.httpcap.model.RequestHolder;
import com.ws.httpcap.model.ResponseHolder;
import com.ws.httpcap.model.tcp.TcpConnection;
import com.ws.httpcap.model.tcp.TcpPacketWrapper;
import com.ws.httpcap.model.tcp.TcpStreamArray;

import java.util.*;

/**
 * Created by wschick on 3/30/17.
 */
public class HttpMessageBuffer {

   final TcpStreamArray tcpStreamArray;
   final HttpParser httpParser;



   final Map<Integer,List<TcpPacketWrapper>> clientBuffers = new HashMap<>();
   final Map<Integer,List<TcpPacketWrapper>> serverBuffers = new HashMap<>();
   final Map<Integer,TreeMap<Long, MessageHolder>> messages = new TreeMap<>();

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
            messages.put(tcpConnection.getId(),new TreeMap<>());

         if (tcpConnection.getClientInputStream() != null)
            clientBuffers.get(tcpConnection.getId()).addAll(tcpConnection.getClientInputStream().drawFromStream());
         serverBuffers.get(tcpConnection.getId()).addAll(tcpConnection.getServerInputStream().drawFromStream());
      }

      List<HttpInteraction> interactions = new ArrayList<>();

      for (int connectionId: messages.keySet()){
         TreeMap<Long, MessageHolder> messageForStream = messages.get(connectionId);

         for (MessageHolder messageHolder: httpParser.getClientRequests(serverBuffers.get(connectionId))){
            messageForStream.put(messageHolder.getSequence(),messageHolder);
         }

         for (MessageHolder messageHolder:httpParser.getServerResponses(clientBuffers.get(connectionId))){
            messageForStream.put(messageHolder.getSequence(),messageHolder);
         }

         Iterator<MessageHolder> messageHolderIterator = messageForStream.values().iterator();


         RequestHolder requestHolder = null;

         List<Long> messagesToRemove = new ArrayList<>();

         while (messageHolderIterator.hasNext()) {
            MessageHolder messageHolder = messageHolderIterator.next();

            if (messageHolder instanceof RequestHolder)
               requestHolder = (RequestHolder) messageHolder;

            if (messageHolder instanceof ResponseHolder) {
               interactions.add(new HttpInteraction("a,",requestHolder, (ResponseHolder) messageHolder));
               if (requestHolder != null)
                  messagesToRemove.add(requestHolder.getSequence());

               messagesToRemove.add(messageHolder.getSequence());

            }

         }

         for (Long timestamp: messagesToRemove)
            messageForStream.remove(timestamp);

      }

      return interactions;



   }
}
