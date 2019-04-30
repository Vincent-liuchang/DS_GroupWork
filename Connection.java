/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package unimelb.bitbox;

import java.io.*;
import java.net.*;

/**
 *
 * @author Administrator
 */
public class Connection extends Thread {
    BufferedReader in;
 BufferedWriter out;
 Socket clientSocket;
 static int i = 0;
 public Connection (Socket aClientSocket) {
 try {
 clientSocket = aClientSocket;
 in = new BufferedReader( new InputStreamReader(clientSocket.getInputStream(), "UTF-8"));
 out =new BufferedWriter( new OutputStreamWriter(clientSocket.getOutputStream(), "UTF-8"));
 this.start();
 } catch(IOException e) {
 System.out.println("Connection:"+e.getMessage());
 }
 }
 public void run(){
 try { // an echo server
     i++;
     String clientMsg = null;
    while((clientMsg = in.readLine()) != null) {
	System.out.println("Message from client " + i + ": " + clientMsg);
	out.write("Server Ack " + clientMsg + "\n");
	out.flush();
	System.out.println("Response sent");
}
 }catch (EOFException e){
 System.out.println("EOF:"+e.getMessage());
 } catch(IOException e) {
 System.out.println("readline:"+e.getMessage());
 } finally{ 
 try {
 clientSocket.close();
 }catch (IOException e){/*close failed*/}
 }
 }
}
