package assign3.Packet;

import java.net.DatagramPacket;

public class ACKPacket extends Packet{
	
	public ACKPacket() {
	}

	public ACKPacket(int block) {
		super(4 , PacketType.ACK , block) ;
		this.block = block ;
		super.buf.putShort((short) 4);
		super.buf.putShort((short) this.block);
		super.datagramPacket = new DatagramPacket(super.buf.array(), super.buf.array().length);
	}
	
}
