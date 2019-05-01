package unimelb.bitbox;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.logging.Logger;

import unimelb.bitbox.util.Configuration;
import unimelb.bitbox.util.Document;
import unimelb.bitbox.util.FileSystemManager;

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

    private static int port =  Integer.parseInt(Configuration.getConfigurationValue("port"));
    private static String [] peerstring = Configuration.getConfigurationValue("peers").split(" ");
    private static ArrayList<String> peers = new ArrayList<String>(Arrays.asList(peerstring));
    private static Client client = new Client(peers,port);
    private static Server server = new Server(port);;

    public static void start(){
        client.start();
        server.start();
    }

    public static void sentToOtherPeers(String message){
        client.sendtoServer(message);
        server.sendtoClient(message);
    }
    public static String operation(Document received_document) throws IOException, NoSuchAlgorithmException {
        Response r = new Response(received_document);

        if(r.pathSafe(received_document)){
            if(r.nameExist(received_document)){
                String command = received_document.getString("command");
                if(command.equals("FILE_CREATE_REQUEST")){
                    r.message = "file loader ready";
                    r.status = "true";
                    return r.createMessage();
//                    if(!r.judgeContent(received_document)){
//                    }
                }
                else if(command.contains("FILE_DELETE_REUQEST")) {
                    return null;
                }
            }

        }
            return null;
    }

}
