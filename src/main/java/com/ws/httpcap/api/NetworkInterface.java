package com.ws.httpcap.api;

/**
 * Created by wschick on 3/29/17.
 */
public class NetworkInterface {

   String name;
   String description;

   public String getName() {
      return name;
   }

   public String getDescription() {
      return description;
   }

   public void setName(String name) {
      this.name = name;
   }

   public void setDescription(String description) {
      this.description = description;
   }
}
