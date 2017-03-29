package com.ws.httpcap.api;

/**
 * Created by wschick on 3/29/17.
 */
public class NameValuePair {

   final String name;
   final String value;


   public NameValuePair(String name, String value) {
      this.name = name;
      this.value = value;
   }

   public String getName() {
      return name;
   }

   public String getValue() {
      return value;
   }
}
