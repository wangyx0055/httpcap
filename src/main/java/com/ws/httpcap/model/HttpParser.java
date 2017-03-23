package com.ws.httpcap.model;

import com.ws.httpcap.Buffer;
import com.ws.httpcap.HttpPacket;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.io.DefaultHttpRequestParser;
import org.apache.http.impl.io.DefaultHttpResponseParser;

import java.io.ByteArrayOutputStream;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by wschick on 3/22/17.
 */
public class HttpParser {

   public List<ResponseHolder> getServerResponses(List<HttpPacket> packets){
      List<ResponseHolder> result = new ArrayList<>();

      byte[] bytes = new byte[0];
      Timestamp timestamp = null;

      for (HttpPacket httpPacket: packets) {

         if (timestamp == null) {
            timestamp = httpPacket.getTimestamp();
            //System.out.println("Seq:" + httpPacket.getSquence());

         }

         bytes = combineArrays(bytes, httpPacket.getContent());
         Buffer buffer = new Buffer(
               bytes
         );
         DefaultHttpResponseParser parser = new DefaultHttpResponseParser(
               buffer
         );

         try {

            HttpResponse parse = parser.parse();

            int contentLength = Integer.parseInt(parse.getFirstHeader("Content-Length").getValue());

            //System.out.println("Content length: " + contentLength);

            if (buffer.available() < contentLength) {

               //for (Header header:parse.getAllHeaders())
                  //System.out.println(header);
               //System.out.println("have:" + buffer.available());
               continue;
            }

            //System.out.println("have:" + buffer.available());

            byte[] contentBytes = new byte[contentLength];

            buffer.read(contentBytes);

            parse.setEntity(new StringEntity(new String(contentBytes)));

            //System.out.println(new String(contentBytes));

            result.add(new ResponseHolder(timestamp,parse));

            bytes = new byte[Math.max(0,buffer.available())];
            timestamp = null;

            if (bytes.length > 0) {
               System.out.println("Copying: " + bytes.length);
               System.out.println(buffer.read(bytes));
               //System.out.println(new String(bytes));
            }


         } catch (Exception e) {
            e.printStackTrace();
         }
      }

      return result;
   }

   public List<RequestHolder> getClientRequests(List<HttpPacket> packets){
      List<RequestHolder> result = new ArrayList<>();


      for (HttpPacket httpPacket: packets){
         try {
            DefaultHttpRequestParser parser = new DefaultHttpRequestParser(
                  new Buffer(httpPacket.getContent())
            );

            result.add(new RequestHolder(httpPacket.getTimestamp(),parser.parse()));
         }catch (Exception e){
            //throw new RuntimeException(e);
         }
      }

      return result;

   }

   public static byte[] combineArrays(byte[] a, byte[] b){
      int aLen = a.length;
      int bLen = b.length;
      byte[] c= new byte[aLen+bLen];
      System.arraycopy(a, 0, c, 0, aLen);
      System.arraycopy(b, 0, c, aLen, bLen);
      return c;
   }
}
