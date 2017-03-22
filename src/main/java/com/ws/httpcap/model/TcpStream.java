package com.ws.httpcap.model;

import com.ws.httpcap.Buffer;
import com.ws.httpcap.HttpPacket;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.io.DefaultHttpRequestParser;
import org.apache.http.impl.io.DefaultHttpResponseParser;

import java.io.ByteArrayOutputStream;
import java.net.InetAddress;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;
import java.util.stream.Collectors;

/**
 * Created by wschick on 3/22/17.
 */
public class TcpStream {

   final int srcPort;
   final int dstPort;

   final InetAddress srcAddr;
   final InetAddress dstAddr;

   TreeMap<Long,HttpPacket> incomingPackets = new TreeMap<>();
   TreeMap<Long,HttpPacket> outgoingPackets = new TreeMap<>();

   boolean closed = false;

   public TcpStream(int srcPort, int dstPort, InetAddress srcAddr, InetAddress dstAddr) {
      this.srcPort = srcPort;
      this.dstPort = dstPort;
      this.srcAddr = srcAddr;
      this.dstAddr = dstAddr;
   }

   public boolean belongsInStream(HttpPacket httpPacket){
      if (httpPacket.getSrcAddr().equals(srcAddr) && httpPacket.getSrcPort() == srcPort
            && httpPacket.getDstAddr().equals(dstAddr) && httpPacket.getDstPort() == dstPort)
         return true;

      if (httpPacket.getSrcAddr().equals(dstAddr) && httpPacket.getSrcPort() == dstPort
            && httpPacket.getDstAddr().equals(srcAddr) && httpPacket.getDstPort() == srcPort)
         return true;

      return false;
   }

   public List<HttpPacket> getClientStream(){
      return new ArrayList<>(incomingPackets.values());
   }

   public List<HttpPacket> getServerStream(){


      return new ArrayList<>(outgoingPackets.values()).stream().sorted((a,b)->new Long(a.getSquence()).compareTo( b.getSquence())).collect(Collectors.toList());
   }

   public void close(){
      closed = true;
   }

   public boolean isClosed() {
      return closed;
   }

   public List<RequestHolder> getClientRequests(){
      List<RequestHolder> result = new ArrayList<>();


      for (HttpPacket httpPacket: getClientStream()){
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

   public List<ResponseHolder> getServerResponses(){
      List<ResponseHolder> result = new ArrayList<>();

      byte[] bytes = new byte[0];
      Timestamp timestamp = null;

      for (HttpPacket httpPacket: getServerStream()) {

         if (timestamp == null)
            timestamp = httpPacket.getTimestamp();

         Buffer buffer = new Buffer(
               combineArrays(bytes, httpPacket.getContent())
         );
         DefaultHttpResponseParser parser = new DefaultHttpResponseParser(
               buffer
         );

         try {

            HttpResponse parse = parser.parse();

            int contentLength = Integer.parseInt(parse.getFirstHeader("Content-Length").getValue());


            if (buffer.available() < contentLength)
               throw new RuntimeException();

            InputStreamEntity entity=new InputStreamEntity(buffer,contentLength );
            entity.setContentType(parse.getFirstHeader("Content-Type"));
            entity.setContentEncoding(parse.getFirstHeader("Content-Encoding"));
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            entity.writeTo(output);

            parse.setEntity(new StringEntity(new String(output.toByteArray())));

            result.add(new ResponseHolder(timestamp,parse));

            bytes = new byte[Math.max(0,buffer.available())];
            timestamp = null;

            if (bytes.length > 0) {
               System.out.println("Copying: " + bytes.length);
               System.out.println(buffer.read(bytes));
            }
         } catch (Exception e) {
            bytes = combineArrays(bytes, httpPacket.getContent());
         }
      }

      return result;
   }


   public void addPacket(HttpPacket httpPacket){
      if (httpPacket.getSrcAddr().equals(srcAddr) && httpPacket.getSrcPort() == srcPort){
         System.out.println("client: " + httpPacket);
         incomingPackets.put(httpPacket.getSquence(),httpPacket);
      } else if (httpPacket.getDstAddr().equals(srcAddr) && httpPacket.getDstPort() == srcPort){
         System.out.println("server: " + httpPacket);
         outgoingPackets.put(httpPacket.getSquence(),httpPacket);
      } else
         throw new RuntimeException("Not a packet for this stream");
   }

   @Override
   public String toString() {
      return "TcpStream{" +
            "dstAddr=" + dstAddr +
            ", srcPort=" + srcPort +
            ", dstPort=" + dstPort +
            ", srcAddr=" + srcAddr +
            '}';
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
