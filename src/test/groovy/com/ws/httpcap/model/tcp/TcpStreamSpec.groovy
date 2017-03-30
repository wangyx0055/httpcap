package com.ws.httpcap.model.tcp

import spock.lang.Specification

/**
 * Created by wschick on 3/30/17.
 */
class TcpStreamSpec extends Specification {

    void "it should accept packets and provide them in a reordered stream"(){
        given:
        "some packets"
        MockTcpPacketWrapper packet1 = new MockTcpPacketWrapper(
                sequence: 1000,
                content: "abcd".bytes
        )
        MockTcpPacketWrapper packet2 = new MockTcpPacketWrapper(
                sequence: 1004,
                content: "efgh".bytes
        )

        and:
        "a tcp stream"
        TcpStream stream = new TcpStream(0,0,null,null,999)

        when:
        "packets are added out of order"
        stream.addPacket(packet2)
        stream.addPacket(packet1)

        then:
        "the should be read back in order"
        stream.drawFromStream() == [
                packet1,packet2
        ]

        and:
        "removed from the stream"
        stream.drawFromStream() == []
    }

    void "it should not return non-contiguous packets"(){
        given:
        "some packets"
        MockTcpPacketWrapper packet1 = new MockTcpPacketWrapper(
                sequence: 1000,
                content: "abcd".bytes
        )
        MockTcpPacketWrapper packet2 = new MockTcpPacketWrapper(
                sequence: 1004,
                content: "efgh".bytes
        )
        MockTcpPacketWrapper packet3 = new MockTcpPacketWrapper(
                sequence: 1008,
                content: "ijkl".bytes
        )

        and:
        "a tcp stream"
        TcpStream stream = new TcpStream(0,0,null,null,999)

        when:
        "packets are added with a missing packet"
        stream.addPacket(packet3)
        stream.addPacket(packet1)

        then:
        "only contiguous packets should be read back"
        stream.drawFromStream() == [
                packet1
        ]

        and:
        "removed from the stream"
        stream.drawFromStream() == []

        when:
        "the missing packet is added"
        stream.addPacket(packet2)

        then:
        "the rest should be available"
        stream.drawFromStream() == [
                packet2,packet3
        ]

        and:
        "removed"
        stream.drawFromStream() == []
    }
}
