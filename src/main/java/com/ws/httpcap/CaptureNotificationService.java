package com.ws.httpcap;

import com.ws.httpcap.api.HttpConversation;
import com.ws.httpcap.model.http.HttpInteraction;

/**
 * Created by wschick on 4/18/17.
 */
public interface CaptureNotificationService {

   void notifyNewInteraction(int captureId, HttpConversation httpConversation);

}
