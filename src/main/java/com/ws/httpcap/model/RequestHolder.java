package com.ws.httpcap.model;

import org.apache.http.HttpRequest;

import java.sql.Timestamp;

/**
 * Created by wschick on 3/22/17.
 */
public class RequestHolder implements MessageHolder {

   private final Long sequence;
   private final HttpRequest httpRequest;

   public RequestHolder(long sequence, HttpRequest httpRequest) {
      this.sequence = sequence;
      this.httpRequest = httpRequest;
   }

   public HttpRequest getHttpRequest() {
      return httpRequest;
   }

   public Long getSequence() {
      return sequence;
   }

   @Override
   public String toString() {
      return "RequestHolder{" +
            "sequence=" + sequence +
            ", httpRequest=" + httpRequest.getRequestLine() +
            '}';
   }
}
