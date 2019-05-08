package remote.control.lejo.ev3;


import java.io.*;
import java.net.*;
import lejos.hardware.*;
import lejos.hardware.motor.*;
import lejos.hardware.port.*;


public class GolfbotServer extends Thread{
	
	//prepare for connection
	private ServerSocket server = null;
	private Socket socket = null;
	private DataInputStream inStream = null;
	
	//Instantiate Lego motors
	//pickup mechanism (port B feeder, port C small tires)
	private static EV3MediumRegulatedMotor B = new EV3MediumRegulatedMotor(MotorPort.A);
	private static EV3LargeRegulatedMotor C = new EV3LargeRegulatedMotor(MotorPort.C);		
	
	//port D and A controls tracks (wheels)
	private static EV3LargeRegulatedMotor D = new EV3LargeRegulatedMotor(MotorPort.D);			
	private static EV3LargeRegulatedMotor A = new EV3LargeRegulatedMotor(MotorPort.A);			
	
	public GolfbotServer(int port){
		try{
			server = new ServerSocket(port);
			System.out.println("Server started and waiting for client");
			
			socket = server.accept();
			System.out.print("Connected");
		
			String str = "";
			while(!str.equals("exit")){
				try{
					str = inStream.readUTF();	
					System.out.println(str);
				}
				catch(IOException i){
					System.out.println(i);
				}
			}
			System.out.println("Closing connection");
			socket.close();
			inStream.close();
		}
		catch(IOException i){
			System.out.println(i);
		}
	}
	
	public static void main(String[] args){
		GolfbotServer server = new GolfbotServer(7360);
	}
}
