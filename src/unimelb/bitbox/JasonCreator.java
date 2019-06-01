package unimelb.bitbox;

import unimelb.bitbox.util.Document;
import unimelb.bitbox.util.FileSystemManager;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

public class JasonCreator {

    private FileSystemManager fm = ServerMain.fileSystemManager;
    private Document reply;
    protected Document received_document;
    protected Document fd;
    private String command;
    protected String message;
    protected boolean status;
    protected int position;
    protected long length;
    protected String content;
    protected String aes;
    RSAPublicKey rsapub;


    public JasonCreator(Document received_document) {
        this.received_document = received_document;
        if (received_document.toJson().contains("fileDescriptor")){

            this.fd = (Document)received_document.get("fileDescriptor");
        }
        this.command = received_document.getString("command");
        reply = new Document();

    }

    public String createMessage(){
        command = command.replace("REQUEST","RESPONSE");
        reply.append("command", command);
        reply.append("fileDescriptor",fd);
        reply.append("pathName",received_document.getString("pathName"));
        reply.append("message",message);
        reply.append("status",status);
        return reply.toJson();
    }


    public String invalidProtocol(){
        command = "INVALID_PROTOCOL";
        reply.append("command", command);
        reply.append("message", message);

        return reply.toJson();


    }

    // FILE_BYTES_REQUEST
    public String fileByteRequest(){
    	reply = new Document();
        command = "FILE_BYTES_REQUEST";
        reply.append("command", command);
        reply.append("fileDescriptor",fd);
        reply.append("pathName",received_document.getString("pathName"));
        reply.append("position", position);
        reply.append("length", length);
        return reply.toJson();
    }

    // FILE_BYTES_RESPONSE
    public String fileByteResponse(){

        command = command.replace("REQUEST","RESPONSE");
        reply.append("command", command);
        reply.append("fileDescriptor",fd);
        reply.append("pathName",received_document.getString("pathName"));
        reply.append("position", position);
        reply.append("length", length);
        reply.append("content", content);
        reply.append("message",message);
        reply.append("status",status);

        return reply.toJson();
    }

    // FILE_DELETE_REQUEST
    public String fileDeleteRequest(){

        command = command.replace("REQUEST","RESPONSE");
        reply.append("command", command);
        reply.append("fileDescriptor",fd);
        reply.append("pathName",received_document.getString("pathName"));

        return reply.toJson();
    }

    // FILE_DELETE_RESPONSE
    public String fileDeleteResponse(){

        command = command.replace("REQUEST","RESPONSE");
        reply.append("command", command);
        reply.append("fileDescriptor",fd);
        reply.append("pathName",received_document.getString("pathName"));
        reply.append("message",message);
        reply.append("status",status);

        return reply.toJson();
    }

    // FILE_MODIFY_REQUEST
    public String fileModifyRequest(){
    	
        command = command.replace("REQUEST","RESPONSE");
        reply.append("command", command);
        reply.append("fileDescriptor",fd);
        reply.append("pathName",received_document.getString("pathName"));

        return reply.toJson();
    }

    // FILE_MODIFY_RESPONSE
    public String fileModifyResponse(){

        command = command.replace("REQUEST","RESPONSE");
        reply.append("command", command);
        reply.append("fileDescriptor",fd);
        reply.append("pathName",received_document.getString("pathName"));
        reply.append("message",message);
        reply.append("status",status);

        return reply.toJson();
    }

    // DIRECTORY_CREATE_REQUEST
    public String directoryCreateRequest(){

        command = command.replace("REQUEST","RESPONSE");
        reply.append("command", command);
        reply.append("pathName",received_document.getString("pathName"));

        return reply.toJson();
    }

    // DIRECTORY_CREATE_RESPONSE
    public String directoryCreateResponse(){

        command = command.replace("REQUEST","RESPONSE");
        reply.append("command", command);
        reply.append("pathName",received_document.getString("pathName"));
        reply.append("message",message);
        reply.append("status",status);

        return reply.toJson();
    }

    public String authorizedResponse() throws NoSuchAlgorithmException, InvalidKeySpecException, NoSuchPaddingException, InvalidKeyException, UnsupportedEncodingException, IllegalBlockSizeException, BadPaddingException, NoSuchProviderException {
        command = command.replace("REQUEST","RESPONSE");
        reply.append("command", command);
        reply.append("AES128", Encryption.RSAencrypt(aes, rsapub));
        reply.append("status", "true");
        reply.append("message", "public key found");
        
        return reply.toJson();
    }
    
    public String notAuthorizedResponse() {
           command = command.replace("REQUEST","RESPONSE");
        reply.append("command", command);
        reply.append("status", "false");
        reply.append("message", "public key not found");
        
        return reply.toJson();
    }
    // DIRECTORY_DELETE_REQUEST
    public String directoryDeleteRequest(){

        command = command.replace("REQUEST","RESPONSE");
        reply.append("command", command);
        reply.append("pathName",received_document.getString("pathName"));

        return reply.toJson();
    }

    // DIRECTORY_DELETE_RESPONSE
    public String directoryDeleteResponse(){

        command = command.replace("REQUEST","RESPONSE");
        reply.append("command", command);
        reply.append("pathName",received_document.getString("pathName"));
        reply.append("message",message);
        reply.append("status",status);

        return reply.toJson();
    }


    public boolean pathSafe(Document received_document){
        if(received_document.getString("pathName").contains(".DS_Store"))
            return false;
        return fm.isSafePathName(received_document.getString("pathName"));
    }

    public boolean nameExist(Document received_document){
        return fm.fileNameExists(received_document.getString("pathName"));
    }

    public boolean judgeContent(Document received_document) throws IOException, NoSuchAlgorithmException {
        fm.createFileLoader(received_document.getString("pathName"),fd.getString("md5"),Long.parseLong(fd.getString("fileSize")),Long.parseLong(fd.getString("lastModified")));
        return fm.checkShortcut(received_document.getString("pathName"));
    }



}
