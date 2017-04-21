package com.ws.httpcap.model.http;

import org.apache.http.HttpEntity;
import org.apache.http.HttpException;
import org.apache.http.HttpMessage;
import org.apache.http.HttpResponse;
import org.apache.http.client.entity.GzipDecompressingEntity;
import org.apache.http.impl.entity.EntityDeserializer;
import org.apache.http.impl.entity.StrictContentLengthStrategy;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by wschick on 4/19/17.
 */
public class HttpParserUtil {

   public HttpEntity extractHttpEntity(Buffer buffer, HttpMessage parse) throws HttpException, IOException {

      EntityDeserializer entityDeserializer = new EntityDeserializer(new StrictContentLengthStrategy());

      return entityDeserializer.deserialize(buffer, parse);
   }

   public HttpEntity decompressIfNeeded(HttpMessage parse, HttpEntity entity) {
      if (parse.getHeaders("Content-Encoding").length != 0){
         if (parse.getFirstHeader("Content-Encoding").getValue().equals("gzip"))
            return new GzipDecompressingEntity(entity);
      }

      return entity;
   }

   public void validateContent(HttpResponse parse) {
      if (parse.getEntity().getContentLength() == 0 &&
            ( parse.getHeaders("Content-Length").length > 0 || parse.getHeaders("Transfer-Encoding").length > 0) &&
            Integer.valueOf(parse.getFirstHeader("Content-Length").getValue()) != 0) {
         throw new RuntimeException("Invalid content");
      }
   }


   public byte[] readAll(InputStream inputStream) throws IOException{
      ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

      byte[] bytes = new byte[1];

      while (inputStream.read(bytes) != -1){
         byteArrayOutputStream.write(bytes[0]);
      }

      return byteArrayOutputStream.toByteArray();
   }
}
