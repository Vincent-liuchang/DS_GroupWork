package unimelb.bitbox;

import unimelb.bitbox.util.Document;

import java.io.IOException;
import java.net.*;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;

public class UDPserver extends Thread{
    private int port;
    private ArrayList<DatagramPacket> clientRequests = new ArrayList();
    private ArrayList<InetAddress> onlinePeers = new ArrayList();
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

                if(response.contains("HANDSHAKE_RESPONSE")){
                    onlinePeers.add(host);
                }
                else if(response.contains("longgenb1995")){

                    String re[] = response.split("longgenb1995");
                    for(String i: re ){
                        this.send(i,host);
                        System.out.println("1 UDP packet sent");
                    }
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

        for(InetAddress a: onlinePeers) {
            this.send(message, a);
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