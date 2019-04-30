public class Peer {
    public Peer(String peername, int port, int port1){
        Server s1 = new Server(peername, port);
        s1.start();
        Client c1 = new Client(peername, port1);
        c1.start();
    }
    public Peer(int port, int port1, String peername){
        Server s1 = new Server(peername, port);
        s1.start();
        Client c1 = new Client(peername, port1);
        c1.start();
    }
    public static void main(String args[]){
        Peer p1 = new Peer("p1",4396,4397);
        Peer p2 = new Peer("p1",4397,4396);
////        Peer p2 = new Peer(4397,4396,"p2");
    }
}
