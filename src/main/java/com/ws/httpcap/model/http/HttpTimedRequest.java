package com.ws.httpcap.model.http;

import org.apache.http.HttpRequest;

/**
 * Created by wschick on 3/22/17.
 */
public class HttpTimedRequest implements HttpTimedMessage {

   private final Long timestamp;
   private final HttpRequest httpRequest;
   private final String srcHost;
   private final int   srcPort;
   private final String dstHost;
   private final int  dstPort;

   public HttpTimedRequest(long timestamp, HttpRequest httpRequest, String srcHost, int srcPort, String dstHost, int dstPort) {
      this.timestamp = timestamp;
      this.httpRequest = httpRequest;
      this.srcHost = srcHost;
      this.srcPort = srcPort;
      this.dstHost = dstHost;
      this.dstPort = dstPort;
   }

   public HttpRequest getHttpRequest() {
      return httpRequest;
   }

   public Long getTimestamp() {
      return timestamp;
   }

   @Override
   public String getSrcHost() {
      return srcHost;
   }

   @Override
   public String getDstHost() {
      return dstHost;
   }

   @Override
   public int getSrcPort() {
      return srcPort;
   }

   @Override
   public int getDstPort() {
      return dstPort;
   }

   @Override
   public String toString() {
      return "HttpTimedRequest{" +
            "timestamp=" + timestamp +
            ", httpRequest=" + httpRequest.getRequestLine() +
            '}';
   }
}
