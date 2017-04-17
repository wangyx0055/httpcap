package com.ws.httpcap;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;

/**
 * Created by wschick on 11/17/16.
 */
@SpringBootApplication
@Import(WebSocketConfig.class)
public class Main {

   public static void main(String[] args) throws Exception{
      SpringApplication.run(Main.class);
   }


}
