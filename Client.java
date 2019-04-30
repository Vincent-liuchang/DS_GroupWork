/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package unimelb.bitbox;
import java.io.*;
import java.net.*;
import java.util.Scanner;
/**
 *
 * @author Administrator
 */
public class Client extends Thread{
    
    private int port;
    public Client(int port) {
        this.port = port;
    }
    @Override
    public void run() {
 Socket s = null;
 try{
 int serverPort = port;
 s = new Socket("10.12.18.206", serverPort); 
 System.out.println("Connection Established");
 BufferedReader in = new BufferedReader(new InputStreamReader(s.getInputStream(), "UTF-8"));
 BufferedWriter out = new BufferedWriter(new OutputStreamWriter(s.getOutputStream(), "UTF-8"));
 System.out.println("Sending data"); 
 Scanner scanner = new Scanner(System.in);
 String inputStr;
 
 while (!(inputStr = scanner.nextLine()).equals("exit")) {
				
				// Send the input string to the server by writing to the socket output stream
				out.write(inputStr + "\n");
				out.flush();
				System.out.println("Message sent");
				
				// Receive the reply from the server by reading from the socket input stream
				String received = in.readLine(); // This method blocks until there
													// is something to read from the
													// input stream
				System.out.println("Message received: " + received);
			}
 }catch (UnknownHostException e) {
 System.out.println("Socket:"+e.getMessage());
 }catch (EOFException e){
 System.out.println("EOF:"+e.getMessage());
 }catch (IOException e){
 System.out.println("readline:"+e.getMessage());
 }finally {
 if(s!=null) try {
 s.close();
 }catch (IOException e){
 System.out.println("close:"+e.getMessage());
 }
 }
 }
}
