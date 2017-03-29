package com.ws.httpcap.api;

import com.ws.httpcap.PacketCaptureService;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.client.utils.URLEncodedUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by wschick on 10/28/16.
 */
@RestController
public class PacketCaptureController {

   @Autowired
   PacketCaptureService packetCaptureService;


   @RequestMapping(path = "/capture", method = RequestMethod.GET)
   Collection<PacketCapture> captureList(){
      return packetCaptureService.getCaptures();
   }

   @RequestMapping(path = "/capture", method = RequestMethod.POST)
   int captureList(@RequestBody PacketCapture packetCapture){

      System.out.println(packetCapture.getPort());
      System.out.println(packetCapture.getInterfaces());
      return packetCaptureService.startCapture(packetCapture);
   }

   @RequestMapping(path = "/capture/{captureId}", method = RequestMethod.GET)
   PacketCapture getPacketCapture(@PathVariable("captureId") int captureId) throws Exception {

      return packetCaptureService.getCapture(captureId);

   }

   @RequestMapping(path = "/interface")
   List<NetworkInterface> listInterfaces(){
      return packetCaptureService.getNetworkInterfaceNames();
   }

   public static String read(HttpEntity input)  {

      try (BufferedReader buffer = new BufferedReader(new InputStreamReader(input.getContent()))) {
         return buffer.lines().collect(Collectors.joining("\n"));
      }catch (IOException e){
         throw new RuntimeException(e);
      }
   }

}
