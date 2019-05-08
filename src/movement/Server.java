package movement;

import java.io.*;
import java.net.*;

public class Server {
	public static final int port = 1234;
	
	public static void main(String[] args) throws IOException {
		
			//listen for clients and accept incoming requests
			ServerSocket server = new ServerSocket(port);
			System.out.println("Awaiting client..");
			
			Socket client = server.accept();
			System.out.println("Connected");
			
			//receive commands from client
			DataInputStream dataIn = new DataInputStream(client.getInputStream());
			
			String str = "";
			while(!str.equals("x")){
				try {
					str = dataIn.readUTF();
					System.out.println(str);
				} catch(IOException i){
					System.out.println(i);
				}
			}
			System.out.println("Closing connection");
			
			//clean up
			dataIn.close();
			server.close();
	}
}