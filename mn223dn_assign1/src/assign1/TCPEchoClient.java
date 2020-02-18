package assign1;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class TCPEchoClient extends NetworkingLayer {

	private Socket socket;
	private DataInputStream inputStream;
	private DataOutputStream outputStream;

	public TCPEchoClient(String[] args) {
		super(args);
	}

	public static void main(String[] args) throws IOException {

		TCPEchoClient TCPClient = new TCPEchoClient(args);
		TCPClient.initializing();
		TCPClient.startClient();

	}

	@Override
	protected void initializing() throws IOException {
		// create socket
		socket = new Socket();
		// bind socket with local point and connect socket with remote bind points
		socket.bind(super.localBindPoint);
		socket.connect(super.remoteBindPoint);

		// create input and output streams for sending and receiving messages
		outputStream = new DataOutputStream(socket.getOutputStream());
		inputStream = new DataInputStream(socket.getInputStream());
	}

	@Override
	protected void sendAndReceive() throws IOException {

		// send message to the server
		outputStream.write(super.MSG.getBytes());

		StringBuilder strBuilder = new StringBuilder();

		// repeat until reading the whole message
		do {
			// read message and save it in the buffer and get the number of read bytes
			int readBytes = inputStream.read(super.buffer);
			super.receivedMSG = new String(super.buffer, 0, readBytes);

			// append the received part of the sent message to string builder for later send
			// in case of the buffer size is smaller than the sent message
			strBuilder.append(super.receivedMSG);
		} while (inputStream.available() > 0);

		super.receivedMSG = strBuilder.toString();

	}

	@Override
	protected void closeSocket() {
		try {
			socket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	protected void checkMessage() {
		// check if the message is empty
		if (super.MSG.isEmpty()) {
			System.err.println("Error: Message is not vaild!");
			System.exit(1);
		}
	}

}
