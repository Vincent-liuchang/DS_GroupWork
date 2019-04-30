import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ConnectException;
import java.net.Socket;
import java.util.Scanner;

public class Client implements Runnable {
    private Thread t1;
    private String peername;
    private int port;

    public Client(String peername, int port){
        this.peername = peername;
        this.port = port;
    }

    public void run(){
        try{
            Socket socket = new Socket("10.98.200.159", port);
            System.out.println("client socket created");
            DataInputStream datain = new DataInputStream(socket.getInputStream());
            DataOutputStream dataout = new DataOutputStream(socket.getOutputStream());
            dataout.writeUTF("this is vincent's macbook");
            System.out.println("client message sent");
            String received = datain.readUTF();
            System.out.println(received);

            Scanner s = new Scanner(System.in);
            String a = "";

            while(! a.equals("stop")){
                a =s.nextLine();
                dataout.writeUTF(a);
                System.out.println("sent");
            }

            s.close();
            socket.close();

        }
        catch(ConnectException e){
            if(e.getMessage() == "Connection refused (Connection refused)")
                run();
        }
        catch(IOException e){
            e.printStackTrace();
        }
    }




}
