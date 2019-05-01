package remote.control.lejo.ev3;

import java.io.DataOutputStream;
import java.net.Socket;
import java.io.*;
import java.net.*;

public class PCClient {

	//prepare for connection and data flow
	private Socket socket = null;					//client socket
	private DataInputStream inStream = null;		//terminal input
	private DataOutputStream outStream = null;  	//data stream to server
	
	//client constructor to accept connection
	public PCClient(String ip, int port){
		try{
			socket = new Socket(ip, port);
			System.out.println("Connected");
			
			inStream = new DataInputStream(System.in);
			outStream = new DataOutputStream(socket.getOutputStream());
		}
		catch(UnknownHostException u) {
			System.out.println(u);
		}
		catch(IOException i){
			System.out.println(i);
		}
		
		String str = "";		//string to read from terminal
		
		//until 'exit' keep connection open and send data stream from terminal to server
		while(!str.equals("exit")){
			try{
				str = inStream.readUTF();	
				outStream.writeUTF(str);
			} 
			catch(IOException i){
				System.out.println(i);
			}
		}
		
		// close the connection 
        try
        { 
            inStream.close(); 
            outStream.close(); 
            socket.close(); 
        } 
        catch(IOException i) 
        { 
            System.out.println(i); 
        } 
	}
	
	public static void main(String[] args) {
		PCClient pc = new PCClient("192.168.43.44", 7360); 
	}

}