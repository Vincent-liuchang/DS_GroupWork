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
    protected ArrayList<HostPort> onlinePeers = new ArrayList();
    private DatagramSocket serverSocket;
    private Peer peer;

    public UDPserver(int port,Peer peer) {
        try {
            this.port = port;
            serverSocket = new DatagramSocket(port);
            this.peer =peer;
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
                HostPort host = new HostPort(request.getSocketAddress().toString().replace("/",""));

                String received  =  new String(request.getData()).trim();
                received = received +"\n";
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
                        System.out.println("HandShake Request Accepted");
                        HostPort h = new HostPort(request.getAddress().toString().replace("/",""),(int)((Document)received_message.get("hostPort")).getLong("port"));
                        if(!onlinePeers.contains(h))
                            onlinePeers.add(h);
                        if(!peer.UDPclient.onlinePeers.contains(h)){
                            peer.UDPclient.onlinePeers.add(h);
                            peer.UDPclient.handShake(h);
                        }

                        System.out.println("server have online peers: "+onlinePeers.size());
                        Document handshake = new Document();
                        handshake.append("command", "HANDSHAKE_RESPONSE");
                        HostPort hostport = new HostPort(Configuration.getConfigurationValue("advertisedName"), port);
                        handshake.append("hostPort", hostport.toDoc());
                        System.out.println(hostport.toDoc());
                        this.send(handshake.toJson(),host);
                    }
                }
                else {
                    response = new Operator().operation(received_message);
                    System.out.println(response.length());
                    if (response.contains("longgenb1995")) {
                        System.out.println("收到了filebyterequest");
                        String re[] = response.split("longgenb1995");
                        for (String i : re) {
                            peer.clientToServer(host.host,i);
                        }
                    } else if (!response.equals("ok")) {
                        this.send(response, host);
                    }
                }
            }

        }
        catch (UnknownHostException e){
            System.out.println("not all host online");
        }
        catch (ClassCastException | IOException | NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
    }

    public void send(String message,HostPort host){
        try {

            DatagramPacket request = new DatagramPacket(message.getBytes(), message.getBytes().length, InetAddress.getByName(host.host), host.port);
            serverSocket.send(request);
            System.out.println(message);
        }
        catch (UnknownHostException e){
            System.out.println("not all host online");
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }
}