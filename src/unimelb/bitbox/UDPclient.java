package unimelb.bitbox;
import unimelb.bitbox.util.Configuration;
import unimelb.bitbox.util.Document;
import unimelb.bitbox.util.HostPort;

import java.io.IOException;
import java.net.*;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.concurrent.CopyOnWriteArrayList;


public class UDPclient extends Thread {

    private ArrayList<HostPort> peers;
    protected ArrayList<HostPort> onlinePeers = new ArrayList<>();
    private DatagramSocket clientSocket;
    private Peer peer;
    private CopyOnWriteArrayList<DatagramPacket> packetList = new CopyOnWriteArrayList<>();

    public UDPclient(ArrayList<HostPort> peers, Peer peer) {
        this.peers = peers;
        this.peer =peer;
    }

    @Override
    public void run() {
        try {
            clientSocket = new DatagramSocket();
            Thread t = new Thread(() -> resent());
            t.start();

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
                Document received_message = Document.parse(received);
                System.out.println(received_message.toJson());
                String response = new Operator().operation(received_message);

                this.deleteSuccessPacket(reply.getAddress(),received_message);

                if (response.equals("HandShakeComplete")) {
                    System.out.println("HandShake Response Received, connected");

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
            HostPort hostport = new HostPort(Configuration.getConfigurationValue("advertisedName"), Integer.parseInt(Configuration.getConfigurationValue("udpPort")));
            handshake.append("hostPort", hostport.toDoc());

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
            if(message.contains("_")) {
                HostPort hostPort = null;
                onlinePeers.removeAll(peer.peerHosts);
                onlinePeers.addAll(peer.peerHosts);
                for (HostPort host : onlinePeers) {
                    if (host.host.equals(ip))
                        hostPort = host;
                }
                if (hostPort != null) {
                    DatagramPacket request = new DatagramPacket(message.getBytes(), message.getBytes().length, InetAddress.getByName(hostPort.host), hostPort.port);
                    clientSocket.send(request);


                    if (!message.contains("FILE_CREATE_RESPONSE")&&packetExist(request)) {
                        packetList.add(request);
                        System.out.println(packetList.size());
                        System.out.println("Added!!!!! Current size is "+packetList.size());
                    }

                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public Boolean packetExist(DatagramPacket p){
        for(DatagramPacket dp: packetList){
            if(p.getAddress().equals(dp.getAddress())&& new String(p.getData()).equals(new String(dp.getData()))){
                return true;
            }
        }
        return false;
    }

    public void deleteSuccessPacket(InetAddress ip, Document received_document){
        try{
            for (DatagramPacket dp : packetList) {
                if (ip.equals(dp.getAddress()) && new String(dp.getData()).equals(new Operator().deOperation(received_document))) {
                    packetList.remove(dp);
                }
            }
        }catch(Exception e){
            e.printStackTrace();
        }
        System.out.println("Deleted!!!! Current size is "+packetList.size());
    }

    public void resent(){
        while(true) {
            System.out.println("Current size is "+packetList.size());
            for (DatagramPacket d : packetList) {
                System.out.println(d.getData().toString());
                this.sendToServer(d.getAddress().getHostName(), new String(d.getData()));
            }
            System.out.print("resent done");
            try {
                Thread.sleep(Integer.parseInt(Configuration.getConfigurationValue("resentInterval")) * 1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

}
