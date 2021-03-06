package uk.ac.gla.atanaspam.pcapj;

import java.net.InetAddress;
import java.util.Arrays;

/**
 * This class represents a TCP packet and stores all the data that we are interested in for a TCP packet
 * @author atanaspam
 * @version 0.4
 */
public class TCPPacket extends IPPacket{

    protected int src_port;
    protected int dst_port;
    protected long seqNum;
    protected long ackNum;
    protected TCPFlags flags;

    protected PacketContents data;

    public int getSrc_port() {
        return src_port;
    }

    public int getDst_port() {
        return dst_port;
    }

    public long getSeqNum() {
        return seqNum;
    }

    public long getAckNum() {
        return ackNum;
    }

    public TCPFlags getFlags() {
        return flags;
    }

    public PacketContents getData() {
        return data;
    }

    public void setSrc_port(int src_port) {
        this.src_port = src_port;
    }

    public void setDst_port(int dst_port) {
        this.dst_port = dst_port;
    }

    public void setFlags(TCPFlags flags) {
        this.flags = flags;
    }

    public void setData(PacketContents data) {
        this.data = data;
    }

    /**
     * @deprecated
     * This constructor takes an IPPacket object(superclass) and constructs a TCP
     * instance of it copying all the data carried by the superclass.
     * @param packet An already generated packet
     */
    public TCPPacket(IPPacket packet){
	    super(packet.timestamp);
	
	    this.src_ip = packet.src_ip;
	    this.dst_ip = packet.dst_ip;
        this.sourceMacAddress = packet.sourceMacAddress;
        this.destMacAddress = packet.destMacAddress;
    }


    /**
     * This constructor creates a TCPPacket instance from its raw representation
     * @param packet a byte[] that stores the raw packet (already determined to be TCP)
     * @param timestamp the time the packet was captured
     */
    public TCPPacket(byte[] packet, long timestamp){
        super(packet, timestamp);

        final int inTCPHeaderSrcPortOffset = 0;
        final int inTCPHeaderDstPortOffset = 2;
        final int inTCPHeaderSeqNumOffset = 4;
        final int inTCPHeaderAckNumOffset = 8;
        final int flagsOffset = 13;

        int ipHeaderLenght = Utils.getIPHeaderLength(packet);

        int srcPortOffset = Utils.calculateTCPoffset(ipHeaderLenght, inTCPHeaderSrcPortOffset);
        this.src_port = Utils.convertShort(packet, srcPortOffset);

        int dstPortOffset = Utils.calculateTCPoffset(ipHeaderLenght, inTCPHeaderDstPortOffset);
        this.dst_port = Utils.convertShort(packet, dstPortOffset);

        int seqNumOffset = Utils.calculateTCPoffset(ipHeaderLenght, inTCPHeaderSeqNumOffset);
        this.seqNum = Utils.convertLong(packet, seqNumOffset);

        int ackNumOffset = Utils.calculateTCPoffset(ipHeaderLenght, inTCPHeaderAckNumOffset);
        this.ackNum = Utils.convertLong(packet, ackNumOffset);

        byte rawFlags = packet[Utils.calculateTCPoffset(ipHeaderLenght, flagsOffset)];

        this.flags = new TCPFlags(Utils.isSet(rawFlags, 0), Utils.isSet(rawFlags, 1), Utils.isSet(rawFlags, 2),
                Utils.isSet(rawFlags, 3), Utils.isSet(rawFlags, 4), Utils.isSet(rawFlags, 5), Utils.isSet(rawFlags, 6),
                Utils.isSet(rawFlags, 7));



        int payloadDataStart =  Utils.etherHeaderLength +
                Utils.getIPHeaderLength(packet) + Utils.getTCPHeaderLength(packet);
        byte[] data = new byte[0];
        if((packet.length - payloadDataStart) > 0){
            data = new byte[packet.length - payloadDataStart];
            System.arraycopy(packet, payloadDataStart, data, 0, data.length);
        }
        try {
            this.data = new PacketContents(data);
        }catch (NullPointerException e){
            this.data = null;
        }
    }

    /**
     * This constructor is only used by an external program using my library and can be safely removed.
     */
    public TCPPacket(long timestamp, String srcMAC, String destMAC, InetAddress srcIP, InetAddress dstIP, int srcPort,
                    int destPort, TCPFlags flags, PacketContents data){

        super(timestamp,srcMAC, destMAC,srcIP, dstIP);
        this.src_port = srcPort;
        this.dst_port = destPort;
        this.flags = flags;
        this.data = data;
    }

    @Override
    public String toString(){
       return String.format(
                /*
                "-----TCP PACKET-----%nTimeStamp: %d%nSRC MAC: %s%nDST MAC: %s%nSRC IP: %s%nDEST IP: %s%nSRC PORT: %d%n" +
                        "DEST PORT: %d%nSEQ NUM: %d%nACK NUM: %d%nPAYLOAD LEN: %d%nFLAGS: %s%n",
               */
               "-----TCP PACKET-----%nTimeStamp: %d%nSRC MAC: %s%nDST MAC: %s%nSRC IP: %s%nDEST IP: %s%nSRC PORT: %d%n" +
                       "DEST PORT: %d%nFLAGS: %s%nCONTENTS: %s%n",

                this.timestamp/1000, this.sourceMacAddress, this.destMacAddress, this.src_ip.getHostAddress(),
               this.dst_ip.getHostAddress(), this.src_port, this.dst_port/*, this.seqNum, this.ackNum,
               this.data.getData().length*/, flags, data.toString());
    }


}
