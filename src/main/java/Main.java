import com.ws.httpcap.Buffer;
import com.ws.httpcap.HttpPacket;
import com.ws.httpcap.model.*;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.impl.io.DefaultHttpRequestParser;
import org.apache.http.impl.io.DefaultHttpResponseParser;
import org.pcap4j.core.PcapHandle;
import org.pcap4j.core.PcapNativeException;
import org.pcap4j.core.Pcaps;
import org.pcap4j.packet.Packet;

import java.io.EOFException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.TreeMap;
import java.util.concurrent.TimeoutException;

/**
 * Created by wschick on 11/17/16.
 */
public class Main {

   public static void main(String[] args) throws Exception{
      byte[] bytes = new byte[0];

      PcapHandle handle;
      try {
         handle = Pcaps.openOffline("capture.out", PcapHandle.TimestampPrecision.NANO);
      } catch (PcapNativeException e) {
         handle = Pcaps.openOffline("out.pcap");
      }

      TcpStreamArray streamArray = new TcpStreamArray(8080);

      for (int i = 0; i < 4000; i++) {
         try {
            Packet packet = handle.getNextPacketEx();
            System.out.println(handle.getTimestamp().getNanos());

            HttpPacket httpPacket = new HttpPacket(packet,handle.getTimestamp());

            streamArray.addPacket(httpPacket);




         } catch (TimeoutException e) {
         } catch (EOFException e) {
            System.out.println("EOF");
            break;
         }
      }

      streamArray.getInteractions().forEach(System.out::println);

      handle.close();
   }


}
