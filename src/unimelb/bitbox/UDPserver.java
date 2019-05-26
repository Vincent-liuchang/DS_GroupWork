package unimelb.bitbox;

import unimelb.bitbox.util.Configuration;
import unimelb.bitbox.util.Document;
import unimelb.bitbox.util.HostPort;

import java.io.IOException;
import java.net.*;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;

public class UDPserver extends Thread{
    private int port;
    private ArrayList<DatagramPacket> clientRequests = new ArrayList();
    private ArrayList<HostPort> onlinePeers = new ArrayList();
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
                HostPort host = new HostPort(request.getAddress().toString(),request.getPort());

                String received  =  new String(request.getData()).trim();
                received = received +"\n";
                System.out.println("udp server received"+received);
                Document received_message = Document.parse(received);
                String response;

                if(received_message.getString("command").equals("HANDSHAKE_REQUEST")){
                    if(onlinePeers.size() > Integer.parseInt(Configuration.getConfigurationValue("maximumIncommingConnections"))){
                        Document handshake = new Document();
                        handshake.append("command", "CONNECTION_REFUSED");
                        handshake.append("message", "connection limit reached");
                        ArrayList<Document> peers = new ArrayList<Document>();
                        for (HostPort ip : onlinePeers) {
                            peers.add(ip.toDoc());
                        }
                        handshake.append("peers", peers);
                    }
                    else{
                        System.out.println("HandShake Request Accepted by TCPserver");
                        onlinePeers.add(new HostPort(Document.parse(received_message.getString("hostPort"))));
                        Document handshake = new Document();
                        handshake.append("command", "HANDSHAKE_RESPONSE");
                        HostPort hostport = new HostPort(Configuration.getConfigurationValue("advertisedName"), port);
                        handshake.append("hostPort", hostport.toDoc());
                        this.send(handshake.toJson(),host);
                    }
                }
                System.out.println(received_message.toJson());
                response = new Peer().operation(received_message);
                System.out.println("udp server's response"+response);

                if(response.contains("longgenb1995")){

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

        for(HostPort a: onlinePeers) {
            this.send(message, a);
        }

    }

    public void send(String message,HostPort host){
        try {
            byte[] buffer = message.getBytes();
            DatagramPacket request = new DatagramPacket(buffer, buffer.length, InetAddress.getByName(host.host), host.port);
            serverSocket.send(request);
            System.out.println("server sent"+message);
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }
}