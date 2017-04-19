package com.ws.httpcap;

import com.ws.httpcap.api.HttpConversation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.stereotype.Component;

/**
 * Created by wschick on 4/18/17.
 */
@Component
public class CaptureNotificationServiceImpl implements CaptureNotificationService {

   @Autowired
   SimpMessageSendingOperations simpMessageSendingOperations;

   @Override
   public void notifyNewInteraction(int captureId, HttpConversation httpConversation) {
      simpMessageSendingOperations.convertAndSend("/capture/" + captureId,httpConversation);
   }
}
