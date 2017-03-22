package com.ws.httpcap;

import org.apache.http.io.HttpTransportMetrics;
import org.apache.http.io.SessionInputBuffer;
import org.apache.http.util.CharArrayBuffer;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by wschick on 11/17/16.
 */
public class Buffer extends ByteArrayInputStream implements SessionInputBuffer {


   public Buffer(byte[] b){
      super(b);
   }

   @Override
   public int readLine(CharArrayBuffer buffer) throws IOException {

      String s = readLine();

      if (s == null)
         return -1;

      buffer.append(s);

      return s.length();
   }

   @Override
   public String readLine() throws IOException {
      ArrayList<Byte> bytes = new ArrayList<>();

      int i = -1;

      while ((i = read()) != -1){
         bytes.add((byte)i);

         if (asString(bytes).endsWith("\r\n")){
            String s = asString(bytes);
            return s.substring(0, s.length()-2);
         }
      }

      return null;
   }

   public static String asString(List<Byte> byteList){
      byte[] b = new byte[byteList.size()];

      for (int j = 0; j < b.length; j++)
         b[j] = byteList.get(j);

      return new String(b);
   }

   @Override
   public boolean isDataAvailable(int timeout) throws IOException {
      return available() > 0;
   }

   @Override
   public HttpTransportMetrics getMetrics() {
      return null;
   }
}
