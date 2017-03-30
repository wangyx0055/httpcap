package com.ws.httpcap.model;

import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;

import java.sql.Timestamp;

/**
 * Created by wschick on 3/22/17.
 */
public class ResponseHolder implements MessageHolder {

   private final Long sequence;
   private final HttpResponse httpResponse;

   public ResponseHolder(long sequence, HttpResponse httpResponse) {
      this.sequence = sequence;
      this.httpResponse = httpResponse;
   }

   public HttpResponse getHttpResponse() {
      return httpResponse;
   }

   public Long getSequence() {
      return sequence;
   }

   @Override
   public String toString() {
      return "ResponseHolder{" +
            "sequence=" + sequence +
            ", httpResponse=" + httpResponse.getStatusLine() +
            '}';
   }
}
