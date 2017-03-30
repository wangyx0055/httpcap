package com.ws.httpcap;

import com.ws.httpcap.api.*;
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


         //PcapHandle handle;


         PcapNetworkInterface pcapNetworkInterface = Pcaps.getDevByName("lo0");

         PcapHandle handle = pcapNetworkInterface.openLive(80000, PcapNetworkInterface.PromiscuousMode.PROMISCUOUS, 10000);

         handle.setFilter("tcp port 3000", BpfProgram.BpfCompileMode.OPTIMIZE);

         //handle = Pcaps.openOffline("capture.out", PcapHandle.TimestampPrecision.NANO);


         TcpStreamArray streamArray = new TcpStreamArray(3000);

         HttpMessageBuffer httpMessageBuffer = new HttpMessageBuffer(streamArray,new HttpParser());




         new Thread(() ->{
            while(true) {
               try {
                  Packet packet = handle.getNextPacketEx();
                  //System.out.println(handle.getTimestamp().getNanos());

                  TcpPacketWrapper tcpPacketWrapper = new TcpPacketWrapperImpl(packet, handle.getTimestamp());

                  streamArray.addPacket(tcpPacketWrapper);

                  List<HttpInteraction> httpInteractions = new ArrayList<>();

                  httpInteractions.addAll(httpMessageBuffer.getHttpInteractions());

                  packetCapture.getHttpInteractions().addAll(httpInteractions.stream().map(httpInteraction -> {

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


               } catch (TimeoutException e) {
               } catch (Exception e) {
                  System.out.println("EOF");
                  break;
               }
            }



            handle.close();


         }).start();


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
