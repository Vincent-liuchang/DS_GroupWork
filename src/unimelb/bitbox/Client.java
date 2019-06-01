/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package unimelb.bitbox;

import cmdlineagsdemo.CmdLineArgs;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.security.interfaces.RSAPrivateKey;
import java.util.ArrayList;
import java.util.Base64;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import static unimelb.bitbox.RSATest.myPrivateKey;
import unimelb.bitbox.util.Document;
import unimelb.bitbox.util.HostPort;

/**
 *
 * @author Administrator
 */
public class Client {
    
    private static RSAPrivateKey myPrivateKey ;
    public static void main(String args[]) throws CmdLineException, IOException, Exception {
        CmdLineArgs argsBean = new CmdLineArgs();
      
		
		//Parser provided by args4j
		CmdLineParser parser = new CmdLineParser(argsBean);
			
			//Parse the arguments
			parser.parseArgument(args);
			
			//After parsing, the fields in argsBean have been updated with the given
			//command line arguments
                String identity = argsBean.getIdentity();
                HostPort hp = new HostPort(argsBean.getMyServer()); 

                Socket socket = new Socket(hp.host, hp.port);
                myPrivateKey =  DecodeRSA.generatePriByPath("/Users/yw/IntelliJProjects/DS_GroupWork/bitboxclient_rsa");
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF-8"));
	        BufferedWriter out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), "UTF-8"));
                
                Document authorizeRequest = new Document();
                authorizeRequest.append("command", "AUTH_REQUEST");
                authorizeRequest.append("identity", identity);
                
                out.write(authorizeRequest.toJson() + "\n");
                out.flush();
               
               
                String received_message = in.readLine();
  
                Document response = Document.parse(received_message);
                
                if(response.getString("status").equals("true")) {
                    String aes = response.getString("AES128");
                    
                        
                    String AESKey = Encryption.RSAdecrypt(aes, myPrivateKey);
                    
             
                    
                String command = argsBean.getCommand();
                
                //Send command
                if(command.equals("list_peers")) {
                    Document lp = new Document();
                    lp.append("command", "LIST_PEERS_REQUEST");
                    
                    String AESencryptedMessage = Encryption.AESencrypt(lp.toJson(), AESKey);
                    
                    Document pl = new Document();
                    pl.append("payload", AESencryptedMessage);
                    
                    out.write(pl.toJson() + "\n");
                    out.flush();
                    
                }else if(command.equals("connect_peer")) {
                     HostPort p = new HostPort(argsBean.getPeer());
                     Document cp = new Document();
                    cp.append("command", "CONNECT_PEER_REQUEST");
                    cp.append("host", p.host);
                    cp.append("port", p.port);
                    
                    String AESencryptedMessage = Encryption.AESencrypt(cp.toJson(), AESKey);
                    
                    Document pl = new Document();
                    pl.append("payload", AESencryptedMessage);
                    
                    out.write(pl.toJson() + "\n");
                    out.flush();
                    
                }else if(command.equals("disconnect_peer")) {
                     HostPort p = new HostPort(argsBean.getPeer());
                    Document dp = new Document();
                    dp.append("command", "DISCONNECT_PEER_REQUEST");
                    dp.append("host", p.host);
                    dp.append("port", p.port);
                    
                    String AESencryptedMessage = Encryption.AESencrypt(dp.toJson(), AESKey);
                    
                    Document pl = new Document();
                    pl.append("payload", AESencryptedMessage);
                    
                    out.write(pl.toJson() + "\n");
                    out.flush();
                    
                }
                
                Document receivedPayload = Document.parse(in.readLine());
                
                Document commandResponse = Document.parse(Encryption.AESdecrypt(receivedPayload.getString("payload"), AESKey));
                
                if(commandResponse.getString("command").equals("LIST_PEERS_RESPONSE")) {
                    ArrayList<Document> peers = (ArrayList)commandResponse.get("peers");
                    System.out.println("Connected peers are: ");
                    
                    if(peers.isEmpty())
                        System.out.println("No peer is connected.");
                    
                    for(Document peer : peers) {
                        System.out.println(new HostPort(peer));
                    }
                        
                }else
                    System.out.println(commandResponse.getString("message"));;
                }
                else System.out.println(response.getString("message"));

    }
}
