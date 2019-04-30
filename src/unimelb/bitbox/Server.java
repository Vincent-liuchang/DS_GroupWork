package unimelb.bitbox;


import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

import javax.net.ServerSocketFactory;

import unimelb.bitbox.util.Configuration;

public class Server extends Thread{
	
	// Declare the port number
	private int port;
	// Identifies the user number connected
	private int counter = 0 ;
	
	private ArrayList<Socket> Socketlist = new ArrayList<Socket>();
	
	public Server(int port){
		this.port = port;
	}
	
	public void run(){
		ServerSocketFactory factory = ServerSocketFactory.getDefault();
		try(ServerSocket server = factory.createServerSocket(port)){
			System.out.println("Waiting for client connection..");
			
			// Wait for connections.
			while(true){
				Socket client = server.accept();
				counter++;
				Socketlist.add(client);
				Configuration.setConfigurationValue("peers",client.getInetAddress().toString());
				//modified but not write to file
				
				
				System.out.println("Client "+counter+": Applying for connection!");
				
				
				// Start a new thread for a connection
				Thread t = new Thread(() -> serveClient(client));
				t.start();
			}
			
		} catch (IOException e) {
			e.printStackTrace();
		}

		
	}
	
	private void serveClient(Socket client){
		try(Socket clientSocket = client){
			//Listen for incoming connections for ever
            while (true) {
             
                System.out.println("Client1 conection number " + counter + " accepted:");
                System.out.println("Remote Port: " + clientSocket.getPort());
                System.out.println("Remote Hostname: " + clientSocket.getInetAddress().getHostName());
                System.out.println("Local Port: " + clientSocket.getLocalPort());
                System.out.println("Connection established");
                
                //Get the input/output streams for reading/writing data from/to the socket
                BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream(), "UTF-8"));
                BufferedWriter out = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream(), "UTF-8"));
                out.write("Server Ack " + "connected" + "\n");
                out.flush();

                //Read the message from the client and reply
                //Notice that no other connection can be accepted and processed until the last line of
                //code of this loop is executed, incoming connections have to wait until the current
                //one is processed unless...we use threads!
                String clientMsg = null;
               
                    while((clientMsg = in.readLine()) != null) {
                        System.out.println("Message from client " + counter + ": " + clientMsg);
                        out.write("Server Ack " + clientMsg + "\n");
                        out.flush();
                        System.out.println("Response sent");
                    }
            }
            
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
