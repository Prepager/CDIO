package movement;

import java.io.*;
import java.net.*;

/*public class PCClient {
	
	public static void main(String[] args) throws IOException {
		//connect to EV3 server
		String ip = "192.168.43.44";
		
		if(args.length > 0){
			ip = args[0];
		}
		Socket client = new Socket(ip, 1234);
		System.out.println("Connected");
		
		//communicate with EV3 server
		//DataInputStream dataIn = new DataInputStream(System.in);					//input from terminal	
		DataOutputStream dataOut = new DataOutputStream(client.getOutputStream());	//output to server
		BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
		
		String str = "beep";
		
		while(!str.equals("exit")){
			try {
				//line = dataIn.readUTF();
//				dataOut.writeUTF(str);
//				dataOut.flush();
				str = reader.readLine();
				dataOut.writeUTF(str);
				dataOut.flush();
				
			} catch(IOException i){
				str = "exit";
				client.close();
				//System.exit(1);
			}
		}
		//clean up
		//dataIn.close();
		client.close();
		dataOut.close();
	}
}*/

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

public class PCClient {
    public static void main(String[] args) throws Exception {
    	String ip = "192.168.43.44";
        /*if (args.length != 1) {
            System.err.println("Pass the server IP as the sole command line argument");
            return;
        }*/
        try (Socket socket = new Socket(ip, 59898)) {
            System.out.println("Enter lines of text then Ctrl+D or Ctrl+C to quit");
            Scanner scanner = new Scanner(System.in);
            Scanner in = new Scanner(socket.getInputStream());
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            while (scanner.hasNextLine()) {
                out.println(scanner.nextLine());
                System.out.println(in.nextLine());
            }
        }
    }
}
