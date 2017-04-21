package com.ws.httpcap.capture;

import com.ws.httpcap.CaptureNotificationService;
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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.BlockingQueue;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * Created by wschick on 4/17/17.
 */
public class CaptureProcessingTask extends Thread {
   private static Logger logger = Logger.getLogger("CaptureProcessingTask");

   //private final PacketCapture packetCapture;
   private final int port;
   private final int captureId;
   Collection<HttpConversation> output;
   private final BlockingQueue<TimedPacket> packetQueue;
   private final TcpStreamArray streamArray;
   private final HttpMessageBuffer httpMessageBuffer;
   private final CaptureNotificationService captureNotificationService;


   private volatile boolean running;

   public CaptureProcessingTask(int port, int captureId, Collection<HttpConversation> output, BlockingQueue<TimedPacket> packetQueue, CaptureNotificationService captureNotificationService) {

      this.captureNotificationService = captureNotificationService;

      this.running = true;
      this.streamArray = new TcpStreamArray(port);

      this.httpMessageBuffer = new HttpMessageBuffer(streamArray,new HttpParser());

      this.output = output;
      this.packetQueue = packetQueue;
      this.port = port;
      this.captureId = captureId;

   }

   @Override
   public void run() {

      logger.info("Starting processing thread for capture: " + captureId );

      while (running) {
         try {
            TimedPacket packet = packetQueue.take();

            logger.fine("Processing packet: " + packet);

            TcpPacketWrapper tcpPacketWrapper = new TcpPacketWrapperImpl(packet.packet, packet.timestamp);

            streamArray.addPacket(tcpPacketWrapper);

            List<HttpInteraction> httpInteractions = new ArrayList<>();

            httpInteractions.addAll(httpMessageBuffer.getHttpInteractions());

            output.addAll(httpInteractions.stream().map(httpInteraction -> {

               ArrayList<NameValuePair> requestHeaders = new ArrayList<>();
               ArrayList<NameValuePair> responseHeaders = new ArrayList<>();

               for (Header header : httpInteraction.getHttpTimedRequest().getHttpRequest().getAllHeaders()) {
                  requestHeaders.add(new NameValuePair(header.getName(), header.getValue()));
               }

               for (Header header : httpInteraction.getHttpTimedResponse().getHttpResponse().getAllHeaders()) {
                  responseHeaders.add(new NameValuePair(header.getName(), header.getValue()));
               }

               try {
                  URI requestURI = new URI(httpInteraction.getHttpTimedRequest().getHttpRequest().getRequestLine().getUri());

                  String body = null;

                  if (httpInteraction.getHttpTimedRequest().getHttpRequest() instanceof BasicHttpEntityEnclosingRequest){
                     body = read((
                           (BasicHttpEntityEnclosingRequest)
                                 httpInteraction.getHttpTimedRequest().getHttpRequest()).getEntity()
                     );
                  }

                  HttpConversationRequest request = new HttpConversationRequest(
                        httpInteraction.getHttpTimedRequest().getHttpRequest().getRequestLine().getMethod(),
                        requestURI.getPath(),
                        URLEncodedUtils.parse(requestURI, "UTF-8").stream().map(p -> new NameValuePair(p.getName(), p.getValue())).collect(Collectors.toList()),
                        requestHeaders,
                        body
                  );

                  HttpConversationResponse reponse = new HttpConversationResponse(
                        httpInteraction.getHttpTimedResponse().getHttpResponse().getStatusLine().getStatusCode(),
                        responseHeaders,
                        read(httpInteraction.getHttpTimedResponse().getHttpResponse().getEntity())
                  );

                  HttpConversation httpConversation = new HttpConversation(
                        UUID.randomUUID().toString(),
                        request,
                        reponse,
                        httpInteraction.getHttpTimedRequest().getSrcHost(),
                        httpInteraction.getHttpTimedRequest().getDstHost(),
                        httpInteraction.getHttpTimedRequest().getSrcPort(),
                        httpInteraction.getHttpTimedRequest().getDstPort(),
                        httpInteraction.getHttpTimedRequest().getTimestamp(),
                        httpInteraction.getHttpTimedResponse().getTimestamp()
                  );

                  logger.info("Parsed HTTP conversation: " + httpConversation );

                  captureNotificationService.notifyNewInteraction(captureId,httpConversation);

                  return httpConversation;
               } catch (Exception e) {
                  throw new RuntimeException(e);
               }


            }).collect(Collectors.toList()));

         } catch (InterruptedException e){
            break;
         } catch (Exception e) {
            logger.log(Level.SEVERE,"Error processing",e);
         }
      }

      logger.info("Stopping capture: " + captureId);

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
