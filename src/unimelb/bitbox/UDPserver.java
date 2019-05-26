package unimelb.bitbox;

import unimelb.bitbox.util.Document;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;

public class UDPserver extends Thread{
    private int port;
    private ArrayList<DatagramPacket> clientRequests = new ArrayList<DatagramPacket>();
    private DatagramSocket serverSocket;

    public UDPserver(int port) {
        try {
            this.port = port;
            serverSocket = new DatagramSocket(port);
        } catch (SocketException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        try {
            while(true){
                byte buffer[] = new byte[15000];
                DatagramPacket request = new DatagramPacket(buffer, buffer.length);
                System.out.println("Server waiting");
                serverSocket.receive(request);
                clientRequests.add(request);
                InetAddress host = request.getAddress();

                String received  =  new String(request.getData()).trim();
                received = received +"\n";
                System.out.println("udp server received"+received);
                Document received_message = Document.parse(received);
                System.out.println(received_message.toJson());
                String response = new Peer().operation(received_message);
                System.out.println("udp server's response"+response);

                if(response.contains("longgenb1995")){
                    String response1 = response.split("longgenb1995")[0];
                    String response2 = response.split("longgenb1995")[1];
                    this.send(response1,host);
                    this.send(response2,host);
                }
                else if (!response.equals("ok")) {
                    this.send(response,host);
                }
            }
        } catch (ClassCastException | IOException | NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
    }
    public void sendtoClient(String message){
            for(DatagramPacket a:clientRequests){
                this.send(message,a.getAddress());
        }
    }

    public void send(String message,InetAddress host){
        try {
            byte[] buffer = message.getBytes();
            DatagramPacket request = new DatagramPacket(buffer, buffer.length, host, port);
            serverSocket.send(request);
            System.out.println("server sent"+message);
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }
}