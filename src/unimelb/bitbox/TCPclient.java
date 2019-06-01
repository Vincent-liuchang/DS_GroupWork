package unimelb.bitbox;
import unimelb.bitbox.util.Configuration;
import unimelb.bitbox.util.Document;
import unimelb.bitbox.util.HostPort;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.BindException;
import java.net.ConnectException;
import java.net.Socket;
import java.net.SocketException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;

public class TCPclient extends Thread {

	// IP and port
	protected String ip;
	private int port;
	protected HostPort hostport;
	private ArrayList<HostPort> iplist;
	protected Socket socket;
	private Peer peer;


	public TCPclient(HostPort hostPort, Peer peer){
		this.hostport = hostPort;
		this.ip = hostport.host;
		this.port = hostport.port;
		this.peer = peer;
	}

	public void run() {
		try{
			String received = null;

			Socket socket = new Socket(ip, port);
			this.socket = socket;
			//Get the input/output streams for reading/writing data from/to the socket
			BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF-8"));
			BufferedWriter out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), "UTF-8"));

			Document handshake = new Document();
			handshake.append("command","HANDSHAKE_REQUEST");
			HostPort hostport = new HostPort(socket.getInetAddress().toString(), Integer.parseInt(Configuration.getConfigurationValue("port")));
			handshake.append("hostPort",hostport.toDoc());
			out.write(handshake.toJson()+"\n");
			out.flush();


			//While the user input differs from "exit"
			while (this.isInterrupted()&&(received = in.readLine()) !=null) {
				Document received_message = Document.parse(received);
				String anbMessage = new Operator().operation(received_message);

				if(anbMessage.equals("HandShakeComplete")){
					System.out.println("HandShake Response Received, the server is: " + ip);
				}
				else if(!anbMessage.equals("ok")) {
					System.out.println(anbMessage);
				}
			}

		}

		catch (ClassCastException e){
			System.out.println("received invalid protocol");
		}
		catch (ConnectException e) {
			try {
				System.out.println("this peer's server not online, try in 5 seconds .... this peer is:"+this.ip);
                peer.peerHosts.remove(ip);
                peer.TCPserver.serverlist.remove(new HostPort(ip,port));
                peer.peerHosts.remove(new HostPort(ip,port));
				Thread.sleep(5*1000);
				run();
			}
			catch (InterruptedException e1) {
				e1.printStackTrace();
			}
		}
		catch (SocketException  e) {
			System.out.println("this peer's server  offline, reconnecting ....");
			try {
				Thread.sleep(5*1000);
				run();
			} catch (InterruptedException e1) {
				e1.printStackTrace();
			}
		}
		catch(IOException| NoSuchAlgorithmException e){
			e.printStackTrace();
		}

	}

	public void sendtoServer(String message){

		if(socket != null){
			try {
				BufferedWriter out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), "UTF-8"));
				System.out.println("Local send: " + message);

				out.write(message+"\n");
				out.flush();
			}
			catch (SocketException  e) {
				System.out.println("a peer's server  offline");
				run();
			}
			catch (IOException e) {
				e.printStackTrace();
			}

		}


	}

}