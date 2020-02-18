package assign3.Packet;

import java.net.DatagramPacket;
import java.nio.ByteBuffer;

public abstract class Packet {

	public enum PacketType {
		SEND, RECEIVE, DATA, ACK, ERROR
	};

	public PacketType packetType;
	public int packetCode;
	public ByteBuffer buf ;
	public DatagramPacket datagramPacket;
	public int block;
	public int receivedByteNum;

	public Packet() {
	}

	public Packet(int bufSize, PacketType packetType, int block) {
		byte[] bufArr = new byte[bufSize];
		buf = ByteBuffer.wrap(bufArr);
		this.packetType = packetType ;
		this.packetCode = this.packetType.ordinal() + 1;
		this.block = block;
	}

	// Analysis the received packet
	public void receivedDataAnalysis() {
		this.receivedByteNum = datagramPacket.getLength();
		this.packetCode = this.buf.getShort(0);
		this.block = this.buf.getShort(2);
		switch (packetCode) {
		case 1:
			this.packetType = PacketType.SEND;
			break;
		case 2:
			this.packetType = PacketType.RECEIVE;
			break;
		case 3:
			this.packetType = PacketType.DATA;
			break;
		case 4:
			this.packetType = PacketType.ACK;
			break;
		case 5:
			this.packetType = PacketType.ERROR;
			break;
		}

	}
}
