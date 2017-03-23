package com.ws.httpcap.model;

/**
 * Created by wschick on 3/22/17.
 */
public class TcpConnection {

   TcpStream clientInputStream;
   final TcpStream serverInputStream;

   public TcpConnection(TcpStream serverInputStream) {
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

}


