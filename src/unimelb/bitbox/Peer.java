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
        Synchronize syn = new Synchronize(mainServer);
        syn.start();

    }

    private int port =  Integer.parseInt(Configuration.getConfigurationValue("port"));
    private String [] peerstring = Configuration.getConfigurationValue("peers").split(" ");
    private ArrayList<String> peers = new ArrayList<String>(Arrays.asList(peerstring));
    private String mode = Configuration.getConfigurationValue("mode");
    private TCPclient TCPclient;
    private TCPserver TCPserver;
    private UDPclient UDPclient;
    private UDPserver UDPserver;


    public  void start(){
        if(mode.equals("TCP")){
            TCPclient = new TCPclient(peers, port);
            TCPserver = new TCPserver(port);
            TCPserver.start();
            if(!peerstring[0].equals("")) {
                TCPclient.start();
            }
        }
        else{
            UDPserver = new UDPserver(port);
            UDPclient = new UDPclient(peers,port);

            UDPclient.start();
            UDPserver.start();
        }


    }

    public  void sentToOtherPeers(String message){

        if(mode.equals("TCP")){
            TCPclient.sendtoServer(message);
            TCPserver.sendtoClient(message);
        }
        else{
            UDPclient.sendtoServer(message);
        }

    }


    public String operation(Document received_document) throws IOException, NoSuchAlgorithmException {
        if(received_document.getString("command").equals("HANDSHAKE_RESPONSE")){
            // receive command = handshake_response, from TCPclient
            return "HandShakeComplete";
        }
        else {

               Response r = new Response(received_document);
           
            String command = received_document.getString("command");

            if(command.contains("REQUEST")) {
                if (command.equals("FILE_CREATE_REQUEST")) {

                    if(r.pathSafe(received_document) && ! r.nameExist(received_document) && ServerMain.fileSystemManager.createFileLoader
                            (received_document.getString("pathName"),
                            r.fd.getString("md5"),
                            r.fd.getLong("fileSize"),
                            r.fd.getLong("lastModified"))){

                        r.message = "File Create request received and byte buffer request sent";
                        r.status = true;

                        r.position = 0;
                        long length = r.fd.getLong("fileSize");
                        int blocksize = (int)Long.parseLong(Configuration.getConfigurationValue("blockSize"));
                        String returnMessage = r.createMessage();

                        long i = length/blocksize + 1;

                        if(i>1){
                            System.out.println("blocksize is"+ blocksize);
                            System.out.println("total length: " + length);
                        }
                        for(int j = 0; j<(int)i ; j++){
                            r.position = j * blocksize;
                            r.length = Math.min(blocksize,length-j*blocksize);
                            returnMessage += "longgenb1995";
                            returnMessage += r.fileByteRequest();
                            System.out.println("generate"+(j+1)+" file byte request, position is: "+r.position+"length is:"+r.length);
                        }
                        createOrModify = true;
                        System.out.println(r.message);
                        return returnMessage;

                    }else{
                        r.message = "file create request received and path not safe";
                        r.status = false;
                        System.out.println(r.message);

                        return r.createMessage();
                    }
                }else if (command.equals("FILE_MODIFY_REQUEST")) {

                    if(r.pathSafe(received_document) && r.nameExist(received_document) && ServerMain.fileSystemManager.modifyFileLoader
                            (received_document.getString("pathName"),
                            r.fd.getString("md5"),
                            r.fd.getLong("lastModified"))){

                            r.message = "modify file loader ready";
                            r.status = true;

                            r.position = 0;
                            r.length = r.fd.getLong("fileSize");
                            long length = r.fd.getLong("fileSize");
                            int blocksize = (int)Long.parseLong(Configuration.getConfigurationValue("blockSize"));
                            String returnMessage = r.fileModifyResponse();

                            long i = length/blocksize + 1;

                            if(i>1){
                                System.out.println("blocksize is"+ blocksize);
                                System.out.println("total length: " + length);
                            }

                            for(int j = 0; j<(int)i ; j++){
                                r.position = j * blocksize;
                                r.length = Math.min(blocksize,length-j*blocksize);
                                returnMessage += "longgenb1995";
                                returnMessage += r.fileByteRequest();
                                System.out.println("generate"+(j+1)+" file byte request, position is: "+r.position+"length is:"+r.length);
                            }

                            createOrModify = false;
                            System.out.println(r.message);
                            return returnMessage;

                    }else{
                        r.message = "Path Not Safe or File Not Exists";
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

                    r.content = bf;
                    r.message = "successfully read";
                    r.status = true;
                    r.position = (int)received_document.getLong("position");
                    r.length = received_document.getLong("length");
                    
                    System.out.println("received a file byte request" + "Position:" + r.position);
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
                    return r.directoryDeleteResponse();
                }else{

                    r.message = "message must contain a command field as string";
                      System.out.println("invalid protocol is " + command);
                      
                    System.out.println(r.message);
                    return r.invalidProtocol();
                }


            }else if(command.contains("RESPONSE")){

                if(command.equals("FILE_BYTES_RESPONSE")){

                            String content = received_document.getString("content");

                            ByteBuffer bf = ByteBuffer.wrap(Base64.getDecoder().decode(content));

                            ServerMain.fileSystemManager.writeFile(
                                    received_document.getString("pathName"),
                                    bf,
                                    received_document.getLong("position"));

                            if (ServerMain.fileSystemManager.checkWriteComplete(received_document.getString("pathName"))) {
                                return "ok";
                                //返回一个传输成功完成的response 进行后续处理

                            } else if (r.length == Long.parseLong(Configuration.getConfigurationValue("blockSize"))){
                                System.out.println("transmitting");
                                return "ok";
                            }
                            else{
                                System.out.println("Something goes wrong for transmitting, need retransmit");
                                ServerMain.fileSystemManager.cancelFileLoader(received_document.getString("pathName"));
                                return "ok";
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
    }
}

