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

    private int port =  Integer.parseInt(Configuration.getConfigurationValue("port"));
    private String [] peerstring = Configuration.getConfigurationValue("peers").split(" ");
    private ArrayList<String> peers = new ArrayList<String>(Arrays.asList(peerstring));
    private Client client = new Client(peers, port);
    private Server server = new Server(port);;

    public  void start(){
        client.start();
        server.start();
    }

    public  void sentToOtherPeers(String message){
        client.sendtoServer(message);
        server.sendtoClient(message);
    }
    public static String operation(Document received_document)  {

        if(received_document.getString("command").equals("HANDSHAKE_RESPONSE")){       // receive command = handshake_response, from client
            return "three way handshake complete";
        }
        else {
            Response r = new Response(received_document);
            String command = received_document.getString("command");

            if(command.contains("REQUEST")) {

                if (r.pathSafe(received_document)) {

                    if (r.nameExist(received_document)) {

                        if (command.equals("FILE_CREATE_REQUEST")) {
                            r.message = "file loader ready";
                            r.status = true;

                            r.position = 0;
                            r.length = r.fd.getInteger("fileSize");

                            return r.createMessage() + "*" + r.fileByteRequest();


//                    if(!r.judgeContent(received_document)){
//                    }

                        } else if (command.equals("FILE_BYTES_REQUEST")) {
                            r.content = "";     // empty content for now
                            r.message = "successfully read";
                            r.status = true;
                            return r.fileByteResponse();

                        } else if (command.equals("FILE_DELETE_REQUEST")) {
                            r.message = "pathname does not exist";
                            r.status = false;
                            return r.fileDeleteResponse();

                        } else if (command.equals("FILE_MODIFY_REQUEST")) {
//                        some commands
                            r.message = "pathname does not exist";
                            r.status = false;
                            return r.fileModifyResponse();

                        } else if (command.equals("DIRECTORY_CREATE_REQUEST")) {

                            r.message = "directory create ok";
                            r.status = false;
                            return r.directoryCreateResponse();

                        } else if (command.equals("DIRECTORY_DELETE_REQUEST")) {

                            r.message = "directory does not exist";
                            r.status = false;
                            return r.directoryDeleteResponse();
                        }else{

                            return "invalid request";
                        }
                    }
                }
            }else if(command.contains("RESPONSE")){
                if(command.equals("FILE_CREATE_RESPONSE")){


                }else if(command.equals("FILE_BYTES_RESPONSE")){
//                        some commands
                    return "task completed";

                }else if(command.equals("FILE_DELETE_RESPONSE")){

                    return "task completed";


                }else if(command.equals("FILE_MODIFY_RESPONSE")){

                    return "task completed";

                }else if(command.equals("DIRECTORY_CREATE_RESPONSE")){
//                      if ...

                    return "task completed";

                }else if(command.equals("DIRECTORY_DELETE_RESPONSE")){
//                      if ...

                    return "task completed";

                }
            }

            return "nothing";
        }
    }
}
