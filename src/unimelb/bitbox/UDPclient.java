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
    private ArrayList<String> onlinePeers;
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

            if(response.contains("longgenb1995")){
                String response1 = response.split("longgenb1995")[0];
                String response2 = response.split("longgenb1995")[1];
                byte[] buf1 = response1.getBytes();
                DatagramPacket request1 = new DatagramPacket(buf1,buf1.length,host,port);
                clientSocket.send(request1);
                byte[] buf2 = response2.getBytes();
                DatagramPacket request2 = new DatagramPacket(buf2,buf2.length,host,port);
                clientSocket.send(request2);
                System.out.println("client sent"+response1);
                System.out.println("client sent"+response2);
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
            for(String ip:peers){
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
