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

	private Peer peer;
	private ArrayList<Socket> Socketlist = new ArrayList<Socket>();
	protected ArrayList<HostPort> serverlist = new ArrayList<>();

	public TCPserver(int port, Peer peer){
		this.port = port;
		this.peer =peer;
	}
	
	public void run(){
		ServerSocketFactory factory = ServerSocketFactory.getDefault();
		try(ServerSocket server = factory.createServerSocket(port)){
			ip = server.getInetAddress().toString();
			System.out.println("Waiting for peer connection..");
			
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
			System.out.println("Connection established" + "\n");

			//Get the input/output streams for reading/writing data from/to the socket
			BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream(), "UTF-8"));
			BufferedWriter out = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream(), "UTF-8"));


			//Read the message from the client and reply
			//Notice that no other connection can be accepted and processed until the last line of
			//code of this loop is executed, incoming connections have to wait until the current
			//one is processed unless...we use threads!

			String clientMsg;
			while ((clientMsg = in.readLine()) != null) {

				Document received = Document.parse(clientMsg);
				if (received.get("command").equals("HANDSHAKE_REQUEST")) {

					System.out.println("HandShake Request received, Connecting...");

					if (Socketlist.size() <= Integer.parseInt(Configuration.getConfigurationValue("maximumIncommingConnections"))) {
						Document handshake = new Document();
						handshake.append("command", "HANDSHAKE_RESPONSE");
						HostPort hostport = new HostPort(Configuration.getConfigurationValue("advertisedName"), port);
						handshake.append("hostPort", hostport.toDoc().toJson());
						out.write(handshake.toJson() + "\n");
						out.flush();
						System.out.println("HandShake Request Accepted");

						System.out.println(clientSocket.getInetAddress().toString());

						HostPort h = new HostPort(clientSocket.getInetAddress().toString().split("/")[1],(int)Document.parse(received.getString("hostPort")).getLong("port"));
						System.out.println(h.toDoc().toJson());
						serverlist.add(h);

					} else {
						Socketlist.remove(Integer.parseInt(Configuration.getConfigurationValue("maximumIncommingConnections")));
						Document handshake = new Document();
						handshake.append("command", "CONNECTION_REFUSED");
						handshake.append("message", "connection limit reached");
						ArrayList<Document> peers = new ArrayList<Document>();
						for (HostPort s : serverlist) {
							peers.add(s.toDoc());
						}
						handshake.append("peers", peers);
						out.write(handshake.toJson() + "\n");
						out.flush();
						System.out.println("HandShake Request refused");
					}
				} else {
					String anbMessage = new Operator().operation(received);
					if (anbMessage.contains("longgenb1995")) {
						String[] message = anbMessage.split("longgenb1995");

						for (String m : message) {
							Document receive = Document.parse(m);
							peer.clientToServer(clientSocket.getInetAddress().toString().split("/")[1],m);
						}

					} else {

						if (!anbMessage.equals("ok")) {
							out.write(anbMessage + "\n");
							out.flush();
						}
					}
				}


			}

		}catch (ClassCastException e){
			System.out.println("receive a invalid protocol");

		}catch(SocketException e){
			System.out.println("1 client off line");
			System.out.println("onlinepeer list need to be update:");
			System.out.println(Socketlist.toString());

		} catch (IOException | NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
	}

	public void sendtoClient(String message){
		if(Socketlist.size()!=0){
			for(Socket s: Socketlist){
				try {
					if (s.isClosed()) {
						Socketlist.remove(s);
					}
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
