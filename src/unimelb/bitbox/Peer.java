package unimelb.bitbox;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.logging.Logger;

import unimelb.bitbox.util.Configuration;

public class Peer 
{
	private static Logger log = Logger.getLogger(Peer.class.getName());
    public static void main( String[] args ) throws IOException, NumberFormatException, NoSuchAlgorithmException
    {
    	System.setProperty("java.util.logging.SimpleFormatter.format",
                "[%1$tc] %2$s %4$s: %5$s%n");
        log.info("BitBox Peer starting...");
        Configuration.getConfiguration();
        
        new ServerMain();
    }

    private ArrayList<String> peers = new ArrayList<String>();
    private int port;
    private Client client;
    private Server server;

    public Peer(){
        String [] peers = Configuration.getConfigurationValue("peers").split(" ");
        this.peers = new ArrayList<String>(Arrays.asList(peers));
        this.port = Integer.parseInt(Configuration.getConfigurationValue("port"));

        this.client = new Client(this.peers,port);
        client.start();
        this.server = new Server(port);
        server.start();
    }

    public void sentToOtherPeers(String message){
        client.sendtoServer(message);
        server.sendtoClient(message);
    }
}
