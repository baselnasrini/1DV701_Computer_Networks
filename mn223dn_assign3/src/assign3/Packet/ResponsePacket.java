package assign3.Packet;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.nio.ByteBuffer;
import java.util.Arrays;

public class ResponsePacket extends Packet {

	public File file;
	public byte[] bytebuf;
	public int bufSize;
	public FileInputStream inStream;
	public FileOutputStream outStream;

	public ResponsePacket() {
	}

	public ResponsePacket(int bufSize, String fileName, int block, int clientPacketCode) throws FileNotFoundException {
		super(bufSize, PacketType.DATA, block);
		file = new File(fileName);
		this.bufSize = bufSize;

		if (clientPacketCode != 1) {
			bytebuf = new byte[this.bufSize];
			super.datagramPacket = new DatagramPacket(super.buf.array(), super.buf.array().length);
			outStream = new FileOutputStream(this.file);
		} else {
			bytebuf = new byte[this.bufSize - 4];
			inStream = new FileInputStream(this.file);
		}
	}

	// but the next part of the file in the buffer to send it to the client
	public void getNext() throws IOException {
		byte[] bufArr = new byte[super.buf.capacity()];
		buf = ByteBuffer.wrap(bufArr);
		buf.putShort((short) super.packetCode);
		buf.putShort((short) super.block++);
		int receivedByteNum = inStream.read(this.bytebuf) ;
		buf.put(this.bytebuf);
		super.datagramPacket = new DatagramPacket(super.buf.array(), receivedByteNum + 4);

	}

	// Check if all parts of the required file have sent to the client
	public boolean hasNext() throws IOException {
		return inStream.available() > 1;
	}

	// Check if all parts of the file have received from the client
	public boolean hasClientNext() {
		return super.receivedByteNum == this.bufSize ;
	}
	
	public void saveFile () throws IOException {
		outStream.write(Arrays.copyOfRange(super.datagramPacket.getData(), 4, super.datagramPacket.getLength()));
		outStream.flush();
		
		if (! hasClientNext()) {
			outStream.close();
		}
	}
}
