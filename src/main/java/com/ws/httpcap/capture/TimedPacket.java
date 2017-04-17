package com.ws.httpcap.capture;

import org.pcap4j.packet.Packet;

import java.sql.Timestamp;

/**
 * Created by wschick on 4/17/17.
 */
public class TimedPacket{
   public final Packet packet;
   public final Timestamp timestamp;


   public TimedPacket(Packet packet, Timestamp timestamp) {
      this.packet = packet;
      this.timestamp = timestamp;
   }

   @Override
   public String toString() {
      return "TimedPacket{" +
            //"packet=" + packet +
            ", timestamp=" + timestamp +
            '}';
   }
}
