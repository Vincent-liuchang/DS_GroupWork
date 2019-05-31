package unimelb.bitbox;

import unimelb.bitbox.util.Configuration;
import unimelb.bitbox.util.Document;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

public class Operator {

    public String operation(Document received_document) throws IOException, NoSuchAlgorithmException,ClassCastException {
        synchronized (this) {
            if (received_document.getString("command").equals("HANDSHAKE_RESPONSE")) {
                // receive command = handshake_response, from TCPclient
                return "HandShakeComplete";
            } else {

                JasonCreator r = new JasonCreator(received_document);

                String command = received_document.getString("command");

                if (command.contains("REQUEST")) {
                    if (command.equals("FILE_CREATE_REQUEST")) {

                        if (r.pathSafe(received_document) && !r.nameExist(received_document)) {

                            r.status = ServerMain.fileSystemManager.createFileLoader
                                    (received_document.getString("pathName"),
                                            r.fd.getString("md5"),
                                            r.fd.getLong("fileSize"),
                                            r.fd.getLong("lastModified"));

                            r.message = "File Create request received and byte buffer request sent";
//                            r.status = true;

                            r.position = 0;
                            long length = r.fd.getLong("fileSize");
                            int blocksize = (int) Long.parseLong(Configuration.getConfigurationValue("blockSize"));

                            if (Configuration.getConfigurationValue("mode").equals("UDP")) {
                                blocksize = Math.min(blocksize, 8192);
                            }

                            String returnMessage = r.createMessage();

                            if(r.status){
                                long i = length / blocksize + 1;

                                if (i > 1) {
                                    System.out.println("blocksize is" + blocksize);
                                    System.out.println("total length: " + length);
                                }
                                for (int j = 0; j < (int) i; j++) {
                                    r.position = j * blocksize;
                                    r.length = Math.min(blocksize, length - j * blocksize);
                                    returnMessage += "longgenb1995";
                                    returnMessage += r.fileByteRequest();
                                    System.out.println("generate" + (j + 1) + " file byte request, position is: " + r.position + "length is:" + r.length);
                                }
                                System.out.println(r.message);
                            }

                            return returnMessage;

                        } else {
                            r.message = "file create request received, path not safe or file exists";
                            r.status = false;
                            System.out.println(r.message);

                            return r.createMessage();
                        }
                    } else if (command.equals("FILE_MODIFY_REQUEST")) {

                        if (r.pathSafe(received_document) && r.nameExist(received_document)) {


                            ServerMain.fileSystemManager.modifyFileLoader
                                    (received_document.getString("pathName"),
                                            r.fd.getString("md5"),
                                            r.fd.getLong("lastModified"));

                            r.message = "modify file loader ready";
                            r.status = true;

                            r.position = 0;
                            r.length = r.fd.getLong("fileSize");
                            long length = r.fd.getLong("fileSize");
                            int blocksize = (int) Long.parseLong(Configuration.getConfigurationValue("blockSize"));
                            String returnMessage = r.fileModifyResponse();

                            long i = length / blocksize + 1;

                            if (i > 1) {
                                System.out.println("blocksize is" + blocksize);
                                System.out.println("total length: " + length);
                            }

                            for (int j = 0; j < (int) i; j++) {
                                r.position = j * blocksize;
                                r.length = Math.min(blocksize, length - j * blocksize);
                                returnMessage += "longgenb1995";
                                returnMessage += r.fileByteRequest();
                                System.out.println("generate" + (j + 1) + " file byte request, position is: " + r.position + "length is:" + r.length);
                            }

                            System.out.println(r.message);
                            return returnMessage;

                        } else {
                            r.message = "Path Not Safe or File Not Exists";
                            r.status = false;

                            System.out.println(r.message);

                            return r.createMessage();
                        }
                    } else if (command.equals("FILE_BYTES_REQUEST")) {


                        ByteBuffer byteBuffer = ServerMain.fileSystemManager.readFile(
                                r.fd.getString("md5"),
                                received_document.getLong("position"),
                                received_document.getLong("length"));


                        String bf = Base64.getEncoder().encodeToString(byteBuffer.array());

                        r.content = bf;
                        r.message = "successfully read";
                        r.status = true;
                        r.position = (int) received_document.getLong("position");
                        r.length = received_document.getLong("length");

                        System.out.println("received a file byte request" + "Position:" + r.position);
                        return r.fileByteResponse();

                    } else if (command.equals("FILE_DELETE_REQUEST")) {

                        if (r.pathSafe(received_document) && r.nameExist(received_document)) {

                            ServerMain.fileSystemManager.deleteFile(
                                    received_document.getString("pathName"),
                                    r.fd.getLong("lastModified"),
                                    r.fd.getString("md5"));
                            r.status = true;
                            r.message = "file delete succeed";

                        } else {
                            r.status = false;
                            r.message = "file delete failed";

                        }

                        System.out.println(r.message);
                        return r.fileDeleteResponse();
                    } else if (command.equals("DIRECTORY_CREATE_REQUEST")) {
                        if (!ServerMain.fileSystemManager.dirNameExists(received_document.getString("pathName"))) {

                            ServerMain.fileSystemManager.makeDirectory(received_document.getString("pathName"));
                            r.message = "directory create succeed";
                            r.status = true;
                        } else {
                            r.message = "directory create failed";
                            r.status = false;
                        }
                        System.out.println(r.message);
                        return r.directoryCreateResponse();

                    } else if (command.equals("DIRECTORY_DELETE_REQUEST")) {

                        if (r.pathSafe(received_document) &&
                                ServerMain.fileSystemManager.dirNameExists(received_document.getString("pathName"))
                        ) {

                            ServerMain.fileSystemManager.deleteDirectory(received_document.getString("pathName"));
                            r.message = "directory delete succeed";
                            r.status = true;
                        } else {
                            r.message = "directory delete failed";
                            r.status = false;
                        }

                        System.out.println(r.message);
                        return r.directoryDeleteResponse();
                    }
//                     else if(command.equals("AUTH_REQUEST")) {
//                        if(Configuration.getConfigurationValue("authorized_keys").contains(received_document.getString("identity")))
//                            
//                    }
                        else {

                        r.message = "message must contain a command field as string";
                        System.out.println("invalid protocol is " + command);

                        System.out.println(r.message);
                        return r.invalidProtocol();
                    }


                } else if (command.contains("RESPONSE")) {

                    if (command.equals("FILE_BYTES_RESPONSE")) {

                        String content = received_document.getString("content");

                        ByteBuffer bf = ByteBuffer.wrap(Base64.getDecoder().decode(content));

                        ServerMain.fileSystemManager.writeFile(
                                received_document.getString("pathName"),
                                bf,
                                received_document.getLong("position"));

                        if (ServerMain.fileSystemManager.checkWriteComplete(received_document.getString("pathName"))) {
                            return "file write done";
                            //返回一个传输成功完成的response 进行后续处理

                        } else {
                            return "transmitting";
                        }

                    } else if (command.equals("FILE_DELETE_RESPONSE") ||
                            command.equals("FILE_MODIFY_RESPONSE") ||
                            command.equals("FILE_CREATE_RESPONSE") ||
                            command.equals("DIRECTORY_CREATE_RESPONSE") ||
                            command.equals("DIRECTORY_DELETE_RESPONSE")) {

                        return "ok";

                    } else {
                        System.out.println("received an unknown command: " + command);
                        return "ok";
                    }

                } else {
                    r.message = "message must contain a command field as string";
                    System.out.println("invalid protocol is " + command);
                    System.out.println(r.message);
                    return r.invalidProtocol();
                }

            }
        }
    }
    public String deOperation(Document received_document) throws IOException, NoSuchAlgorithmException,ClassCastException {
        synchronized (this) {
        	Document returnDocument  = new Document(); 
            String command = received_document.getString("command");

            System.out.println(command);
                if (command.equals("FILE_CREATE_RESPONSE")) { 
                	returnDocument.append("command", "FILE_CREATE_REQUEST");
        			
        			returnDocument.append("fileDescriptor",(Document)received_document.get("fileDescriptor"));
        			returnDocument.append("pathName",received_document.getString("pathName"));
        			String message = returnDocument.toJson();
        			return message;
                } 
                else if(command.equals("FILE_MODIFY_RESPONSE")) {
                	returnDocument.append("command", "FILE_MODIFY_REQUEST");
        			
                	returnDocument.append("fileDescriptor",(Document)received_document.get("fileDescriptor"));
        			returnDocument.append("pathName",received_document.getString("pathName"));
        			String message = returnDocument.toJson();
        			return message;
                } else if(command.equals("FILE_DELETE_RESPONSE")) {

                	returnDocument.append("command", "FILE_DELETE_REQUEST");
                	returnDocument.append("fileDescriptor",(Document)received_document.get("fileDescriptor"));
        			returnDocument.append("pathName",received_document.getString("pathName"));
        			String message = returnDocument.toJson();
        			return message;
                } else if (command.equals("DIRECTORY_CREATE_RESPONSE")) {

                	returnDocument.append("command", "DIRECTORY_CREATE_REQUEST");
        			
                	returnDocument.append("pathName",received_document.getString("pathName"));
        			String message = returnDocument.toJson();
        			return message;
                } else if (command.equals("DIRECTORY_DELETE_RESPONSE")) {
                	returnDocument.append("command", "DIRECTORY_DELETE_REQUEST");
        			
                	returnDocument.append("pathName",received_document.getString("pathName"));
        			String message = returnDocument.toJson();
        			return message;
                } else {
                	
                	returnDocument.append("command",received_document.getString("command"));
                    
                    returnDocument.append("fileDescriptor",(Document)received_document.get("fileDescriptor"));
                    returnDocument.append("pathName",received_document.getString("pathName"));
                    returnDocument.append("position",received_document.getInteger("position"));
                    returnDocument.append("lenght",received_document.getLong("length"));
                    String message = returnDocument.toJson();
        			return message;
                }
            
        }
    }                 
}
