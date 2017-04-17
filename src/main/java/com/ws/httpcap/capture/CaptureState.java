package com.ws.httpcap.capture;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Created by wschick on 4/17/17.
 */
public class CaptureState{
   CaptureProcessingTask processingThread;

   Collection<PacketCollectionTask> collectionThreads = new ArrayList<>();

   public void shutdown(){
      if (processingThread != null)
         processingThread.stopProcessing();

      for (PacketCollectionTask thread:collectionThreads)
         thread.stopCollection();
   }

   public void setProcessingThread(CaptureProcessingTask processingThread) {
      this.processingThread = processingThread;
   }

   public Collection<PacketCollectionTask> getCollectionThreads() {
      return collectionThreads;
   }
}