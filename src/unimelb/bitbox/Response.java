package unimelb.bitbox;

import unimelb.bitbox.util.Document;
import unimelb.bitbox.util.FileSystemManager;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;

public class Response {

    private FileSystemManager fm = ServerMain.fileSystemManager;
    private Document reply;
    private Document received_document;
    private Document fd = Document.parse(received_document.get("fileDescriptor").toString());
    private String command = received_document.getString("command");
    protected String message;
    protected String status;

    public Response(Document received_document) {
        this.received_document = received_document;


    }

    public String createMessage(){
        command = command.replace("REQUEST","RESPONSE");
        reply.append("command", command);
        reply.append("fileDesriptor",fd);
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
