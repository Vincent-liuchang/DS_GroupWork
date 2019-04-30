public class Peer {
    public Peer(String peername, int port, int port1){
        Thread s1 = new Thread(new Server(peername, port));
        s1.start();
        Thread c1 = new Thread(new Client(peername, port));
        c1.start();
    }
    public static void main(String args[]){
        Peer p1 = new Peer("p1",4396,4396);
//////        Peer p2 = new Peer(4397,4396,"p2");
    }
}
