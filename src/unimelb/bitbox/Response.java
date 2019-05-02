package unimelb.bitbox;

import unimelb.bitbox.util.Document;
import unimelb.bitbox.util.FileSystemManager;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;

public class Response {

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




    public Response(Document received_document) {
        this.received_document = received_document;
        this.fd = Document.parse(received_document.getString("fileDescriptor"));
        this.command = received_document.getString("command");

    }

    public String createMessage(){
        command = command.replace("REQUEST","RESPONSE");
        reply.append("command", command);
        reply.append("fileDescriptor",fd.toJson());
        reply.append("pathName",received_document.getString("pathName"));
        reply.append("message",message);
        reply.append("status",status);
        return reply.toJson();
    }

    // FILE_BYTES_REQUEST
    public String fileByteRequest(){

        command = command.replace("REQUEST","RESPONSE");
        reply.append("command", command);
        reply.append("fileDescriptor",fd.toJson());
        reply.append("pathName",received_document.getString("pathName"));
        reply.append("position", position);
        reply.append("length", length);
        return reply.toJson();
    }

    // FILE_BYTES_RESPONSE
    public String fileByteResponse(){

        command = command.replace("REQUEST","RESPONSE");
        reply.append("command", command);
        reply.append("fileDescriptor",fd.toJson());
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
        reply.append("fileDescriptor",fd.toJson());
        reply.append("pathName",received_document.getString("pathName"));

        return reply.toJson();
    }

    // FILE_DELETE_RESPONSE
    public String fileDeleteResponse(){

        command = command.replace("REQUEST","RESPONSE");
        reply.append("command", command);
        reply.append("fileDescriptor",fd.toJson());
        reply.append("pathName",received_document.getString("pathName"));
        reply.append("message",message);
        reply.append("status",status);

        return reply.toJson();
    }

    // FILE_MODIFY_REQUEST
    public String fileModifyRequest(){

        command = command.replace("REQUEST","RESPONSE");
        reply.append("command", command);
        reply.append("fileDescriptor",fd.toJson());
        reply.append("pathName",received_document.getString("pathName"));

        return reply.toJson();
    }

    // FILE_MODIFY_RESPONSE
    public String fileModifyResponse(){

        command = command.replace("REQUEST","RESPONSE");
        reply.append("command", command);
        reply.append("fileDescriptor",fd.toJson());
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

    public String getResponceMessage(){
        return reply.toJson();
    }

    public boolean pathSafe(Document received_document){
        return fm.isSafePathName(received_document.getString("pathName"));
    }

    public boolean nameExist(Document received_document){
        return ! fm.fileNameExists(received_document.getString("pathName"));
    }

    public boolean judgeContent(Document received_document) throws IOException, NoSuchAlgorithmException {
        fm.createFileLoader(received_document.getString("pathName"),fd.getString("md5"),Long.parseLong(fd.getString("fileSize")),Long.parseLong(fd.getString("lastModified")));
        return fm.checkShortcut(received_document.getString("pathName"));
    }



}
