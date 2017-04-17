package com.ws.httpcap.capture;

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
import org.apache.http.message.BasicHttpEntityEnclosingRequest;
import org.springframework.messaging.simp.SimpMessageSendingOperations;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.BlockingQueue;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * Created by wschick on 4/17/17.
 */
public class CaptureProcessingTask extends Thread {
   private static Logger logger = Logger.getLogger("CaptureProcessingTask");

   private final PacketCapture packetCapture;
   private final BlockingQueue<TimedPacket> packetQueue;
   private final TcpStreamArray streamArray;
   private final HttpMessageBuffer httpMessageBuffer;

   SimpMessageSendingOperations messageSendingOps;

   private volatile boolean running;

   public CaptureProcessingTask(PacketCapture packetCapture, BlockingQueue<TimedPacket> packetQueue, SimpMessageSendingOperations messageSendingOperations) {

      this.messageSendingOps = messageSendingOperations;

      this.running = true;
      this.streamArray = new TcpStreamArray(packetCapture.getPort());

      this.httpMessageBuffer = new HttpMessageBuffer(streamArray,new HttpParser());

      this.packetCapture = packetCapture;
      this.packetQueue = packetQueue;

   }

   @Override
   public void run() {

      logger.info("Starting processing thread for capture: " + packetCapture.getId());

      while (running) {
         try {
            TimedPacket packet = packetQueue.take();

            logger.info("Processing packet: " + packet);

            TcpPacketWrapper tcpPacketWrapper = new TcpPacketWrapperImpl(packet.packet, packet.timestamp);

            streamArray.addPacket(tcpPacketWrapper);

            List<HttpInteraction> httpInteractions = new ArrayList<>();

            httpInteractions.addAll(httpMessageBuffer.getHttpInteractions());

            packetCapture.getHttpInteractions().addAll(httpInteractions.stream().map(httpInteraction -> {

               ArrayList<NameValuePair> requestHeaders = new ArrayList<>();
               ArrayList<NameValuePair> responseHeaders = new ArrayList<>();

               for (Header header : httpInteraction.getRequestHolder().getHttpRequest().getAllHeaders()) {
                  requestHeaders.add(new NameValuePair(header.getName(), header.getValue()));
               }

               for (Header header : httpInteraction.getResponseHolder().getHttpResponse().getAllHeaders()) {
                  responseHeaders.add(new NameValuePair(header.getName(), header.getValue()));
               }

               try {
                  URI requestURI = new URI(httpInteraction.getRequestHolder().getHttpRequest().getRequestLine().getUri());

                  String body = null;

                  if (httpInteraction.getRequestHolder().getHttpRequest() instanceof BasicHttpEntityEnclosingRequest){
                     body = read((
                           (BasicHttpEntityEnclosingRequest)
                                 httpInteraction.getRequestHolder().getHttpRequest()).getEntity()
                     );
                  }


                  HttpConversationRequest request = new HttpConversationRequest(
                        httpInteraction.getRequestHolder().getHttpRequest().getRequestLine().getMethod(),
                        requestURI.getPath(),
                        URLEncodedUtils.parse(requestURI, "UTF-8").stream().map(p -> new NameValuePair(p.getName(), p.getValue())).collect(Collectors.toList()),
                        requestHeaders,
                        body
                  );

                  HttpConversationResponse reponse = new HttpConversationResponse(
                        httpInteraction.getResponseHolder().getHttpResponse().getStatusLine().getStatusCode(),
                        responseHeaders,
                        read(httpInteraction.getResponseHolder().getHttpResponse().getEntity())
                  );

                  logger.info("Parsed HTTP conversation: ");


                  HttpConversation httpConversation = new HttpConversation(UUID.randomUUID().toString(), request, reponse);

                  messageSendingOps.convertAndSend("/capture/" + packetCapture.getId(),httpConversation);


                  return httpConversation;
               } catch (Exception e) {
                  throw new RuntimeException(e);
               }


            }).collect(Collectors.toList()));

         } catch (InterruptedException e){
            break;
         } catch (Exception e) {
            e.printStackTrace();
            System.out.println("EOF");
            break;
         }
      }

      logger.info("Stopping capture: " + packetCapture.getId());

   }

   public void stopProcessing(){
      this.running = false;
      this.interrupt();
   }

   public CaptureProcessingTask startProcessing(){
      this.start();
      return this;
   }


   public static String read(HttpEntity input)  {

      try (BufferedReader buffer = new BufferedReader(new InputStreamReader(input.getContent()))) {
         return buffer.lines().collect(Collectors.joining("\n"));
      }catch (IOException e){
         throw new RuntimeException(e);
      }
   }
}
