package unimelb.bitbox;

import java.io.IOException;
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
    public static String operation(Document received_document) throws IOException, NoSuchAlgorithmException {
        if(received_document.getString("command").equals("HANDSHAKE_RESPONSE")){
            // receive command = handshake_response, from client
            try{
                Thread t = new Thread(() -> mainServer.initialSync());
                t.start();
                Thread.sleep(Long.parseLong(Configuration.getConfigurationValue("syncInterval")));
            } catch(Exception e) {
                e.printStackTrace();
            }
            return "three way handshake complete";
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

                        return r.createMessage() + "*" + r.fileByteRequest();
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

                    String bf = new String(byteBuffer.array());

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



//                if (r.pathSafe(received_document)) {
//
//                    if (r.nameExist(received_document)) {
//
//                        if (command.equals("FILE_CREATE_REQUEST")) {
//                            r.message = "file loader ready";
//                            r.status = true;
//
//                            r.position = 0;
//                            r.length = r.fd.getLong("fileSize");
//
//                            return r.createMessage() + "*" + r.fileByteRequest();
//
//
////                    if(!r.judgeContent(received_document)){
////                    }
//
//                        } else if (command.equals("FILE_BYTES_REQUEST")) {
//                            r.content = "";     // empty content for now
//                            r.message = "successfully read";
//                            r.status = true;
//                            return r.fileByteResponse();
//
//                        } else if (command.equals("FILE_DELETE_REQUEST")) {
//                            r.message = "pathname does not exist";
//                            r.status = false;
//                            return r.fileDeleteResponse();
//
//                        } else if (command.equals("FILE_MODIFY_REQUEST")) {
////                        some commands
//                            r.message = "pathname does not exist";
//                            r.status = false;
//                            return r.fileModifyResponse();
//
//                        } else if (command.equals("DIRECTORY_CREATE_REQUEST")) {
//
//                            r.message = "directory create ok";
//                            r.status = false;
//                            return r.directoryCreateResponse();
//
//                        } else if (command.equals("DIRECTORY_DELETE_REQUEST")) {
//
//                            r.message = "directory does not exist";
//                            r.status = false;
//                            return r.directoryDeleteResponse();
//                        }else{
//
//                            return "invalid request";
//                        }
//                    }else{
//
//                        if (command.equals("FILE_CREATE_REQUEST")) {
//                            r.message = "file exists";
//                            r.status = false;
//
//                            return r.createMessage();
//
//                        } else if (command.equals("FILE_BYTES_REQUEST")) {
//                            r.content = "";     // empty content for now
//                            r.message = "successfully read";
//                            r.status = true;
//                            return r.fileByteResponse();
//
//                        } else if (command.equals("FILE_DELETE_REQUEST")) {
//                            r.message = "pathname does not exist";
//                            r.status = false;
//                            return r.fileDeleteResponse();
//
//                        } else if (command.equals("FILE_MODIFY_REQUEST")) {
////                        some commands
//                            r.message = "pathname does not exist";
//                            r.status = false;
//                            return r.fileModifyResponse();
//
//                        } else if (command.equals("DIRECTORY_CREATE_REQUEST")) {
//
//                            r.message = "directory create ok";
//                            r.status = false;
//                            return r.directoryCreateResponse();
//
//                        } else if (command.equals("DIRECTORY_DELETE_REQUEST")) {
//
//                            r.message = "directory does not exist";
//                            r.status = false;
//                            return r.directoryDeleteResponse();
//                        }else{
//
//                            return "invalid request";
//                        }
//                    }
//                }
            }else if(command.contains("RESPONSE")){
                if(command.equals("FILE_CREATE_RESPONSE")){

                    return "file create response received";

                }else if(command.equals("FILE_BYTES_RESPONSE")){
//                        some commands

                    if(createOrModify){
                        String content = received_document.getString("content");
                        ByteBuffer bf = ByteBuffer.wrap(content.getBytes());

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
                            return "file bytes response";
                        }else{
                            return null;
                        }

                    }



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