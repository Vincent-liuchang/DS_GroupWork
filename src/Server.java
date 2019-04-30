import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

import javax.net.ServerSocketFactory;

public class Server implements Runnable{

    private Thread t1;
    private String peername;
    // Declare the port number
    private int port;

    // Identifies the user number connected
    private int counter = 0;

    private ArrayList<Socket> SocketList = new ArrayList<Socket>();

    public Server(String peername, int port){
        this.peername = peername;
        this.port = port;
    }

    public void run() {
        ServerSocketFactory factory = ServerSocketFactory.getDefault();
        try(ServerSocket server = factory.createServerSocket(port)){
            Thread.sleep(50);
            System.out.println("Waiting for client connection..");

            // Wait for connections.
            while(true){
                Socket client = server.accept();
                counter++;
                System.out.println("Client "+counter+": Applying for connection!"+ client.getInetAddress());
                SocketList.add(client);

                // Start a new thread for a connection
                Thread t = new Thread(() -> serveClient(client));
                t.start();
            }

        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }


    }

    private void serveClient(Socket client){
        try(Socket clientSocket = client){
            // Input stream
            DataInputStream input = new DataInputStream(clientSocket.
                    getInputStream());
            // Output Stream
            DataOutputStream output = new DataOutputStream(clientSocket.
                    getOutputStream());

            while(true) {
                System.out.println("CLIENT: " + input.readUTF());
                output.writeUTF("This is Vincent's Mac and you are the " + counter + " one to connect, thank you");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
