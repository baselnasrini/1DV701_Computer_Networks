package assign1;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

class Client implements Runnable {
	
	public void run() {
		try {
						
			String[] args = {"192.168.157.3","4950","1024","2"};
			TCPEchoClient client = new TCPEchoClient(args) ;
			client.initializing();
			client.startClient();

		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}

public class multiTCPClients {

	public static void main(String[] args) throws IOException {
		
		ExecutorService executor = Executors.newFixedThreadPool(3); //create a thread loop with three threads
		
		for (int i = 0; i < 9; i++) { //create 12 tasks and submit them to the queue
			executor.submit(new Client());
		}
		
		executor.shutdown(); //stop accepting new tasks
	}
}
