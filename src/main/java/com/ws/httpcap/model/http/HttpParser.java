package com.ws.httpcap.model.http;

import com.ws.httpcap.Buffer;
import com.ws.httpcap.model.RequestHolder;
import com.ws.httpcap.model.ResponseHolder;
import com.ws.httpcap.model.tcp.TcpPacketWrapper;
import org.apache.http.HttpResponse;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.io.ChunkedInputStream;
import org.apache.http.impl.io.DefaultHttpRequestParser;
import org.apache.http.impl.io.DefaultHttpResponseParser;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
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

            if (parse.getHeaders("Transfer-Encoding").length != 0 && parse.getFirstHeader("Transfer-Encoding").getValue().equals("chunked")){

               ChunkedInputStream chunkedInputStream = new ChunkedInputStream(buffer);
               ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

               int b = -1;

               while ((b = chunkedInputStream.read()) != -1){
                  byteArrayOutputStream.write(b);
               }

               parse.setEntity(new StringEntity(new String(byteArrayOutputStream.toByteArray())));


            } else {



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
            }

            //System.out.println(new String(contentBytes));

            result.add(new ResponseHolder(timestamp,parse));
            packetsToRemoce.addAll(consumedPackets);


            bytes = new byte[Math.max(0,buffer.available())];
            timestamp = null;
            consumedPackets.clear();

            if (bytes.length > 0) {
               System.out.println("Copying: " + bytes.length);
               System.out.println(buffer.read(bytes));
               //System.out.println(new String(bytes));
            }


         } catch (Exception e) {
           // e.printStackTrace();
         }
      }

      packets.removeAll(packetsToRemoce);

      return result;
   }

   public List<RequestHolder> getClientRequests(List<TcpPacketWrapper> packets){
      List<RequestHolder> result = new ArrayList<>();


      for (TcpPacketWrapper tcpPacketWrapper : packets){
         try {
            DefaultHttpRequestParser parser = new DefaultHttpRequestParser(
                  new Buffer(tcpPacketWrapper.getContent())
            );

            result.add(new RequestHolder(tcpPacketWrapper.getTimestamp(),parser.parse()));
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
