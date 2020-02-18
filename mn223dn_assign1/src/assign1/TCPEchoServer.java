package assign1;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class TCPEchoServer {

	public static final int MYPORT = 4950;
	public static int threadId = 0;

	public static void main(String[] args) throws IOException {

		// create server socket
		@SuppressWarnings("resource")
		ServerSocket serverSocket = new ServerSocket(MYPORT);

		// forever loop for the server until manual termination
		while (true) {
			// wait for a connection to this server socket and accept it
			Socket socket = serverSocket.accept();

			// create a client thread
			TCPClientThread client = new TCPClientThread(socket, threadId++);

			// run the thread
			client.run();

		}
	}
}

class TCPClientThread implements Runnable {

	private final int BUFSIZE = 1024;
	private Socket socket;
	private InputStream inputStream;
	private OutputStream outputStream;
	private String receivedMessage;
	private int threadId;

	public TCPClientThread(Socket socket, int threadId) throws IOException {
		this.socket = socket;
		this.threadId = threadId;

		// create input and output streams for receiving and sending messages
		inputStream = new DataInputStream(this.socket.getInputStream());
		outputStream = new DataOutputStream(this.socket.getOutputStream());

	}

	@Override
	public void run() {
		System.out.printf("TCP connection started from %s:%d\n", socket.getInetAddress().getHostAddress(),
				socket.getPort());

		byte[] buffer = new byte[BUFSIZE];

		try {

			// do until send back the message to the client
			do {
				StringBuilder strBuilder = new StringBuilder();

				// do until reading the whole sent message then send it back to the client
				do {
					receivedMessage = "";

					// read message and save it in the buffer and get the number of read bytes
					int readBytes = inputStream.read(buffer);

					// read the buffer into a string, the if condition in case in the last message
					// and the stream in the end of the file
					if (readBytes > -1)
						receivedMessage = new String(buffer, 0, readBytes);

					// append the received part of the sent message to string builder for later send
					// in case of the buffer size is smaller than the sent message
					strBuilder.append(receivedMessage);
				} while (inputStream.available() > 0);

				receivedMessage = strBuilder.toString();

				if (!receivedMessage.isEmpty()) {

					// send message back
					outputStream.write(receivedMessage.getBytes());

					System.out.println("Client " + threadId + " send TCP echo request using "
							+ socket.getInetAddress().getHostAddress().toString() + ":" + socket.getPort()
							+ " | Sent and received:" + receivedMessage.length() + " bytes | Buffer Size: " + BUFSIZE
							+ " bytes");
				}

			} while (!receivedMessage.isEmpty());
			// close the thread socket
			socket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
