package com.ws.httpcap.api;

import java.util.Collection;

/**
 * Created by wschick on 3/29/17.
 */
public class HttpConversationResponse {

   final int status;
   final String body;
   final Collection<NameValuePair> headers;


   public HttpConversationResponse(int statuc, Collection<NameValuePair> headers, String body) {
      this.status = statuc;
      this.body = body;
      this.headers = headers;
   }

   public Collection<NameValuePair> getHeaders() {
      return headers;
   }

   public int getStatus() {
      return status;
   }

   public String getBody() {
      return body;
   }
}
