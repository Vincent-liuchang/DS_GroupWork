package unimelb.bitbox;
import unimelb.bitbox.util.Document;
import unimelb.bitbox.util.HostPort;

import java.io.IOException;
import java.net.*;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;


public class UDPclient extends Thread {

    private int port;
    private ArrayList<String> peers;
    private ArrayList<String> onlinePeers = new ArrayList<>();
    private DatagramSocket clientSocket;

    public UDPclient(ArrayList<String> peers, int port) {
        this.peers = peers;
        this.port = port;

        try {
            clientSocket = new DatagramSocket();
        } catch (SocketException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        try {
            byte[] buffer = new byte[15000];
            DatagramPacket reply = new DatagramPacket(buffer,buffer.length);
            System.out.println("Local clients ready for accept");
            clientSocket.receive(reply);
            InetAddress host = reply.getAddress();

            String received  =  new String(reply.getData()).trim();
            Document received_message = Document.parse(received);
            String response = new Peer().operation(received_message);

            if(response.equals("HandShakeComplete")){
                String ip =reply.getAddress().getHostAddress();
                System.out.println("HandShake Response Received, the server is" + ip);
                onlinePeers.add(ip);
            }
            else if(response.contains("longgenb1995")){
                String re[] = response.split("longgenb1995");
                for(String i: re ){
                    byte[] buf = i.getBytes();
                    DatagramPacket request = new DatagramPacket(buf,buf.length,host,port);
                    clientSocket.send(request);
                    System.out.println("1 UDP packet sent");
                }
            }
            else if (!response.equals("ok")) {
                byte[] buf = response.getBytes();
                DatagramPacket request = new DatagramPacket(buf,buf.length,host,port);
                clientSocket.send(request);
                System.out.println("client sent"+response);
            }

        } catch (IOException | NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch(ClassCastException e){
            System.out.println("Invalid Protocol Received By Local Client");
        }
    }


    public void handShake(String ip){

        try {
            Document handshake = new Document();
            handshake.append("command", "HANDSHAKE_REQUEST");
            HostPort hostport = new HostPort(ip, port);
            handshake.append("hostPort", hostport.toDoc());

            byte[] buffer = handshake.toJson().getBytes();
            InetAddress host = InetAddress.getByName(ip);
            DatagramPacket request = new DatagramPacket(buffer, buffer.length, host, port);
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
            for(String ip: peers){
                byte[] buffer = message.getBytes();
                InetAddress host = InetAddress.getByName(ip);
                DatagramPacket request = new DatagramPacket(buffer,buffer.length,host,port);
                clientSocket.send(request);
                System.out.println("client sent"+message);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
