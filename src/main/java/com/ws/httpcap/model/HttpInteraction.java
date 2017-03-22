package com.ws.httpcap.model;

/**
 * Created by wschick on 3/22/17.
 */
public class HttpInteraction {

   final RequestHolder requestHolder;
   final ResponseHolder responseHolder;


   public HttpInteraction(RequestHolder requestHolder, ResponseHolder responseHolder) {
      this.requestHolder = requestHolder;
      this.responseHolder = responseHolder;
   }

   public RequestHolder getRequestHolder() {
      return requestHolder;
   }

   public ResponseHolder getResponseHolder() {
      return responseHolder;
   }

   @Override
   public String toString() {
      return "HttpInteraction{" +
            "requestHolder=" + requestHolder +
            ", responseHolder=" + responseHolder +
            '}';
   }
}
