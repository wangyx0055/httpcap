package com.ws.httpcap;

import com.ws.httpcap.api.*;
import com.ws.httpcap.capture.CaptureProcessingTask;
import com.ws.httpcap.capture.CaptureState;
import com.ws.httpcap.capture.PacketCollectionTask;
import com.ws.httpcap.capture.TimedPacket;
import com.ws.httpcap.model.http.HttpInteraction;
import com.ws.httpcap.model.http.HttpMessageBuffer;
import com.ws.httpcap.model.http.HttpParser;
import com.ws.httpcap.model.tcp.TcpPacketWrapper;
import com.ws.httpcap.model.tcp.TcpPacketWrapperImpl;
import com.ws.httpcap.model.tcp.TcpStreamArray;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.client.utils.URLEncodedUtils;
import org.pcap4j.core.BpfProgram;
import org.pcap4j.core.PcapHandle;
import org.pcap4j.core.PcapNetworkInterface;
import org.pcap4j.core.Pcaps;
import org.pcap4j.packet.Packet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.sql.Timestamp;
import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * Created by wschick on 3/29/17.
 */
@Service
public class PacketCaptureServiceImpl implements PacketCaptureService {

   private static Logger logger = Logger.getLogger("PacketCaptureServiceImpl");
   private final ConcurrentHashMap<Integer,PacketCapture> captures = new ConcurrentHashMap<>();
   private int currentId = 0;
   private final Map<Integer,CaptureState> captureStates = new ConcurrentHashMap<>();
   private final ConcurrentHashMap<Integer,List<HttpConversation>> conversationMap = new ConcurrentHashMap<>();



   public List<NetworkInterface> networkInterfaces = null;

   @Autowired
   CaptureNotificationService captureNotificationService;

   @Override
   public synchronized int startCapture(PacketCapture packetCapture) {

      if (packetCapture.getPort() <=0 )
         throw new RuntimeException("Capture port cannot be 0");

      if (packetCapture.getInterfaces().size() <= 0)
         throw new RuntimeException("Need at least one interface for capture");

      try {

         packetCapture.setId(currentId++);

         captureStates.put(packetCapture.getId(),new CaptureState());

         List<HttpConversation> conversations = Collections.synchronizedList(new ArrayList<>());


         BlockingQueue<TimedPacket> packetQueue = new ArrayBlockingQueue<TimedPacket>(1000);

         packetCapture.getInterfaces().forEach( networkInterface -> {

            try {

               logger.info("Starting collection on iface: " + networkInterface + " port: " + packetCapture.getPort());

               logger.info("Using buffer size: " + packetCapture.getBufferSize() + "K");


               PcapHandle handle = new PcapHandle.Builder(networkInterface)
                     .snaplen(8000000)
                     .promiscuousMode(PcapNetworkInterface.PromiscuousMode.PROMISCUOUS)
                     .timeoutMillis(1000)
                     .bufferSize(packetCapture.getBufferSize()* 1024)
                     .timestampPrecision(PcapHandle.TimestampPrecision.MICRO).build();

               handle.setFilter("tcp port " + packetCapture.getPort(), BpfProgram.BpfCompileMode.OPTIMIZE);

               captureStates.get(packetCapture.getId()).getCollectionThreads().add(
                     new PacketCollectionTask(handle,packetQueue)
                        .startCollection()
               );

            }catch (Exception ex){
               ex.printStackTrace();
            }

         });

         captureStates.get(packetCapture.getId()).setProcessingThread(
               new CaptureProcessingTask(packetCapture.getPort(),packetCapture.getId(),conversations, packetQueue, captureNotificationService)
                     .startProcessing()
         );

         packetCapture.setRunning(true);

         captures.put(packetCapture.getId(),packetCapture);
         conversationMap.put(packetCapture.getId(),conversations);

         return packetCapture.getId();


      }catch (Exception e){
         throw new RuntimeException(e);
      }
   }


   @Override
   public synchronized void stopCapture(int id) {
      if (captureStates.containsKey(id)) {
         captureStates.get(id).shutdown();
         captureStates.remove(id);
      }
   }

   @Override
   public synchronized PacketCapture getCapture(int id) {
      return captures.get(id);
   }

   @Override
   public List<HttpConversation> getConversationsForCapture(int id) {
      return conversationMap.get(id);
   }

   @Override
   public synchronized Collection<PacketCapture> getCaptures() {
      return captures.values();
   }

   @Override
   public synchronized void deleteCapture(int captureId) {
      stopCapture(captureId);
      captures.remove(captureId);
      conversationMap.remove(captureId);
   }

   @Override
   public synchronized List<NetworkInterface> getNetworkInterfaceNames() {


      if (networkInterfaces == null){
         try {
            networkInterfaces = Pcaps.findAllDevs().stream().map(i -> {
               NetworkInterface networkInterface = new NetworkInterface();
               networkInterface.setName(i.getName());
               networkInterface.setDescription(i.getDescription() == null?"":i.getDescription());
               return networkInterface;
            }).collect(Collectors.toList());
         }catch (Exception e){
            throw new RuntimeException(e);
         }
      }

      return networkInterfaces;

   }
}
