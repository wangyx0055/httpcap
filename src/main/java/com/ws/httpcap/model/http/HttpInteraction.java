package com.ws.httpcap.model.http;

/**
 * Created by wschick on 3/22/17.
 */
public class HttpInteraction {

   final HttpTimedRequest httpTimedRequest;
   final HttpTimedResponse httpTimedResponse;
   final String id;


   public HttpInteraction(String id, HttpTimedRequest httpTimedRequest, HttpTimedResponse httpTimedResponse) {
      this.id = id;
      this.httpTimedRequest = httpTimedRequest;
      this.httpTimedResponse = httpTimedResponse;
   }

   public HttpTimedRequest getHttpTimedRequest() {
      return httpTimedRequest;
   }

   public HttpTimedResponse getHttpTimedResponse() {
      return httpTimedResponse;
   }

   public String getId() {
      return id;
   }

   @Override
   public String toString() {
      return "HttpInteraction{" +
            "httpTimedRequest=" + httpTimedRequest +
            ", httpTimedResponse=" + httpTimedResponse +
            '}';
   }
}
