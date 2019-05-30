package unimelb.bitbox;
import unimelb.bitbox.util.Configuration;
import unimelb.bitbox.util.Document;
import unimelb.bitbox.util.HostPort;

import java.io.IOException;
import java.net.*;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;


public class UDPclient extends Thread {

    private ArrayList<HostPort> peers;
    protected ArrayList<HostPort> onlinePeers = new ArrayList<>();
    private DatagramSocket clientSocket;
    private Peer peer;

    public UDPclient(ArrayList<HostPort> peers, Peer peer) {
        this.peers = peers;
        this.peer =peer;
    }

    @Override
    public void run() {
        try {
            clientSocket = new DatagramSocket();
            for(HostPort ip: peers){
                this.handShake(ip);
            }
        } catch (SocketException e) {
            e.printStackTrace();
        }
        try {
            System.out.println("Local clients ready for accept");
            while(true) {
                byte[] buffer = new byte[15000];
                DatagramPacket reply = new DatagramPacket(buffer,buffer.length);
                clientSocket.receive(reply);

                String received = new String(reply.getData()).trim();
                System.out.println("mei zhuan de"+received);
                Document received_message = Document.parse(received);
                System.out.println(received_message.toJson());
                String response = new Operator().operation(received_message);


                if (response.equals("HandShakeComplete")) {
                    System.out.println("HandShake Response Received, connected");
                    if(!peer.syn.isAlive()){
                        peer.syn.start();
                        System.out.println("Connected to the peer");
                        System.out.println("Synchronize service start");
                    }
                }  else if (!response.equals("ok")) {
                    System.out.println(response);
                }
            }

        } catch (IOException | NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch(ClassCastException e){
            System.out.println("Invalid Protocol Received By Local Client");
        }
    }


    public void handShake(HostPort ip){

        try {
            Document handshake = new Document();
            handshake.append("command", "HANDSHAKE_REQUEST");
            HostPort hostport = new HostPort(Configuration.getConfigurationValue("advertisedName"), Integer.parseInt(Configuration.getConfigurationValue("port")));
            handshake.append("hostPort", hostport.toDoc().toJson());

            byte[] buffer = handshake.toJson().getBytes();
            InetAddress host = InetAddress.getByName(ip.host);
            DatagramPacket request = new DatagramPacket(buffer, buffer.length, host,ip.port);
            clientSocket.send(request);
            System.out.println("client sent handshake request");
        } catch (UnknownHostException e){
            System.out.println("not all host online");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendToServer(String ip,String message){
        try {

            HostPort hostPort = null;
            onlinePeers.removeAll(peer.peerHosts);
            onlinePeers.addAll(peer.peerHosts);
            for(HostPort host: onlinePeers){
                if(host.host.equals(ip))
                    hostPort = host;
            }
            if(hostPort!= null) {
                System.out.println("client发了");
                System.out.println(message.getBytes().length);
                DatagramPacket request = new DatagramPacket( message.getBytes(),  message.getBytes().length, InetAddress.getByName(hostPort.host), hostPort.port);
                clientSocket.send(request);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
