package com.ws.httpcap.model.http;

import com.ws.httpcap.model.tcp.TcpPacketWrapper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * Created by wschick on 4/19/17.
 */
public class PacketParser {

   private static Logger logger = Logger.getLogger("HttpParser");

   public interface ParseFunction<T>{
      T parse(Buffer buffer, Long timestamp,TcpPacketWrapper lastPacket) throws Exception;
   }

   public  <T> List<T> parsePacketsToMessages(List<TcpPacketWrapper> packets, ParseFunction<T> parseFunction) {

      List<T> result = new ArrayList<>();
      List<TcpPacketWrapper> consumedPacketsForMessage = new ArrayList<>();
      List<TcpPacketWrapper> allConsumedPackets = new ArrayList<>();

      byte[] bytes = new byte[0];
      Long timestamp = null;

      for (TcpPacketWrapper tcpPacketWrapper : packets) {
         consumedPacketsForMessage.add(tcpPacketWrapper);

         if (timestamp == null) {
            timestamp = tcpPacketWrapper.getTimestamp();
         }

         bytes = concatenateArrays(bytes, tcpPacketWrapper.getContent());

         Buffer buffer = new Buffer(bytes);

         try {
            result.add(parseFunction.parse(buffer,timestamp,tcpPacketWrapper));
         } catch (Exception e) {
            logger.fine("Error parsing, waiting for more data. " + e);
            continue;
         }

         allConsumedPackets.addAll(consumedPacketsForMessage);
         consumedPacketsForMessage.clear();
         bytes = extractRemainingBytes(buffer);
         timestamp = null;
      }

      packets.removeAll(allConsumedPackets);

      return result;
   }

   private static byte[] extractRemainingBytes(Buffer buffer) {
      try {
         byte[] bytes;
         bytes = new byte[Math.max(0, buffer.available())];

         //copy over any unread bytes
         if (bytes.length > 0) {
            buffer.read(bytes);
         }
         return bytes;
      }catch (IOException e){
         throw new RuntimeException(e);
      }
   }

   private static byte[] concatenateArrays(byte[] a, byte[] b){
      int aLen = a.length;
      int bLen = b.length;
      byte[] c= new byte[aLen+bLen];
      System.arraycopy(a, 0, c, 0, aLen);
      System.arraycopy(b, 0, c, aLen, bLen);
      return c;
   }
}
