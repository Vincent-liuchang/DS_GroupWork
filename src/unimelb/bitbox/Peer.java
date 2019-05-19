package unimelb.bitbox;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.logging.Logger;

import unimelb.bitbox.util.Configuration;
import unimelb.bitbox.util.Document;

public class Peer
{
    static protected ServerMain mainServer;
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
    private TCPclient TCPclient = new TCPclient(peers, port);
    private TCPserver TCPserver = new TCPserver(port);;

    public  void start(){
        TCPclient.start();
        TCPserver.start();
    }

    public  void sentToOtherPeers(String message){
        TCPclient.sendtoServer(message);
        TCPserver.sendtoClient(message);
    }

    public static void sync(){
        try{  
            
           Synchronize syn = new Synchronize(mainServer);
           syn.start();
            

        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    public String operation(Document received_document) throws IOException, NoSuchAlgorithmException {
        if(received_document.getString("command").equals("HANDSHAKE_RESPONSE")){
            // receive command = handshake_response, from TCPclient
            return "ok";
        }
        else {
           try{ 
               Response r = new Response(received_document);
           
            String command = received_document.getString("command");

            if(command.contains("REQUEST")) {
                if (command.equals("FILE_CREATE_REQUEST")) {

                    if(r.pathSafe(received_document)){
//                        && !r.nameExist(received_document)
                        r.message = "create file loader ready";
                        r.status = true;

                        r.position = 0;
                        r.length = r.fd.getLong("fileSize");

                        createOrModify = true;

                        System.out.println(r.message);

                        return r.createMessage() + "longgenb1995" + r.fileByteRequest();
                    }else{
                        r.message = "path not safe";
                        r.status = false;
                        
                        System.out.println(r.message);
                        return r.createMessage();
                    }
                }else if (command.equals("FILE_MODIFY_REQUEST")) {

                    if(r.pathSafe(received_document) && r.nameExist(received_document)){

                        r.message = "modify file loader ready";
                        r.status = true;

                        r.position = 0;
                        r.length = r.fd.getLong("fileSize");

                        createOrModify = false;
                        
                        System.out.println(r.message);

                        return r.createMessage() + "longgenb1995" + r.fileByteRequest();

                    }else{
                        r.message = "file loader not ready";
                        r.status = false;
                        
                        System.out.println(r.message);

                        return r.createMessage();
                    }
                }
                else if(command.equals("FILE_BYTES_REQUEST")) {


                    ByteBuffer byteBuffer = ServerMain.fileSystemManager.readFile(
                            r.fd.getString("md5"),
                            received_document.getLong("position"),
                            received_document.getLong("length"));


                    String bf = Base64.getEncoder().encodeToString(byteBuffer.array());
//                    String bf = new String(byteBuffer.array());

                    r.content = bf;
                    r.message = "successfully read";
                    r.status = true;
                    
                    System.out.println(r.message);
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
                    
                    System.out.println(r.message);
                    return r.fileDeleteResponse();
                }else if (command.equals("DIRECTORY_CREATE_REQUEST")) {
                    if(!ServerMain.fileSystemManager.dirNameExists(received_document.getString("pathName"))) {

                        ServerMain.fileSystemManager.makeDirectory(received_document.getString("pathName"));
                        r.message = "directory create succeed";
                        r.status = true;
                    }else{
                        r.message = "directory create failed";
                        r.status = false;
                    }
                    System.out.println(r.message);
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
                    
                    System.out.println(r.message);
                    return r.directoryCreateResponse();
                }else{

                    r.message = "message must contain a command field as string";
                      System.out.println("invalid protocol is " + command);
                      
                    System.out.println(r.message);
                    return r.invalidProtocol();
                }


            }else if(command.contains("RESPONSE")){

                if(command.equals("FILE_BYTES_RESPONSE")){

                    if(createOrModify){
                        if(r.nameExist(received_document)){
                            createOrModify = false;
                            String content = received_document.getString("content");

                            ByteBuffer bf = ByteBuffer.wrap(Base64.getDecoder().decode(content));

                            if(ServerMain.fileSystemManager.modifyFileLoader(
                                    received_document.getString("pathName"),
                                    r.fd.getString("md5"),
                                    r.fd.getLong("lastModified"))) {

                                ServerMain.fileSystemManager.writeFile(
                                        received_document.getString("pathName"),
                                        bf,
                                        received_document.getLong("position"));

                                if (ServerMain.fileSystemManager.checkWriteComplete(received_document.getString("pathName"))) {
                                    return "ok";
                                } else {

                                    ServerMain.fileSystemManager.cancelFileLoader(received_document.getString("pathName"));

                                    r.position = 0;
                                    r.length = r.fd.getLong("fileSize");
                                    return r.fileByteRequest();
                                }
                            }
                            else{
                                return "ok";
                            }
                        }
                        else {
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

                            if (ServerMain.fileSystemManager.checkWriteComplete(received_document.getString("pathName"))) {
                                return "ok";

                            } else {

                                ServerMain.fileSystemManager.cancelFileLoader(received_document.getString("pathName"));

                                r.position = 0;
                                r.length = r.fd.getLong("fileSize");
                                return r.fileByteRequest();
                            }
                        }

                    }else{
                        String content = received_document.getString("content");

                        ByteBuffer bf = ByteBuffer.wrap(Base64.getDecoder().decode(content));

                        ServerMain.fileSystemManager.modifyFileLoader(
                                received_document.getString("pathName"),
                                r.fd.getString("md5"),
                                r.fd.getLong("lastModified"));

                        ServerMain.fileSystemManager.writeFile(
                                received_document.getString("pathName"),
                                bf,
                                received_document.getLong("position"));

                        if(ServerMain.fileSystemManager.checkWriteComplete(received_document.getString("pathName"))){
                            return "ok";
                        }else{

                            ServerMain.fileSystemManager.cancelFileLoader(received_document.getString("pathName"));

                            r.position = 0;
                            r.length = r.fd.getLong("fileSize");
                            return r.fileByteRequest();
                        }

                    }

                }else if(command.equals("FILE_DELETE_RESPONSE") ||
                        command.equals("FILE_MODIFY_RESPONSE") ||
                        command.equals("FILE_CREATE_RESPONSE") ||
                        command.equals("DIRECTORY_CREATE_RESPONSE") ||
                        command.equals("DIRECTORY_DELETE_RESPONSE") ){
   
                    return "ok";

                }else{
                    r.message = "message must contain a command field as string";
                    System.out.println("invalid protocol is " + command);
                    System.out.println(r.message);
                    return r.invalidProtocol();
                }

                }else{
                r.message = "message must contain a command field as string";
                 System.out.println("invalid protocol is " + command);
                 System.out.println(r.message);
                return r.invalidProtocol();
            }
            }
        catch (ClassCastException e){
		return "ok";
           } 
        }
    }
}

