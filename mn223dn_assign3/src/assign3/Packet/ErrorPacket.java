package assign3.Packet;

import java.net.DatagramPacket;

public class ErrorPacket extends Packet {

	public enum ErrorType {
		NOT_DEFINED, FILE_NOT_FOUND, ACCESS_VIOLATION, DISK_FULL, ILLEGAL_TFTP_OP, UNKNOWN_TRANSFER_ID, FILE_ALREADY_EXIST
	};

	public String errorMsg;
	public int errorCode;

	public ErrorPacket() {
		super();
	}

	public ErrorPacket(String MSG, short errorCode, int packetCode , int bufSize) {
		super(bufSize , PacketType.ERROR , errorCode) ;
		this.errorMsg = MSG;
		this.errorCode = errorCode;
		super.buf.putShort((short) packetCode);
		super.buf.putShort(errorCode);
		super.buf.put(("\n" + MSG).getBytes());
		super.datagramPacket = new DatagramPacket(super.buf.array(), super.buf.array().length);
	}
}
