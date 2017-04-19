package com.ws.httpcap.model.tcp;

import com.ws.httpcap.model.*;
import com.ws.httpcap.model.http.HttpInteraction;
import com.ws.httpcap.model.http.HttpParser;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.TreeMap;
import java.util.logging.Logger;

/**
 * Created by wschick on 3/22/17.
 */
public class TcpStreamArray {

   private static final Logger logger = Logger.getLogger("TcpStreamArray");

   private final int serverPort;
   private final Collection<TcpStream> streamCollection = new ArrayList<>();
   private final Collection<TcpConnection> tcpConnections = new ArrayList<>();


   public TcpStreamArray(int serverPort) {
      this.serverPort = serverPort;
   }

   private int connectionCounter = 0;


   public void addPacket(TcpPacketWrapper tcpPacketWrapper){

      if (tcpPacketWrapper.isSyn()){
         TcpStream tcpStream = new TcpStream(
               tcpPacketWrapper.getSrcPort(),
               tcpPacketWrapper.getDstPort(),
               tcpPacketWrapper.getSrcAddr(),
               tcpPacketWrapper.getDstAddr(),
               tcpPacketWrapper.getSequence()
         );

         logger.info("Watching newly opened connection: " + tcpStream);

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
         TcpStream tcpStream = new TcpStream(
               tcpPacketWrapper.getSrcPort(),
               tcpPacketWrapper.getDstPort(),
               tcpPacketWrapper.getSrcAddr(),
               tcpPacketWrapper.getDstAddr(),
               tcpPacketWrapper.getSequence() -1
         );

         logger.info("Watching existing connection: " + tcpStream);

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


      if (stream == null) {
         logger.severe("No stream available for packet: " + tcpPacketWrapper);
         return;
      }



      if (tcpPacketWrapper.hasData()) {
         stream.addPacket(tcpPacketWrapper);
         logger.fine("Adding packet to stream: " + stream + " " + tcpPacketWrapper);
      }
      if (tcpPacketWrapper.isFin()) {
         logger.info("Closing stream: " + stream);
         stream.close();
      }

   }

   public Collection<TcpConnection> getTcpConnections() {
      return tcpConnections;
   }

}
