package assign3;

import java.io.File;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.util.Arrays;

import assign3.Packet.ACKPacket;
import assign3.Packet.ErrorPacket;
import assign3.Packet.Packet.PacketType;
import assign3.Packet.ResponsePacket;

public class TFTPServer {
	public static final int TFTPPORT = 4970;
	public static final int BUFSIZE = 516;
	private final int 	TRANSFARE_RATE = 10 ;
	public static final String READDIR = "src/assign3/res/read/";
	public static final String WRITEDIR = "src/assign3/res/write/";
	// OP codes
	public static final int OP_RRQ = 1;
	public static final int OP_WRQ = 2;
	public static final int OP_DAT = 3;
	public static final int OP_ACK = 4;
	public static final int OP_ERR = 5;

	public static void main(String[] args) throws IOException {
		if (args.length > 0) {
			System.err.printf("usage: java %s\n", TFTPServer.class.getCanonicalName());
			System.exit(1);
		}
		// Starting the server
		try {
			TFTPServer server = new TFTPServer();
			server.start();
		} catch (SocketException e) {
			e.printStackTrace();
		}
	}

	private void start() throws IOException {
		byte[] buf = new byte[BUFSIZE];

		// Create socket
		DatagramSocket socket = new DatagramSocket(null);

		// Create local bind point
		SocketAddress localBindPoint = new InetSocketAddress(TFTPPORT);
		socket.bind(localBindPoint);

		System.out.printf("Listening at port %d for new requests\n", TFTPPORT);

		// Loop to handle client requests
		while (true) {

			final InetSocketAddress clientAddress = receiveFrom(socket, buf);

			// If clientAddress is null, an error occurred in receiveFrom()
			if (clientAddress == null)
				continue;

			final StringBuffer requestedFile = new StringBuffer();
			StringBuffer octet = new StringBuffer();

			final int reqtype = ParseRQ(buf, requestedFile, octet);

			new Thread() {
				public void run() {
					try {
						DatagramSocket sendSocket = new DatagramSocket(0);
						// set the time out to the socket
						sendSocket.setSoTimeout(600);
						
						// Connect to client
						sendSocket.connect(clientAddress);

						System.out.printf("%s request from %s using port %d\n",
								(reqtype == OP_RRQ) ? "Read" : "Write", clientAddress.getHostName(),
								clientAddress.getPort());

						// Read request
						if (reqtype == OP_RRQ) {
							requestedFile.insert(0, READDIR);
							HandleRQ(sendSocket, requestedFile.toString(), OP_RRQ, octet.toString());
						}
						// Write request
						else {
							requestedFile.insert(0, WRITEDIR);
							HandleRQ(sendSocket, requestedFile.toString(), OP_WRQ, octet.toString());
						}
						sendSocket.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}.start();
		}
	}

	/**
	 * Reads the first block of data, i.e., the request for an action (read or
	 * write).
	 * 
	 * @param socket
	 *            (socket to read from)
	 * @param buf
	 *            (where to store the read data)
	 * @return socketAddress (the socket address of the client)
	 */
	private InetSocketAddress receiveFrom(DatagramSocket socket, byte[] buf) throws IOException {
		// Create datagram packet
		DatagramPacket packet = new DatagramPacket(buf, BUFSIZE);

		// Receive packet
		socket.receive(packet);

		// Extract address and port from the received packet
		InetSocketAddress socketAddress = new InetSocketAddress(packet.getAddress(), packet.getPort());
		return socketAddress;
	}

	/**
	 * Parses the request in buf to retrieve the type of request and requestedFile
	 * 
	 * @param buf
	 *            (received request)
	 * @param requestedFile
	 *            (name of file to read/write)
	 * @return opcode (request type: RRQ or WRQ)
	 */
	private int ParseRQ(byte[] buf, StringBuffer requestedFile, StringBuffer octet) {
		// wrap the bytes array in ByteBuffer
		ByteBuffer byteBuffer = ByteBuffer.wrap(buf);
		
		// Read the operation code from the buffer
		int opcode = byteBuffer.getShort();
		
		// Read the requested file path from the buffer
		requestedFile.append(new String(buf, 2, buf.length - 2).split("\0")[0]);

		// Read the mode type from the buffer
		octet.append(new String(buf, 2, buf.length - 2).split("\0")[1]);

		return opcode;
	}

	/**
	 * Handles RRQ and WRQ requests
	 * 
	 * @param sendSocket
	 *            (socket used to send/receive packets)
	 * @param requestedFile
	 *            (name of file to read/write)
	 * @param opcode
	 *            (RRQ or WRQ)
	 * @throws IOException 
	 */
	private void HandleRQ(DatagramSocket sendSocket, String requestedFile, int opcode, String octect)  {
		// Check if the request-mode is octet
		if (! octect.equals("octet")) {
			ErrorPacket notDefinedErr = new ErrorPacket("Not Defined", (short) 0, OP_ERR , BUFSIZE );
			send_ERR(notDefinedErr , sendSocket);
			return;
		}
		// If the request is read
		if (opcode == OP_RRQ) {
			//Check if the file is exists
			if (! new File (requestedFile).exists() ) {
				ErrorPacket fileNotFoundErr = new ErrorPacket("File Not Found", (short) 1, OP_ERR , BUFSIZE );
				send_ERR(fileNotFoundErr , sendSocket);
				return;
			}

			// Empty response
			ResponsePacket responsePacket=null;
			try {
				// Try to read the file from the server's files
				responsePacket = new ResponsePacket (BUFSIZE , requestedFile , 1 , 1);
				// Send the response and receive the acknowledgment
				boolean result = send_DATA_receive_ACK(responsePacket , sendSocket);
			} catch (IOException e) {
				// If any I/O exception while reading the file from the server,
				// then, send access Violation Err. then finish
				ErrorPacket accessViolationErr = new ErrorPacket("Access Violation", (short) 2, OP_ERR , BUFSIZE );
				send_ERR(accessViolationErr , sendSocket);
				return;
			}
			
			
			
		} 
		// else If the request is write
		else if (opcode == OP_WRQ) {
			// Check if the file is already exists
			if (new File (requestedFile).exists()) {
				ErrorPacket fileAlreadyExist = new ErrorPacket("File Already Exist", (short) 6, OP_ERR , BUFSIZE );
				send_ERR(fileAlreadyExist , sendSocket);
				return;
			}
			
			// Empty response
			ResponsePacket responsePacket=null;
			try {
				responsePacket = new ResponsePacket (BUFSIZE , requestedFile , 1 , 2);
				boolean result = receive_DATA_send_ACK(responsePacket , sendSocket);
			} catch (IOException e) {
				// If any I/O exception while writing the file to the server,
				// then, send access Violation Err. then finish
				ErrorPacket accessViolationErr = new ErrorPacket("Access Violation", (short) 2, OP_ERR , BUFSIZE );
				send_ERR(accessViolationErr , sendSocket);
				return;
			}
			
		} else {
			// If the request is not read nor write, then send Illegal Tftp Operation error
			System.err.println("Invalid request. Sending an error packet.");
			ErrorPacket illegalTftpOperationErr = new ErrorPacket("Illegal TFTP Operation", (short) 4, OP_ERR , BUFSIZE );
			send_ERR(illegalTftpOperationErr , sendSocket);
			return;
		}
	}

	// Send the data to the client and then receive ACK
	private boolean send_DATA_receive_ACK(ResponsePacket responsePacket , DatagramSocket socket) {
		// An empty ACK
		ACKPacket responseACK = new ACKPacket(0) ;
		try {
			// Do this loop until send all file's parts
			while(responsePacket.hasNext()) {
				// Put the next part in the datagramPacket
				responsePacket.getNext();
				
				int transfareRate = TRANSFARE_RATE ;
				// Repeat the sending of the current part until receive ACK from the client or reach the transfer-rate
				do {
					// Reset the ACK package
					responseACK = new ACKPacket(0) ;
					
					//Send the current part
					socket.send(responsePacket.datagramPacket);
					
					// Receive ACK from the client
					socket.receive(responseACK.datagramPacket);
					
					// Analysis the received data
					responseACK.receivedDataAnalysis();
					
					// If the port in the ACK packet does not match the socket's port
					// then, send Unknown Transfer Id Error. Then, finish
					if (responseACK.datagramPacket.getPort() != socket.getPort()) {
						ErrorPacket unknownTransferIdErr = new ErrorPacket("Unknown Transfer Id", (short) 5, OP_ERR , BUFSIZE );
						send_ERR(unknownTransferIdErr , socket);
						return false;
					}
					
				}while((responseACK.packetCode != 4 || responseACK.block != responsePacket.block -1)&& -- transfareRate>0 );
				
				// If transfer rate reach to 0, this means that the client does not response
				// then, 
				if (transfareRate == 0) {
					System.out.println("Error while sending: the client not response.");
					return false;
				}
			}
		} catch (IOException e) {
			// If any I/O exception while sending the file to the server,
			// then, send access Violation Err. then finish
			ErrorPacket accessViolationErr = new ErrorPacket("Access Violation", (short) 2, OP_ERR , BUFSIZE );
			send_ERR(accessViolationErr , socket);
			return false;
		}
		
		return true;
	}

	// Receive the data from the client and then send ACK
	private boolean receive_DATA_send_ACK(ResponsePacket responsePacket , DatagramSocket socket) throws IOException {
		// ACK with block 0
		ACKPacket responseACK = new ACKPacket(0) ;
		// send the ACK
		socket.send(responseACK.datagramPacket);
		
		File receivedFile = responsePacket.file;
		
		// Do this loop until receive all file's parts
		do {
			// receive data from the client
			socket.receive(responsePacket.datagramPacket);
			// Analysis the received data
			responsePacket.receivedDataAnalysis();
			
			// if the received data is an error packet, then delete the file and return false
			if (responsePacket.packetType == PacketType.ERROR) {
				System.err.println("Error while receiving.");
				responsePacket.outStream.close();
				receivedFile.delete();
				return false;
			}
			
			// If there is no space in the hard disk,
			// then send Disk Full Error
			if (receivedFile.getUsableSpace()< responsePacket.datagramPacket.getLength()) {
				ErrorPacket diskFullErr = new ErrorPacket("Disk Full Or Allocation Exceeded", (short) 3, OP_ERR , BUFSIZE );
				send_ERR(diskFullErr , socket);
				return false;
			}
			
			// If the port in the received packet does not match the socket's port
			// then, delete the file, send Unknown Transfer Id Error. Then, return false
			if (responsePacket.datagramPacket.getPort() != socket.getPort()) {
				System.err.println("Error while receiving.");
				responsePacket.outStream.close();
				receivedFile.delete();
				ErrorPacket unknownTransferIdErr = new ErrorPacket("Unknown Transfer Id", (short) 5, OP_ERR , BUFSIZE );
				send_ERR(unknownTransferIdErr , socket);
				return false;
			}
			
			// If reach here, so no error. Then write the data to the hard disk
			responsePacket.outStream.write(Arrays.copyOfRange(responsePacket.datagramPacket.getData(), 4, responsePacket.datagramPacket.getLength()));
			responsePacket.outStream.flush();
			if (!responsePacket.hasClientNext()) {
				responsePacket.outStream.close();
			}
			
			// Reset the ACK to the new block and send it 
			responseACK = new ACKPacket(responsePacket.block) ;
			socket.send(responseACK.datagramPacket);
		}while (responsePacket.hasClientNext());
		
		return true;
	}

	// Method to send an error packet
	private void send_ERR(ErrorPacket errPacket , DatagramSocket socket)  {
		ACKPacket errACK = new ACKPacket(0) ;
		try {
			socket.send(errPacket.datagramPacket);
			socket.receive(errACK.datagramPacket);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}

}