package com.ws.httpcap.model.http;

import org.apache.http.HttpResponse;

/**
 * Created by wschick on 3/22/17.
 */
public class HttpTimedResponse implements HttpTimedMessage {

   private final Long timestamp;
   private final HttpResponse httpResponse;

   public HttpTimedResponse(long timestamp, HttpResponse httpResponse) {
      this.timestamp = timestamp;
      this.httpResponse = httpResponse;
   }

   public HttpResponse getHttpResponse() {
      return httpResponse;
   }

   public Long getTimestamp() {
      return timestamp;
   }

   @Override
   public String toString() {
      return "HttpTimedResponse{" +
            "timestamp=" + timestamp +
            ", httpResponse=" + httpResponse.getStatusLine() +
            '}';
   }
}
