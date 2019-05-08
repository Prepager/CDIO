package movement;
import java.io.*;
import java.net.*;

public class PCClient {
	
	public static void main(String[] args) throws IOException {
		//connect to EV3 server
		String ip = "192.168.43.44";
		
		if(args.length > 0){
			ip = args[0];
		}
		Socket client = new Socket(ip, Server.port);
		System.out.println("Connected");
		
		//communicate with EV3 server
		//DataInputStream dataIn = new DataInputStream(System.in);					//input from terminal	
		DataOutputStream dataOut = new DataOutputStream(client.getOutputStream());	//output to server
		String str = "hello world";
		
		while(!str.equals("x")){
			try {
				//line = dataIn.readUTF();
				dataOut.writeUTF(str);
				dataOut.flush();
				str = "x";
				dataOut.writeUTF(str);
				dataOut.flush();
				
			} catch(IOException i){
				System.out.println(i);
			}
		}
		//clean up
		//dataIn.close();
		dataOut.close();
		client.close();
	}
}
