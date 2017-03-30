package com.ws.httpcap.model.tcp;

/**
 * Created by wschick on 3/22/17.
 */
public class TcpConnection {

   TcpStream clientInputStream;
   final TcpStream serverInputStream;
   final int id;

   public TcpConnection(int id,TcpStream serverInputStream) {
      this.id = id;
      this.serverInputStream = serverInputStream;
   }


   public TcpStream getClientInputStream() {
      return clientInputStream;
   }

   public TcpStream getServerInputStream() {
      return serverInputStream;
   }

   public void setClientInputStream(TcpStream clientInputStream) {
      this.clientInputStream = clientInputStream;
   }

   public int getId() {
      return id;
   }
}


