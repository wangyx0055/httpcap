package com.ws.httpcap;

import com.ws.httpcap.api.*;
import com.ws.httpcap.model.HttpInteraction;
import com.ws.httpcap.model.PortType;
import com.ws.httpcap.model.TcpStreamArray;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.client.utils.URLEncodedUtils;
import org.pcap4j.core.BpfProgram;
import org.pcap4j.core.PcapHandle;
import org.pcap4j.core.PcapNetworkInterface;
import org.pcap4j.core.Pcaps;
import org.pcap4j.packet.Packet;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

/**
 * Created by wschick on 3/29/17.
 */
@Service
public class PacketCaptureServiceImpl implements PacketCaptureService {

   ConcurrentHashMap<Integer,PacketCapture> captures = new ConcurrentHashMap<>();

   int currentId = 0;

   @Override
   public int startCapture(PacketCapture packetCapture) {
      try {
         byte[] bytes = new byte[0];


         PcapHandle handle;


         PcapNetworkInterface pcapNetworkInterface = Pcaps.getDevByName("lo0");

         //handle = pcapNetworkInterface.openLive(8000, PcapNetworkInterface.PromiscuousMode.PROMISCUOUS, 1000);

         //handle.setFilter("tcp port 8080", BpfProgram.BpfCompileMode.OPTIMIZE);

         handle = Pcaps.openOffline("capture.out", PcapHandle.TimestampPrecision.NANO);


         TcpStreamArray streamArray = new TcpStreamArray(8080);

         for (int i = 0; i < 400; i++) {
            try {
               Packet packet = handle.getNextPacketEx();
               //System.out.println(handle.getTimestamp().getNanos());

               HttpPacket httpPacket = new HttpPacket(packet, handle.getTimestamp());

               streamArray.addPacket(httpPacket);


            } catch (TimeoutException e) {
            } catch (EOFException e) {
               System.out.println("EOF");
               break;
            }
         }



         handle.close();

         packetCapture.setHttpInteractions(streamArray.getInteractions().stream().map(httpInteraction -> {

            ArrayList<NameValuePair> requestHeaders = new ArrayList<>();
            ArrayList<NameValuePair> responseHeaders = new ArrayList<>();

            for (Header header : httpInteraction.getRequestHolder().getHttpRequest().getAllHeaders()) {
               requestHeaders.add(new NameValuePair(header.getName(), header.getValue()));
            }

            for (Header header: httpInteraction.getResponseHolder().getHttpResponse().getAllHeaders()){
               responseHeaders.add(new NameValuePair(header.getName(),header.getValue()));
            }

            try {
               URI requestURI = new URI(httpInteraction.getRequestHolder().getHttpRequest().getRequestLine().getUri());

               HttpConversationRequest request = new HttpConversationRequest(
                     httpInteraction.getRequestHolder().getHttpRequest().getRequestLine().getMethod(),
                     requestURI.getPath(),
                     URLEncodedUtils.parse(requestURI,"UTF-8").stream().map(p -> new NameValuePair(p.getName(),p.getValue())).collect(Collectors.toList()),
                     requestHeaders
               );

               HttpConversationResponse reponse = new HttpConversationResponse(
                     httpInteraction.getResponseHolder().getHttpResponse().getStatusLine().getStatusCode(),
                     responseHeaders,
                     read(httpInteraction.getResponseHolder().getHttpResponse().getEntity())
               );


               return new HttpConversation(UUID.randomUUID().toString(),request, reponse);
            }catch (Exception e){
               throw new RuntimeException(e);
            }


         }).collect(Collectors.toList()));

         packetCapture.setId(currentId++);

         captures.put(packetCapture.getId(),packetCapture);

         return packetCapture.getId();


      }catch (Exception e){
         throw new RuntimeException(e);
      }
   }

   public static String read(HttpEntity input)  {

      try (BufferedReader buffer = new BufferedReader(new InputStreamReader(input.getContent()))) {
         return buffer.lines().collect(Collectors.joining("\n"));
      }catch (IOException e){
         throw new RuntimeException(e);
      }
   }

   @Override
   public void stopCapture(int id) {

   }

   @Override
   public PacketCapture getCapture(int id) {
      return captures.get(id);
   }

   @Override
   public Collection<PacketCapture> getCaptures() {
      return captures.values();
   }

   @Override
   public List<NetworkInterface> getNetworkInterfaceNames() {

      try {
         return Pcaps.findAllDevs().stream().map(i -> {
            NetworkInterface networkInterface = new NetworkInterface();
            networkInterface.setName(i.getName());
            networkInterface.setDescription(i.getDescription() == null?i.getName():i.getDescription());
            return networkInterface;
         }).collect(Collectors.toList());
      }catch (Exception e){
         throw new RuntimeException(e);
      }

   }
}
