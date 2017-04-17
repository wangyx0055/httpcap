package com.ws.httpcap.model.http;

import com.ws.httpcap.Buffer;
import com.ws.httpcap.model.RequestHolder;
import com.ws.httpcap.model.ResponseHolder;
import com.ws.httpcap.model.tcp.TcpPacketWrapper;
import org.apache.http.HttpMessage;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.client.entity.GzipDecompressingEntity;
import org.apache.http.entity.BasicHttpEntity;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.io.ChunkedInputStream;
import org.apache.http.impl.io.DefaultHttpRequestParser;
import org.apache.http.impl.io.DefaultHttpResponseParser;
import org.apache.http.message.BasicHttpEntityEnclosingRequest;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by wschick on 3/22/17.
 */
public class HttpParser {

   public List<ResponseHolder> getServerResponses(List<TcpPacketWrapper> packets){
      List<ResponseHolder> result = new ArrayList<>();
      List<TcpPacketWrapper> consumedPackets = new ArrayList<>();
      List<TcpPacketWrapper> packetsToRemoce = new ArrayList<>();

      byte[] bytes = new byte[0];
      Long timestamp = null;

      for (TcpPacketWrapper tcpPacketWrapper : packets) {
         System.out.println("Handling chunked encoding");
         consumedPackets.add(tcpPacketWrapper);

         if (timestamp == null) {
            timestamp = tcpPacketWrapper.getTimestamp();
            //System.out.println("Seq:" + tcpPacketWrapper.getSequence());

         }

         bytes = combineArrays(bytes, tcpPacketWrapper.getContent());
         Buffer buffer = new Buffer(
               bytes
         );
         DefaultHttpResponseParser parser = new DefaultHttpResponseParser(
               buffer
         );

         try {

            HttpResponse parse = parser.parse();

            System.out.println(parse.getClass());

            if (parse.getHeaders("Transfer-Encoding").length != 0 && parse.getFirstHeader("Transfer-Encoding").getValue().equals("chunked")){

               System.out.println("Handling chunked encoding");
               ChunkedInputStream chunkedInputStream = new ChunkedInputStream(buffer);
               ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

               int b;

               while ((b = chunkedInputStream.read()) != -1){
                  byteArrayOutputStream.write(b);
               }

               parse.setEntity(new ByteArrayEntity(byteArrayOutputStream.toByteArray()));


            } else {

               StringEntity entity = extractContentLengthEntity(buffer, parse);

               if (entity == null) continue;

               parse.setEntity(entity);
            }

            if (parse.getHeaders("Content-Encoding").length != 0){
               if (parse.getFirstHeader("Content-Encoding").getValue().equals("gzip"))
                  parse.setEntity(new GzipDecompressingEntity(parse.getEntity()));
            }

            result.add(new ResponseHolder(timestamp,parse));
            packetsToRemoce.addAll(consumedPackets);


            bytes = new byte[Math.max(0,buffer.available())];

            //copy over any unread bytes
            if (bytes.length > 0) {
               buffer.read(bytes);
            }


            timestamp = null;
            consumedPackets.clear();

         } catch (Exception e) {
            System.out.println(e);
           // e.printStackTrace();
         }
      }

      packets.removeAll(packetsToRemoce);

      return result;
   }

   private StringEntity extractContentLengthEntity(Buffer buffer, HttpMessage parse) throws IOException {
      int contentLength = Integer.parseInt(parse.getFirstHeader("Content-Length").getValue());

      if (buffer.available() < contentLength) {
         return null;
      }

      byte[] contentBytes = new byte[contentLength];

      buffer.read(contentBytes);

      StringEntity entity = new StringEntity(new String(contentBytes));
      return entity;
   }

   public List<RequestHolder> getClientRequests(List<TcpPacketWrapper> packets){
      List<RequestHolder> result = new ArrayList<>();
      List<TcpPacketWrapper> consumedPackets = new ArrayList<>();
      List<TcpPacketWrapper> packetsToRemoce = new ArrayList<>();


      for (TcpPacketWrapper tcpPacketWrapper : packets){
         consumedPackets.add(tcpPacketWrapper);
         try {
            Buffer buffer = new Buffer(tcpPacketWrapper.getContent());

            DefaultHttpRequestParser parser = new DefaultHttpRequestParser(
                  buffer
            );

            HttpRequest parse = parser.parse();

            if (parse instanceof BasicHttpEntityEnclosingRequest){

               StringEntity entity = extractContentLengthEntity(buffer, parse);

               if (entity == null) continue;

               ((BasicHttpEntityEnclosingRequest)parse).setEntity(entity);
            }


            System.out.println(parse.getClass());


            RequestHolder requestHolder = new RequestHolder(tcpPacketWrapper.getTimestamp(), parse);

            packetsToRemoce.addAll(consumedPackets);
            consumedPackets.clear();

            result.add(requestHolder);
         }catch (Exception e){
            System.out.println(e);
            //throw new RuntimeException(e);
         }
      }

      packets.removeAll(packetsToRemoce);

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
