package capture.samples

import com.ws.httpcap.model.http.HttpMessageBuffer
import com.ws.httpcap.model.http.HttpParser
import com.ws.httpcap.model.tcp.TcpPacketWrapperImpl
import com.ws.httpcap.model.tcp.TcpStreamArray
import groovy.json.JsonSlurper
import org.apache.http.ProtocolVersion
import org.pcap4j.core.PcapHandle
import org.pcap4j.core.Pcaps
import org.pcap4j.packet.Packet
import spock.lang.Specification

/**
 * Created by wschick on 4/19/17.
 */
class SampleCaptureSpec extends Specification{


    public String prepareTestFile(String resource){
        new File("testfiles").mkdirs();

        new File("testfiles/testfile.pcap").delete()
        new File("testfiles/testfile.pcap") << getClass().getResourceAsStream(resource)

        return "testfiles/testfile.pcap"
    }

    void cleanup(){
        new File("testfiles").deleteDir()
    }

    public void slurpAllPacketsFromHandle(PcapHandle handle,TcpStreamArray streamArray){
        try {
            while (true) {
                Packet packet = handle.nextPacketEx

                if (!packet)
                    break;

                streamArray.addPacket(new TcpPacketWrapperImpl(packet, handle.timestamp))
            }
        }catch (EOFException e){}
    }

    void "it should parse the 304 responses"(){

        TcpStreamArray streamArray = new TcpStreamArray(3000);

        HttpMessageBuffer httpMessageBuffer = new HttpMessageBuffer(streamArray,new HttpParser());

        given:
        "some packets from the file"
        PcapHandle handle = Pcaps.openOffline(
                prepareTestFile("/captures/simple.pcap"),PcapHandle.TimestampPrecision.NANO
        )

        slurpAllPacketsFromHandle(handle,streamArray)

        when:
        "the http traffic is fetched"
        def result = httpMessageBuffer.httpInteractions

        then:
        "there should be 7 http interaction"
        result.size() == 7

        result*.httpTimedRequest.httpRequest.requestLine.method == ["GET","GET","GET","GET","GET","GET","GET"]
        result*.httpTimedRequest.httpRequest.requestLine.uri == [
                "/hike/1","/favicon.ico", "/hike/1","/favicon.ico", "/hike/1","/favicon.ico","/hike/1"
        ]
        result*.httpTimedRequest.httpRequest.requestLine.protocolVersion == [
                new ProtocolVersion("HTTP", 1, 1),new ProtocolVersion("HTTP", 1, 1),new ProtocolVersion("HTTP", 1, 1),
                new ProtocolVersion("HTTP", 1, 1),new ProtocolVersion("HTTP", 1, 1),new ProtocolVersion("HTTP", 1, 1),
                new ProtocolVersion("HTTP", 1, 1)
        ]
    }

    void "it should parse the simple json sample"(){

        TcpStreamArray streamArray = new TcpStreamArray(3000);

        HttpMessageBuffer httpMessageBuffer = new HttpMessageBuffer(streamArray,new HttpParser());

        given:
        "some packets from the file"
        PcapHandle handle = Pcaps.openOffline(
                prepareTestFile("/captures/one-request.pcap"),PcapHandle.TimestampPrecision.NANO
        )

        slurpAllPacketsFromHandle(handle,streamArray)

        when:
        "the http traffic is fetched"
        def result = httpMessageBuffer.httpInteractions

        then:
        "there should be one http interaction (and a favicon)"
        result.size() == 2

        result*.httpTimedRequest.httpRequest.requestLine.method == ["GET","GET"]
        result*.httpTimedRequest.httpRequest.requestLine.uri == [
                "/hike","/favicon.ico"
        ]
        result*.httpTimedRequest.httpRequest.requestLine.protocolVersion == [
                new ProtocolVersion("HTTP", 1, 1),new ProtocolVersion("HTTP", 1, 1)
        ]

        and:
        "the content json should be valid"
        def content = new JsonSlurper().parseText(
                result[0].httpTimedResponse.httpResponse.entity.content.text
        )

        content.size() == 3

    }

    void "it should parse post-body requests"(){

        TcpStreamArray streamArray = new TcpStreamArray(3000);

        HttpMessageBuffer httpMessageBuffer = new HttpMessageBuffer(streamArray,new HttpParser());

        given:
        "some packets from the file"
        PcapHandle handle = Pcaps.openOffline(
                prepareTestFile("/captures/post-body.pcap"),PcapHandle.TimestampPrecision.NANO
        )

        slurpAllPacketsFromHandle(handle,streamArray)

        when:
        "the http traffic is fetched"
        def result = httpMessageBuffer.httpInteractions

        then:
        "there should be one http interaction (and a favicon)"
        result.size() == 1

        result*.httpTimedRequest.httpRequest.requestLine.method == ["POST"]
        result*.httpTimedRequest.httpRequest.requestLine.uri == ["/hike"]
        result*.httpTimedRequest.httpRequest.requestLine.protocolVersion == [
                new ProtocolVersion("HTTP", 1, 1)
        ]

        and:
        "the request content should be valid json"
        def requestContent = new JsonSlurper().parseText(
                result[0].httpTimedRequest.httpRequest.entity.content.text
        )

        requestContent == [
                id:1,
                name:"test",
                persons:[],
                startDate:"2017-04-29T06:00:00.000Z",
                startLocation:[
                        latitude:39.9602803542957,
                        longitude:-105.8203125
                ]
        ]

        and:
        "the response content json should be valid"
        def content = new JsonSlurper().parseText(
                result[0].httpTimedResponse.httpResponse.entity.content.text
        )

        content == [
                id:30,
                name:"test",
                persons:[],
                startDate:"2017-04-29T06:00:00.000Z",
                startLocation:[
                        latitude:39.9602803542957,
                        longitude:-105.8203125
                ]
        ]

    }

    void "it should parse gzipped responses"(){

        TcpStreamArray streamArray = new TcpStreamArray(10004);

        HttpMessageBuffer httpMessageBuffer = new HttpMessageBuffer(streamArray,new HttpParser());

        given:
        "some packets from the file"
        PcapHandle handle = Pcaps.openOffline(
                prepareTestFile("/captures/gziped-response.pcap"),PcapHandle.TimestampPrecision.NANO
        )

        slurpAllPacketsFromHandle(handle,streamArray)

        when:
        "the http traffic is fetched"
        def result = httpMessageBuffer.httpInteractions

        then:
        "there should be one http interaction (and a favicon)"
        result.size() == 1

        result*.httpTimedRequest.httpRequest.requestLine.method == ["GET"]
        result*.httpTimedRequest.httpRequest.requestLine.uri == ["/resource/calsync/calendar/?r_accesskey=NKPFMWeC&r_username=ha_calsync_api"]
        result*.httpTimedRequest.httpRequest.requestLine.protocolVersion == [
                new ProtocolVersion("HTTP", 1, 1)
        ]


        and:
        "the response content json should be valid"
        def content = new JsonSlurper().parseText(
                result[0].httpTimedResponse.httpResponse.entity.content.text
        )

        content.size() == 12

    }



}
