/*
  UDPEchoClient.java
  A simple echo client with no error handling
*/

package assign1;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;

public class UDPEchoClient extends NetworkingLayer {

	public DatagramSocket socket;
	public DatagramPacket sendPacket;
	public DatagramPacket receivePacket;

	public UDPEchoClient(String[] args) {
		super(args);
	}

	public static void main(String[] args) throws IOException {

		UDPEchoClient UDPClient = new UDPEchoClient(args);
		UDPClient.initializing();
		UDPClient.startClient();

	}

	@Override
	protected void initializing() throws IOException {

		// create a socket
		socket = new DatagramSocket(null);
		// bind the socket with local bind point
		socket.bind(super.localBindPoint);
	}

	@Override
	protected void sendAndReceive() throws IOException {

		// create datagram packet for sending message
		sendPacket = new DatagramPacket(super.MSG.getBytes(), super.MSG.length(), super.remoteBindPoint);

		// create datagram packet for receiving message
		receivePacket = new DatagramPacket(super.buffer, super.buffer.length);

		// sending packet
		socket.send(sendPacket);

		// receiving packet
		socket.receive(receivePacket);

		// convert the received message into string
		super.receivedMSG = new String(receivePacket.getData(), receivePacket.getOffset(), receivePacket.getLength());
	}

	@Override
	protected void closeSocket() {
		socket.close();
	}

	@Override
	protected void checkMessage() {
		// check if the message is empty or bigger than UDP max packet size
		// which is 65507
		if (super.MSG.length() > 65507 || super.MSG.isEmpty()) {
			System.err.println("Error: Message is not vaild!");
			System.exit(1);
		}
	}

}