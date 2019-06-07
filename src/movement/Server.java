package movement;

import java.io.*;
import java.net.*;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;

import lejos.hardware.Sound;
import lejos.hardware.device.NXTMMX;
import lejos.hardware.lcd.LCD;
import lejos.hardware.motor.Motor;
import lejos.hardware.port.Port;
import lejos.hardware.port.SensorPort;
import lejos.hardware.sensor.EV3TouchSensor;
import lejos.hardware.sensor.EV3UltrasonicSensor;
import lejos.hardware.sensor.SensorMode;
import lejos.remote.ev3.RMIRegulatedMotor;
import lejos.remote.ev3.RemoteEV3;
import lejos.robotics.SampleProvider;
import lejos.utility.Delay;
import lejos.utility.Stopwatch;


public class Server {
	public static final int port = 1234;
	
	public static void main(String[] args) throws IOException {
		
			//listen for clients and accept incoming requests
			ServerSocket server = new ServerSocket(port);
			System.out.println("Awaiting client..");
			
			Socket client = server.accept();
			System.out.println("Connected");
			
			
			MovementTest move = new MovementTest();
			
			//receive commands from client
			DataInputStream dataIn = new DataInputStream(client.getInputStream());
			
			String str = "";
			while(!str.equals("exit")){
				try {
					str = dataIn.readUTF();
						if(str.equals("beep")){
							Sound.beep();
						}
					
					} catch(IOException i){
					System.out.println(i);
				}
			}
			System.out.println("Closing connection");
			
			//clean up
			//dataIn.close();
			server.close();
	}
}