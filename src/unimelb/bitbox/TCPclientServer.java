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
import java.security.interfaces.RSAPublicKey;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.net.ServerSocketFactory;

import unimelb.bitbox.util.Configuration;
import unimelb.bitbox.util.Document;
import unimelb.bitbox.util.HostPort;

public class TCPclientServer extends Thread{

	private String ip;
	// Declare the port number
	private int port;
	// Identifies the user number connected
	private int counter = 0 ;

	private Peer peer;
        public static String AESkey;

	public TCPclientServer(int port, Peer peer){
		this.port = port;
		this.peer =peer;
                AESkey = Encryption.AESkey();
	}
	
	public void run(){
		ServerSocketFactory factory = ServerSocketFactory.getDefault();
		try(ServerSocket server = factory.createServerSocket(port)){
			ip = server.getInetAddress().toString();
			System.out.println("Waiting for peer connection..");
			
			// Wait for connections.
			while(true){
                            
				Socket client = server.accept();
                       
                                       
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


			System.out.println("ClientCommander connection number " + counter + " accepted:");
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
	
				if(clientMsg.contains("command")){
                                    String publicKey = null;
                                    boolean authorized = false;
                                    String[] keys = Configuration.getConfigurationValue("authorized_keys").split(" ");
                                    for(int i = 0; i < keys.length; i++)
                                        if(keys[i].contains(received.getString("identity"))) {
                                            publicKey = keys[i-1];
                                            authorized = true;
                                        }
                                    RSAPublicKey RSAPublicKey = DecodeRSA.generatePub(publicKey);
                                    JasonCreator res = new JasonCreator(received);
                                    res.aes = AESkey;
                                    res.rsapub = RSAPublicKey;
                                    
                                    if(authorized)
                                        out.write(res.authorizedResponse() + "\n");
                                    else out.write(res.notAuthorizedResponse() + "\n");
                                    out.flush();
                                }
                                else {
                                    Document rec = Document.parse(Encryption.AESdecrypt(received.getString("payload"), AESkey));
                              
                                if(rec.get("command").equals("LIST_PEERS_REQUEST")) {
                                    System.out.println("list request");
                                    Document res = new Document();
                                    res.append("command", "LIST_PEERS_RESPONSE");
                                    if(Configuration.getConfigurationValue("mode").equals("tcp")) {
                                        ArrayList<Document> peers = new ArrayList<Document>();
						for (HostPort s : peer.TCPserver.serverlist) {
							peers.add(s.toDoc());
						}
                                    res.append("peers", peers);
                                    }
                                    
                                    else{ 
                                        ArrayList<Document> peers = new ArrayList<Document>();
                                            for (HostPort s : peer.UDPserver.onlinePeers) {
						peers.add(s.toDoc());
                                            }
                                            
                                        res.append("peers", peers);
                                    }
                                    
                                    Document pl = new Document();
                                    pl.append("payload",  Encryption.AESencrypt(res.toJson(), AESkey));
                                    out.write(pl.toJson() + "\n");
                                    out.flush();
                                   
                                }else if(rec.get("command").equals("CONNECT_PEER_REQUEST")) {

                                    if(Configuration.getConfigurationValue("mode").equals("tcp")) {
                                        if(!peer.TCPserver.serverlist.contains(new HostPort(rec.getString("host"), (int) rec.getLong("port")))) {
                                            peer.TCPserver.serverlist.add(new HostPort(rec.getString("host"), (int) rec.getLong("port")));
                                            System.out.println("client add a new host to peer"+peer.TCPserver.serverlist);
                                        }
                                    }
                                    
                                    else
                                        if(!peer.UDPserver.onlinePeers.contains(new HostPort(rec.getString("host"), (int)rec.getLong("port"))))
                                        peer.UDPserver.onlinePeers.add(new HostPort(rec.getString("host"), (int)rec.getLong("port")));
                                    Document res = new Document();
                                    
                                    res.append("command", "CONNECT_PEER_RESPONSE");
                                    res.append("host", rec.getString("host"));
                                    res.append("port", (int)rec.getLong("port"));
                                    res.append("status", "true");
                                    res.append("message", "Trying to connect to peer(use list_peers to confirm)");                
                                    
                                    Document pl = new Document();
                                    pl.append("payload", Encryption.AESencrypt(res.toJson(), AESkey));
                                    out.write(pl.toJson() + "\n");
                                    out.flush();
                                   
                                }else if(rec.get("command").equals("DISCONNECT_PEER_REQUEST")) {
                                    System.out.println(rec.toJson());
                                    Document res = new Document();
                                    
                                    res.append("command", "DISCONNECT_PEER_RESPONSE");
                                    res.append("host", rec.getString("host"));
                                    res.append("port", (int)rec.getLong("port"));
                                    res.append("status", "true");
                                    res.append("message", "Trying to disconnect peer(use list_peers to confirm)");
                                    System.out.println("woshiniba");
                                      if(Configuration.getConfigurationValue("mode").equals("tcp")){
                                          System.out.println(rec.getString("host")+"duide");
                                          System.out.println(peer.peerHosts);
                                          System.out.println(peer.TCPserver.serverlist);
                                          for(TCPclient c: peer.clientList){
                                              System.out.println(c.hostport.host+"gdl");
                                              if(c.hostport.host.equals(rec.getString("host"))){
                                                  System.out.println("thread interupted");
                                                    peer.clientList.remove(c);
                                                    peer.peerHosts.remove(c.hostport);
                                                    peer.TCPserver.serverlist.remove(c.hostport);
                                                    c.socket.close();
                                                    c.interrupt();
                                              }
                                           }
                                          
                                      }
                                    else {
                                          peer.peerHosts.remove(new HostPort(rec.getString("host"), (int)rec.getLong("port")));
                                    peer.UDPserver.onlinePeers.remove(new HostPort(rec.getString("host"), (int)rec.getLong("port")));
                                }
                                    
                                    Document pl = new Document();
                                    pl.append("payload",  Encryption.AESencrypt(res.toJson(), AESkey));
                                    out.write(pl.toJson() + "\n");
                                    out.flush();
                                   
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

			}

		}catch (ClassCastException e){
			e.printStackTrace();
			System.out.println("receive a invalid protocol");

		}catch(SocketException e){
			System.out.println("1 client off line");
			System.out.println("onlinepeer list need to be update:");
			

		} catch (IOException | NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (Exception ex) {
                    ex.printStackTrace();
                Logger.getLogger(TCPserver.class.getName()).log(Level.SEVERE, null, ex);
            }
	}

}
