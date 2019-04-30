package unimelb.bitbox;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.ConnectException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;

import javax.sql.ConnectionEvent;

public class Client extends Thread {
	
	// IP and port
	private String ip;
	private int port;
	
	public Client(String ip, int port){
		this.ip = ip;
		this.port = port;
	}
	
	public void run() {
		try(Socket socket = new Socket(ip, port);){
			 

	            // Get the input/output streams for reading/writing data from/to the socket
	            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF-8"));
	            BufferedWriter out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), "UTF-8"));
	            System.out.println(in.readLine());
	            Scanner scanner = new Scanner(System.in);
	            String inputStr = null;

	            //While the user input differs from "exit"
	            while (!(inputStr = scanner.nextLine()).equals("exit")) {

	                // Send the input string to the server by writing to the socket output stream
	                out.write(inputStr + "\n");
	                out.flush();
	                System.out.println("Message sent");

	                // Receive the reply from the server by reading from the socket input stream
	                String received = in.readLine(); // This method blocks until there
	                // is something to read from the
	                // input stream
	                System.out.println("Message received: " + received);
	                }
		    
		} catch (ConnectException e) {
			try {
				Thread.sleep(5*1000);
				System.out.println("no por found, finding new ...."+ e.toString());
				run();
			} catch (InterruptedException e1) {
				e1.printStackTrace();
			}
		} catch (IOException e) {
			
		}

	}

}
