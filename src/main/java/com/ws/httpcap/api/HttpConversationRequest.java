package com.ws.httpcap.api;

import java.util.Collection;

/**
 * Created by wschick on 3/29/17.
 */
public class HttpConversationRequest {

   final String method;
   final String path;
   final String body;
   final Collection<NameValuePair> headers;
   final Collection<NameValuePair> query;


   public HttpConversationRequest(String method, String path, Collection<NameValuePair> query,Collection<NameValuePair> headers, String body) {
      this.method = method;
      this.path = path;
      this.query = query;
      this.headers = headers;
      this.body = body;
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

   public String getBody() {
      return body;
   }

   public Collection<NameValuePair> getQuery() {
      return query;
   }
}
