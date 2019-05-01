package unimelb.bitbox;


import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import javax.net.ServerSocketFactory;
import unimelb.bitbox.util.Document;
import unimelb.bitbox.util.HostPort;

public class Server extends Thread{

	private String ip;
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
			ip = server.getInetAddress().toString();
			System.out.println("Waiting for client connection..");
			
			// Wait for connections.
			while(true){
				Socket client = server.accept();
				counter++;
				Socketlist.add(client);

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
             
                System.out.println("Client1 connection number " + counter + " accepted:");
                System.out.println("Remote Port: " + clientSocket.getPort());
                System.out.println("Remote Hostname: " + clientSocket.getInetAddress().getHostName());
                System.out.println("Local Port: " + clientSocket.getLocalPort());
                System.out.println("Connection established");

                //Get the input/output streams for reading/writing data from/to the socket
                BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream(), "UTF-8"));
                BufferedWriter out = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream(), "UTF-8"));


                //Read the message from the client and reply
                //Notice that no other connection can be accepted and processed until the last line of
                //code of this loop is executed, incoming connections have to wait until the current
                //one is processed unless...we use threads!
                String clientMsg = null;
                    while((clientMsg = in.readLine()) != "exit") {
						Document received = Document.parse(clientMsg);
						if(clientMsg.equals("3time_handshake_complete")) {
							System.out.println(clientMsg);
						}
						else {
							if (received.get("command").equals("HANDSHAKE_REQUEST")) {
								System.out.println("HandShake Request Accepted by Server");
								System.out.println("HandSacke Response Sent");
								if (Socketlist.size() <= 10) {
									Document handshake = new Document();
									handshake.append("command", "HANDSHAKE_RESPONSE");
									HostPort hostport = new HostPort(ip, port);
									handshake.append("hostPort", hostport.toDoc());
									out.write(handshake.toJson() + "\n");
									out.flush();
								} else {
									Socketlist.remove(10);
									Document handshake = new Document();
									handshake.append("command", "CONNECTION_REFUSED");
									handshake.append("message", "connection limit reached");
									ArrayList<Document> peers = new ArrayList<Document>();
									for (Socket s : Socketlist) {
										HostPort hostport = new HostPort(s.getInetAddress().toString(), s.getPort());
										peers.add(hostport.toDoc());
									}
									handshake.append("peers", peers);

									out.write(handshake.toJson()+"\n");
									out.flush();
								}
							} else {
								out.write(Peer.operation(received)+"\n");
								out.flush();
							}
						}
                    }
            }
            
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void sendtoClient(String message){
		for(Socket s: Socketlist){
			try {
				BufferedReader in = new BufferedReader(new InputStreamReader(s.getInputStream(), "UTF-8"));
				BufferedWriter out = new BufferedWriter(new OutputStreamWriter(s.getOutputStream(), "UTF-8"));
				out.write(message+"\n");
				out.flush();
			} catch (IOException e) {
				e.printStackTrace();
			}

		}
	}

}
