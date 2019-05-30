package unimelb.bitbox;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.logging.Logger;

import unimelb.bitbox.util.Configuration;
import unimelb.bitbox.util.Document;
import unimelb.bitbox.util.HostPort;

public class Peer extends Thread
{
    static protected ServerMain mainServer;
    private static Logger log = Logger.getLogger(Peer.class.getName());
    protected static Synchronize syn;

    public static void main( String[] args ) throws IOException, NumberFormatException, NoSuchAlgorithmException, InterruptedException
    {
        System.setProperty("java.util.logging.SimpleFormatter.format",
                "[%1$tc] %2$s %4$s: %5$s%n");
        log.info("BitBox Peer starting...");
        Configuration.getConfiguration();

        mainServer = new ServerMain();
        syn = new Synchronize(mainServer);

    }

    private int port =  Integer.parseInt(Configuration.getConfigurationValue("port"));
    private String [] peerstring = Configuration.getConfigurationValue("peers").split(" ");
    private ArrayList<String> peers = new ArrayList<String>(Arrays.asList(peerstring));
    protected ArrayList<HostPort> peerHosts = new ArrayList<>();

    private String mode = Configuration.getConfigurationValue("mode");

    protected ArrayList<TCPclient> clientList = new ArrayList<>();
    private TCPserver TCPserver;
    private UDPclient UDPclient;
    private UDPserver UDPserver;
    private int length;

    public void run(){

        for (String i : peers) {
            peerHosts.add(new HostPort(i));
        }
        length = peerHosts.size();

        if (mode.equals("TCP")) {
            TCPserver = new TCPserver(port, this);
            TCPserver.start();

        } else {
            UDPserver = new UDPserver(port, this);
            UDPserver.start();
        }

        System.out.println("peer has peers "+peerHosts.size()+" " +peerHosts.get(0).host + "sercersize "+TCPserver.serverlist.size()+"length"+length);


        while(true){
//            System.out.println("check if there are new peers");

            if (mode.equals("TCP")) {

                if (length != TCPserver.serverlist.size()){
                        System.out.println("start a connecting to other peers");
                        peerHosts.removeAll(TCPserver.serverlist);
                        peerHosts.addAll(TCPserver.serverlist);

                        for (HostPort hostport : peerHosts) {
                            TCPclient client = new TCPclient(hostport, this);
                            clientList.add(client);
                            System.out.println("new client "+client.ip);
                            if (!peerHosts.equals("")) {
                                client.start();
                            }
                        }
                        length = TCPserver.serverlist.size();
                }


            } else {

                if (length != UDPserver.onlinePeers.size()) {
                    peerHosts.removeAll(UDPserver.onlinePeers);
                    peerHosts.addAll(UDPserver.onlinePeers);
                    if (!peerstring[0].equals("") && UDPclient == null) {
                        UDPclient = new UDPclient(peerHosts, this);
                        UDPclient.start();
                    } else if (UDPclient != null) {
                        peerHosts.removeAll(UDPclient.onlinePeers);
                        for (HostPort hostport : peerHosts) {
                            UDPclient.handShake(hostport);
                        }
                    }
                    length = UDPserver.onlinePeers.size();
                }
            }
//            try {
//                Thread.sleep(5*1000);
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
        }

        //configuration into peerHost
    }

    public  void Broadcast(String message){

        if(mode.equals("TCP")){
            for(TCPclient t:clientList) {
                System.out.println("作为client我发了byterequest");
                t.sendtoServer(message);
            }
        }
        else{
            for(HostPort hostpot:peerHosts) {
                UDPclient.sendToServer(hostpot.host,message);
            }
        }

    }

    public void clientToServer(String ip,String message){
        if(mode.equals("TCP")){
            for(TCPclient t:clientList) {
                if(t.ip .equals(ip.replace("localhost",""))) {
                    t.sendtoServer(message);
                }
            }
        }
        else{
            UDPclient.sendToServer(ip,message);
        }
    }

}

