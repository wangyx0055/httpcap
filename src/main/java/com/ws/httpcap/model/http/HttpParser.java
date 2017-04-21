package com.ws.httpcap.model.http;

import com.ws.httpcap.model.tcp.TcpPacketWrapper;
import org.apache.http.*;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.io.DefaultHttpRequestParser;
import org.apache.http.impl.io.DefaultHttpResponseParser;
import org.apache.http.message.BasicHttpEntityEnclosingRequest;

import java.util.List;
import java.util.logging.Logger;

/**
 * Created by wschick on 3/22/17.
 */
public class HttpParser {


   PacketParser packetParser = new PacketParser();

   HttpParserUtil httpParserUtil = new HttpParserUtil();

   private static Logger logger = Logger.getLogger("HttpParser");

   public List<HttpTimedResponse> getServerResponses(List<TcpPacketWrapper> packets){

      return packetParser.parsePacketsToMessages(packets,(buffer,timestamp,tcpPacketWrapper) ->{
         DefaultHttpResponseParser parser = new DefaultHttpResponseParser(buffer);

         HttpResponse httpResponse = parser.parse();

         httpResponse.setEntity(
               new ByteArrayEntity(
                     httpParserUtil.readAll(
                           httpParserUtil.extractHttpEntity(buffer, httpResponse).getContent()
                     )
               )
         );

         httpResponse.setEntity(
               httpParserUtil.decompressIfNeeded(httpResponse, httpResponse.getEntity())
         );

         httpResponse.setEntity(new ByteArrayEntity(
               httpParserUtil.readAll(httpResponse.getEntity().getContent())
         ));
         httpParserUtil.validateContent(httpResponse);

         return  new HttpTimedResponse(timestamp, httpResponse,
               tcpPacketWrapper.getSrcAddr().getHostAddress(),
               tcpPacketWrapper.getSrcPort(),
               tcpPacketWrapper.getDstAddr().getHostAddress(),
               tcpPacketWrapper.getDstPort()
         );
      });
   }

   public List<HttpTimedRequest> getClientRequests(List<TcpPacketWrapper> packets){
      return packetParser.parsePacketsToMessages(packets,(buffer,timestamp,tcpPacketWrapper) ->{
         DefaultHttpRequestParser parser = new DefaultHttpRequestParser(
               buffer
         );

         HttpRequest httpRequest = parser.parse();

         if (httpRequest instanceof BasicHttpEntityEnclosingRequest){

            BasicHttpEntityEnclosingRequest entityEnclosingRequest = (BasicHttpEntityEnclosingRequest)httpRequest;

            entityEnclosingRequest.setEntity(new ByteArrayEntity(
                  httpParserUtil.readAll(
                        httpParserUtil.extractHttpEntity(buffer,entityEnclosingRequest).getContent()
                  )
            ));

            entityEnclosingRequest.setEntity(
                  httpParserUtil.decompressIfNeeded(entityEnclosingRequest,entityEnclosingRequest.getEntity())
            );

            entityEnclosingRequest.setEntity(new ByteArrayEntity(
                  httpParserUtil.readAll(entityEnclosingRequest.getEntity().getContent())
            ));
         }

         return new HttpTimedRequest(timestamp, httpRequest,
               tcpPacketWrapper.getSrcAddr().getHostAddress(),
               tcpPacketWrapper.getSrcPort(),
               tcpPacketWrapper.getDstAddr().getHostAddress(),
               tcpPacketWrapper.getDstPort()
         );
      });
   }



}
