package com.ws.httpcap.api;

/**
 * Created by wschick on 3/29/17.
 */
public class HttpConversation {

   final String id;
   final String clientHost;
   final String serverHost;
   final int clientPort;
   final int serverPort;
   final HttpConversationRequest request;
   final HttpConversationResponse response;
   final long requestTimestamp;
   final long responseTimestamp;


   public HttpConversation(
         String id,
         HttpConversationRequest request,
         HttpConversationResponse response,
         String clientHost,
         String serverHost,
         int clientPort,
         int serverPort, long requestTimestamp, long responseTimestamp) {
      this.id = id;
      this.request = request;
      this.response = response;
      this.clientHost = clientHost;
      this.serverHost = serverHost;
      this.clientPort = clientPort;
      this.serverPort = serverPort;
      this.requestTimestamp = requestTimestamp;
      this.responseTimestamp = responseTimestamp;
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

   public int getClientPort() {
      return clientPort;
   }

   public int getServerPort() {
      return serverPort;
   }

   public String getClientHost() {
      return clientHost;
   }

   public String getServerHost() {
      return serverHost;
   }

   public long getRequestTimestamp() {
      return requestTimestamp;
   }

   public long getResponseTimestamp() {
      return responseTimestamp;
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
