package unimelb.bitbox;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.NoSuchAlgorithmException;
import java.util.Scanner;
import java.util.logging.Logger;

import unimelb.bitbox.util.Configuration;
import unimelb.bitbox.util.FileSystemManager;
import unimelb.bitbox.util.FileSystemObserver;
import unimelb.bitbox.util.FileSystemManager.FileSystemEvent;

public class ServerMain implements FileSystemObserver {
	private static Logger log = Logger.getLogger(ServerMain.class.getName());
	protected FileSystemManager fileSystemManager;
	
	public ServerMain(int host) throws NumberFormatException, IOException, NoSuchAlgorithmException {
		fileSystemManager=new FileSystemManager(Configuration.getConfigurationValue("path"),this);
                try {
                     Scanner s = new Scanner(System.in);
                    
                    //server
                    int serverPort = Integer.parseInt(Configuration.getConfigurationValue("port"));
                    ServerSocket listenSocket = new ServerSocket(serverPort);
                    int i = 0;
                        System.out.println("Please choose a peer to connect: ");
                       Client c1 = new Client(s.nextInt());
                       c1.start();
                    while(true) {
                        System.out.println("Server" + Configuration.getConfigurationValue("port")+ "listening for a connection");
                    
            
                        Socket clientSocket = listenSocket.accept();
                        
                        i++;
                        System.out.println("received connection " + i);
                        Connection c = new Connection(clientSocket);
                       
                     
                    }
                }
        catch(IOException e) {
            System.out.println("Listen socket: " + e.getMessage());
        }
	}

	@Override
	public void processFileSystemEvent(FileSystemEvent fileSystemEvent) {
		// TODO: process events
	}
	
}
