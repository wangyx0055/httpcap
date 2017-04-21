package com.ws.httpcap.model.http;

import org.apache.http.HttpResponse;

/**
 * Created by wschick on 3/22/17.
 */
public class HttpTimedResponse implements HttpTimedMessage {

   private final Long timestamp;
   private final HttpResponse httpResponse;
   private final String srcHost;
   private final int   srcPort;
   private final String dstHost;
   private final int  dstPort;

   public HttpTimedResponse(long timestamp, HttpResponse httpResponse, String srcHost, int srcPort, String dstHost, int dstPort) {
      this.timestamp = timestamp;
      this.httpResponse = httpResponse;
      this.srcHost = srcHost;
      this.srcPort = srcPort;
      this.dstHost = dstHost;
      this.dstPort = dstPort;
   }

   public HttpResponse getHttpResponse() {
      return httpResponse;
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
      return "HttpTimedResponse{" +
            "timestamp=" + timestamp +
            ", httpResponse=" + httpResponse.getStatusLine() +
            '}';
   }
}
