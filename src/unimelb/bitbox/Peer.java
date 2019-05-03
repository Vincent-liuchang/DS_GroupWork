package unimelb.bitbox;

import java.io.IOException;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.logging.Logger;

import unimelb.bitbox.util.Configuration;
import unimelb.bitbox.util.Document;
import unimelb.bitbox.util.FileSystemManager;

public class Peer
{
    static private ServerMain mainServer;
    private static Logger log = Logger.getLogger(Peer.class.getName());
    private static boolean createOrModify;    // true = create    false = modify

    public static void main( String[] args ) throws IOException, NumberFormatException, NoSuchAlgorithmException, InterruptedException
    {
        System.setProperty("java.util.logging.SimpleFormatter.format",
                "[%1$tc] %2$s %4$s: %5$s%n");
        log.info("BitBox Peer starting...");
        Configuration.getConfiguration();

        mainServer = new ServerMain();
    }

    private int port =  Integer.parseInt(Configuration.getConfigurationValue("port"));
    private String [] peerstring = Configuration.getConfigurationValue("peers").split(" ");
    private ArrayList<String> peers = new ArrayList<String>(Arrays.asList(peerstring));
    private Client client = new Client(peers, 8111);
    private Server server = new Server(port);;

    public  void start(){
        client.start();
        server.start();
    }

    public  void sentToOtherPeers(String message){
        client.sendtoServer(message);
        server.sendtoClient(message);
    }

    public static void sync(){
        try{
            Thread t = new Thread(() -> mainServer.initialSync());
            t.start();
            Thread.sleep(Long.parseLong(Configuration.getConfigurationValue("syncInterval")));
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    public static String operation(Document received_document) throws IOException, NoSuchAlgorithmException {
        if(received_document.getString("command").equals("HANDSHAKE_RESPONSE")){
            // receive command = handshake_response, from client
            Peer.sync();
            return "nothing";
        }else {
            Response r = new Response(received_document);
            String command = received_document.getString("command");

            if(command.contains("REQUEST")) {
                if (command.equals("FILE_CREATE_REQUEST")) {

                    if(r.pathSafe(received_document) && !r.nameExist(received_document)){

                        r.message = "file loader ready";
                        r.status = true;


                        r.position = 0;
                        r.length = r.fd.getLong("fileSize");

                        createOrModify = true;

                        return r.createMessage() + "longgenb1995" + r.fileByteRequest();
//                        return r.fileByteRequest();

                    }else{
                        r.message = "file loader not ready";
                        r.status = false;

                        return r.createMessage();
                    }
                }else if(command.equals("FILE_BYTES_REQUEST")) {


                    ByteBuffer byteBuffer = ServerMain.fileSystemManager.readFile(
                            r.fd.getString("md5"),
                            received_document.getLong("position"),
                            received_document.getLong("length"));


                    String bf = Base64.getEncoder().encodeToString(byteBuffer.array());
//                    String bf = new String(byteBuffer.array());

                    r.content = bf;
                    r.message = "successfully read";
                    r.status = true;
                    return r.fileByteResponse();

                }else if (command.equals("FILE_DELETE_REQUEST")) {

                    if(r.pathSafe(received_document) && r.nameExist(received_document)){

                        ServerMain.fileSystemManager.deleteFile(
                                received_document.getString("pathName"),
                                r.fd.getLong("lastModified"),
                                r.fd.getString("md5"));
                        r.status = true;
                        r.message = "file delete succeed";

                    }else{
                        r.status = false;
                        r.message = "file delete failed";

                    }
                    return r.fileDeleteResponse();
                }else if (command.equals("DIRECTORY_CREATE_REQUEST")) {
                    if(r.pathSafe(received_document) &&
                            !ServerMain.fileSystemManager.dirNameExists(received_document.getString("pathName"))) {

                        ServerMain.fileSystemManager.makeDirectory(received_document.getString("pathName"));
                        r.message = "directory create succeed";
                        r.status = true;
                    }else{
                        r.message = "directory create failed";
                        r.status = false;
                    }
                    return r.directoryCreateResponse();

                }else if (command.equals("DIRECTORY_DELETE_REQUEST")) {

                    if(r.pathSafe(received_document) &&
                            ServerMain.fileSystemManager.dirNameExists(received_document.getString("pathName"))
                    ) {

                        ServerMain.fileSystemManager.deleteDirectory(received_document.getString("pathName"));
                        r.message = "directory delete succeed";
                        r.status = true;
                    }else{
                        r.message = "directory delete failed";
                        r.status = false;
                    }
                    return r.directoryCreateResponse();
                }


            }else if(command.contains("RESPONSE")){

                if(command.equals("FILE_BYTES_RESPONSE")){

                    if(createOrModify){
                        String content = received_document.getString("content");

                        ByteBuffer bf = ByteBuffer.wrap(Base64.getDecoder().decode(content));


                        ServerMain.fileSystemManager.createFileLoader(
                                received_document.getString("pathName"),
                                r.fd.getString("md5"),
                                received_document.getLong("length"),
                                r.fd.getLong("lastModified"));

                        ServerMain.fileSystemManager.writeFile(
                                received_document.getString("pathName"),
                                bf,
                                received_document.getLong("position"));

                        if(ServerMain.fileSystemManager.checkWriteComplete(received_document.getString("pathName"))){
                            return "nothing";
                        }else{

                            ServerMain.fileSystemManager.cancelFileLoader(received_document.getString("pathName"));

                            r.position = 0;
                            r.length = r.fd.getLong("fileSize");
                            return r.fileByteRequest();
                        }

                    }

                }

                }
            }

            return "nothing";
        }
    }


