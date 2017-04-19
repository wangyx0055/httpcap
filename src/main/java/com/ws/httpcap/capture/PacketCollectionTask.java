package com.ws.httpcap.capture;

import org.pcap4j.core.PcapHandle;
import org.pcap4j.packet.Packet;

import java.sql.Timestamp;
import java.util.concurrent.BlockingQueue;
import java.util.logging.Logger;

/**
 * Created by wschick on 4/17/17.
 */
public class PacketCollectionTask extends Thread {

   private static Logger logger = Logger.getLogger("PacketCollectionTask");

   private final PcapHandle handle;
   private final BlockingQueue<TimedPacket> packetQueue;
   private volatile boolean running = true;

   public PacketCollectionTask(PcapHandle handle, BlockingQueue<TimedPacket> packetQueue) {
      this.handle = handle;
      this.packetQueue = packetQueue;
   }


   @Override
   public void run() {

      logger.info("Starting collection: " + handle);

      while (running) {
         try {
            Packet packet = handle.getNextPacketEx();
            Timestamp timestamp = handle.getTimestamp();

            packetQueue.add(new TimedPacket(packet, timestamp));
         } catch (Exception e) {
            logger.fine("Error collecting packets: " + e);
         }
      }

      handle.close();

      logger.info("Stopping collection: " + handle);
   }

   public void stopCollection(){
      this.running = false;
      this.interrupt();
   }

   public PacketCollectionTask startCollection(){
      this.start();
      return this;
   }
}
