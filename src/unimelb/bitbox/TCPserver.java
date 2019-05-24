package unimelb.bitbox;


import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import javax.net.ServerSocketFactory;

import unimelb.bitbox.util.Configuration;
import unimelb.bitbox.util.Document;
import unimelb.bitbox.util.HostPort;

public class TCPserver extends Thread{

	private String ip;
	// Declare the port number
	private int port;
	// Identifies the user number connected
	private int counter = 0 ;
	
	private ArrayList<Socket> Socketlist = new ArrayList<Socket>();
	
	public TCPserver(int port){
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
				if(Socketlist.size() != 0) {
					for (int i = 0; i< Socketlist.size(); i++) {
						if (Socketlist.get(i).isClosed()) {
							Socketlist.remove(i);
						}
					}
				}

				Socketlist.add(client);
				System.out.println("Now the server has " + Socketlist.size() + " clients"+"\n");

				System.out.println("TCPclient "+counter+": Applying for connection!"+"\n");
				
				
				// Start a new thread for a connection
				Thread t = new Thread(() -> serveClient(client));
				t.start();
			}
			
		} catch (IOException e) {
			e.printStackTrace();
		}

		
	}
	
	private void serveClient(Socket client){
		try(Socket clientSocket = client) {
			//Listen for incoming connections for ever


				System.out.println("Client1 connection number " + counter + " accepted:");
				System.out.println("Remote Port: " + clientSocket.getPort());
				System.out.println("Remote Hostname: " + clientSocket.getInetAddress().getHostName());
				System.out.println("Local Port: " + clientSocket.getLocalPort());
				System.out.println("Connection established"+"\n");

				//Get the input/output streams for reading/writing data from/to the socket
				BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream(), "UTF-8"));
				BufferedWriter out = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream(), "UTF-8"));


				//Read the message from the client and reply
				//Notice that no other connection can be accepted and processed until the last line of
				//code of this loop is executed, incoming connections have to wait until the current
				//one is processed unless...we use threads!

				String clientMsg;
				while ((clientMsg = in.readLine()) !=  null) {


					if (!clientMsg.contains("_")) {
						System.out.println(clientMsg);
					} else {
						Document received = Document.parse(clientMsg);
						if (received.get("command").equals("HANDSHAKE_REQUEST")) {
							System.out.println("HandShake Request Accepted by TCPserver");

							if (Socketlist.size() <= Integer.parseInt(Configuration.getConfigurationValue("maximumIncommingConnections"))) {
								Document handshake = new Document();
								handshake.append("command", "HANDSHAKE_RESPONSE");
								HostPort hostport = new HostPort(Configuration.getConfigurationValue("advertisedName"), port);
								handshake.append("hostPort", hostport.toDoc());
								out.write(handshake.toJson() + "\n");
								out.flush();
								System.out.println("HandShake Response Sent"+"\n");

								Synchronize syn1 = new Synchronize(Peer.mainServer);
								syn1.start();
							} else {
								Socketlist.remove(Integer.parseInt(Configuration.getConfigurationValue("maximumIncommingConnections")));
								Document handshake = new Document();
								handshake.append("command", "CONNECTION_REFUSED");
								handshake.append("message", "connection limit reached");
								ArrayList<Document> peers = new ArrayList<Document>();
								for (Socket s : Socketlist) {
									HostPort hostport = new HostPort(s.getInetAddress().toString(), s.getPort());
									peers.add(hostport.toDoc());
								}
								handshake.append("peers", peers);

								out.write(handshake.toJson() + "\n");
								out.flush();
							}
						} else {
							if(new Peer().operation(received).contains("longgenb1995")){

								String[] message = new Peer().operation(received).split("longgenb1995");

								for(String m : message){
									Document receive = Document.parse(m);
									out.write(receive.toJson()+"\n");
									out.flush();
								}

							}else {

								if (!new Peer().operation(received).equals("ok")) {
									System.out.println("TCPserver received from client: " + new Peer().operation((received)) + "\n");
									out.write(new Peer().operation(received) + "\n");
									out.flush();
								}
							}
						}
					}


				}


		}catch(SocketException e){

			System.out.println("Socketlist need to be update");
			System.out.println(Socketlist.toString());
			System.out.println("client off line");

		} catch (IOException | NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
	}

	public void sendtoClient(String message){
		if(Socketlist.size()!=0){
			for(Socket s: Socketlist){
				try {
					BufferedReader in = new BufferedReader(new InputStreamReader(s.getInputStream(), "UTF-8"));
					BufferedWriter out = new BufferedWriter(new OutputStreamWriter(s.getOutputStream(), "UTF-8"));
					System.out.println("TCPserver send to Clients:" + message);

					out.write(message+"\n");
					out.flush();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
}
