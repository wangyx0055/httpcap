package com.ws.httpcap.model.http;

import org.apache.http.HttpRequest;

/**
 * Created by wschick on 3/22/17.
 */
public class HttpTimedRequest implements HttpTimedMessage {

   private final Long timestamp;
   private final HttpRequest httpRequest;

   public HttpTimedRequest(long timestamp, HttpRequest httpRequest) {
      this.timestamp = timestamp;
      this.httpRequest = httpRequest;
   }

   public HttpRequest getHttpRequest() {
      return httpRequest;
   }

   public Long getTimestamp() {
      return timestamp;
   }

   @Override
   public String toString() {
      return "HttpTimedRequest{" +
            "timestamp=" + timestamp +
            ", httpRequest=" + httpRequest.getRequestLine() +
            '}';
   }
}
