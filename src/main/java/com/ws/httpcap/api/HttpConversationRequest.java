package com.ws.httpcap.api;

import java.util.Collection;

/**
 * Created by wschick on 3/29/17.
 */
public class HttpConversationRequest {

   final String method;
   final String path;
   final Collection<NameValuePair> headers;
   final Collection<NameValuePair> query;


   public HttpConversationRequest(String method, String path, Collection<NameValuePair> query,Collection<NameValuePair> headers) {
      this.method = method;
      this.path = path;
      this.query = query;
      this.headers = headers;
   }

   public Collection<NameValuePair> getHeaders() {
      return headers;
   }

   public String getMethod() {
      return method;
   }

   public String getPath() {
      return path;
   }

   public Collection<NameValuePair> getQuery() {
      return query;
   }
}
