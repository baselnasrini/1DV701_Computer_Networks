package assign1;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;

public abstract class NetworkingLayer {

	protected int BUFSIZE = 1024;
	protected final int MYPORT = 0;
	protected final String MSG = "An Echo Message!";
	protected byte[] buffer;
	protected int transRate;
	protected String IP;
	protected int port;
	protected SocketAddress localBindPoint;
	protected SocketAddress remoteBindPoint;
	protected String receivedMSG ;

	public NetworkingLayer(String[] args) {
		
		// check the arguments
		checkArguments(args);
		// check the sent message
		checkMessage();

		// create local and remote bind points
		localBindPoint = new InetSocketAddress(MYPORT);
		remoteBindPoint = new InetSocketAddress(IP, port);
	}

	// This method will run the client for 1 second
	protected void startClient() throws IOException {

		//catch the start time of the sending process
		long startTime = System.nanoTime();
		
		// Calculate the end time of the process ( after 1 sec)
		long endTime = startTime + 1000000000;
		int messageCounter = 0 ;
		
		// sending the message how the transfer rate is
		for (int i = 0; i < transRate; i++) {
	
			long sendingTime = startTime + (((long) i * 1000000000) / (long) transRate);

			// method will delay process until the time of sending the next message 
			sleepToTime(sendingTime);

			// stop the sending process if it's more than 1 sec
			if (System.nanoTime() > endTime) {
				break;
			}
			
			// this method will be implemented in clients class for sending the messages
			sendAndReceive();

			// compare sent and received messages
			checksentAndReceiveMessages();

			// counter for catch the sent messages
			messageCounter++;	
		}
		
		// method for closing socket at end
		closeSocket();
		
		System.out.println(messageCounter + " messages sent");
		System.out.println(transRate - messageCounter + " messages left");
		
		// delay method to until complete the 1 second (after sending the last message)
		sleepToEndTime(endTime);
		
		// the complete process time from the beginning to the end
		long totalTime = System.nanoTime() - startTime;
		System.out.println("Total Time: " + totalTime +" ns" );

	}

	// method to initializing the client
	protected abstract void initializing() throws IOException ;
	
	protected abstract void sendAndReceive() throws IOException;

	protected abstract void closeSocket();
	
	protected abstract void checkMessage() ;

	protected void checksentAndReceiveMessages() {
		
		if (MSG.compareTo(receivedMSG) == 0)
			System.out.printf("%d bytes sent and received | Buffer size: %d bytes\n", receivedMSG.length() , BUFSIZE);
		else
			System.out.printf("Sent and received msg not equal!, Sent: %d bytes | Received: %d bytes | Buffer size: %d bytes\n", MSG.length() , receivedMSG.length() , BUFSIZE);
	}
	
	private void sleepToTime(long time) {
		if (System.nanoTime() < time)
			System.out.println("Sleeping Time: "+ (time - System.nanoTime()) +" ns" );
			
		while (System.nanoTime() < time);

	}

	private void sleepToEndTime(long time) {
		if (System.nanoTime() < time)
			System.out.println("Sleeping Time to complete 1s: "+ (time - System.nanoTime()) +" ns" );
		while (System.nanoTime() < time);

	}
	
	private void checkArguments(String[] arr) {

		if (arr.length != 4) {
			System.err.printf("The number of arguments is wrong");
			System.exit(1);
		}

		
		
		IP = arr[0];
		port = convertToInt(arr[1], "Port");
		BUFSIZE = convertToInt(arr[2], "Buffer Size");
		transRate = convertToInt(arr[3], "Message Transfer Rate");
		
		String[] splitIP = IP.split("\\.");

		// IP must be four parts divided by three dots "."
		if (splitIP.length != 4) {
			System.err.println("Error: IP address is not valid!");
			System.exit(1);
		}

		// IP must have four integers between 0 and 255
		for (int i = 0; i < splitIP.length; i++) {
			if (convertToInt(splitIP[i], "IP") > 255 || convertToInt(splitIP[i], "IP") < 0) { // Range
				System.err.println("Error: IP address is not valid!");
				System.exit(1);
			}

		}

		// Check port number
		if (port > 65535 || port < 1) {
			System.err.println("Error: Port number is not correct!");
			System.exit(1);
		}

		// minimum buffer size is 1
		if (BUFSIZE < 1) {
			System.err.println("Error: Buffer Size is not correct!");
			System.exit(1);
		}

		// catch out of memory exception if the buffer size is too big
		try {
			buffer = new byte[BUFSIZE];
		} catch (OutOfMemoryError e) {
			System.err.println("Error: Buffer size is too big!!");
			System.exit(1);
		}

		// Check if transfer rate is less than 0
		if (transRate < 0) {
			System.err.println("Error: Message transfer rate is not correct!");
			System.exit(1);
		}
		
		// transfer rate is 1 at minimum
		if (transRate == 0) {
			transRate = 1;
		}
				
	}

	// used for convert the arguments to integers and catch exception if it contains other than integers 
	private int convertToInt(String str, String arg) {
		try {
			return Integer.parseInt(str);
		} catch (Exception e) {
			System.err.printf("Error: %s is not valid!", arg);
			System.exit(1);
			return -1;
		}

	}
}
