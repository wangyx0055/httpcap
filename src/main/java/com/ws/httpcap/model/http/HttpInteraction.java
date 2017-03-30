package com.ws.httpcap.model.http;

import com.ws.httpcap.model.RequestHolder;
import com.ws.httpcap.model.ResponseHolder;

/**
 * Created by wschick on 3/22/17.
 */
public class HttpInteraction {

   final RequestHolder requestHolder;
   final ResponseHolder responseHolder;
   final String id;


   public HttpInteraction(String id,RequestHolder requestHolder, ResponseHolder responseHolder) {
      this.id = id;
      this.requestHolder = requestHolder;
      this.responseHolder = responseHolder;
   }

   public RequestHolder getRequestHolder() {
      return requestHolder;
   }

   public ResponseHolder getResponseHolder() {
      return responseHolder;
   }

   public String getId() {
      return id;
   }

   @Override
   public String toString() {
      return "HttpInteraction{" +
            "requestHolder=" + requestHolder +
            ", responseHolder=" + responseHolder +
            '}';
   }
}
