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
    private ArrayList<HostPort> onlinePeers = new ArrayList<>();
    private DatagramSocket clientSocket;
    private byte[] buffer = new byte[15000];
    private DatagramPacket reply = new DatagramPacket(buffer,buffer.length);

    public UDPclient(ArrayList<HostPort> peers) {
        this.peers = peers;

        try {
            clientSocket = new DatagramSocket();
            for(HostPort ip: peers){
                this.handShake(ip);
            }
        } catch (SocketException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        try {
            System.out.println("Local clients ready for accept");
            while(true) {
                clientSocket.receive(reply);
                InetAddress host = reply.getAddress();

                String received = new String(reply.getData()).trim();
                Document received_message = Document.parse(received);
                String response = new Peer().operation(received_message);

                if (response.equals("HandShakeComplete")) {
                    String ip = reply.getAddress().getHostAddress();
                    System.out.println("HandShake Response Received, the server is" + ip);
                    onlinePeers.add(new HostPort(ip,reply.getPort()));
                } else if (response.contains("longgenb1995")) {
                    String re[] = response.split("longgenb1995");
                    for (String i : re) {
                        byte[] buf = i.getBytes();
                        DatagramPacket request = new DatagramPacket(buf, buf.length, host, reply.getPort());
                        clientSocket.send(request);
                        System.out.println("1 UDP packet sent");
                    }
                } else if (!response.equals("ok")) {
                    byte[] buf = response.getBytes();
                    DatagramPacket request = new DatagramPacket(buf, buf.length, host, reply.getPort());
                    clientSocket.send(request);
                    System.out.println("client sent" + response);
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
            HostPort hostport = new HostPort(clientSocket.getInetAddress().toString(), Integer.parseInt(Configuration.getConfigurationValue("port")));
            handshake.append("hostPort", hostport.toDoc());

            byte[] buffer = handshake.toJson().getBytes();
            InetAddress host = InetAddress.getByName(ip.host);
            DatagramPacket request = new DatagramPacket(buffer, buffer.length, host,ip.port);
            clientSocket.send(request);
            System.out.println("client sent handshake request");
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendtoServer(String message){
        try {
            peers.removeAll(onlinePeers);
            peers.addAll(onlinePeers);
            for(HostPort ip: peers){
                byte[] buffer = message.getBytes();
                DatagramPacket request = new DatagramPacket(buffer,buffer.length,InetAddress.getByName(ip.host),ip.port);
                clientSocket.send(request);
                System.out.println("client sent"+message);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
