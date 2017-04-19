package com.ws.httpcap.api;

/**
 * Created by wschick on 3/29/17.
 */
public class HttpConversation {

   final String id;
   final HttpConversationRequest request;
   final HttpConversationResponse response;


   public HttpConversation(String id,HttpConversationRequest request, HttpConversationResponse response) {
      this.id = id;
      this.request = request;
      this.response = response;
   }

   public HttpConversationRequest getRequest() {
      return request;
   }

   public HttpConversationResponse getResponse() {
      return response;
   }

   public String getId() {
      return id;
   }

   @Override
   public String toString() {
      return "HttpConversation{" +
            "id='" + id + '\'' +
            ", request=" + request.getMethod() + " " + request.getPath() +
            ", response=" + response.getStatus() +
            '}';
   }
}
