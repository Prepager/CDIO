package movement;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import lejos.hardware.Sound;
import lejos.hardware.lcd.LCD;
import lejos.hardware.motor.Motor;
import lejos.utility.Delay;
import lejos.hardware.motor.JavaMotorRegulator;

public class Server {

	public static void main(String[] args) throws Exception {
		try (ServerSocket listener = new ServerSocket(59898)) { // Creates server socket/IP port
			System.out.println("Server is running");
			ExecutorService pool = Executors.newFixedThreadPool(20); // Creates thread pool
			Sound.beep();
			pool.execute(new sound());
			while (true) {
				pool.execute(new serverRuntime(listener.accept())); // Creates one instance
			}
		}
	}
	
	private static class sound implements Runnable{
		/*sound(){
			
		}*/

		@Override
		public void run() {
			// TODO Auto-generated method stub
			final File file = new File("Imperial_March.wav");
			Sound.playSample(file, 100);
		}
	}

	private static class serverRuntime implements Runnable { // Implements server logic
		private Socket socket; // Private socket

		serverRuntime(Socket socket) { // Initiate socket
			this.socket = socket;
		}

		@Override
		public void run() { // Runs server
			Movement move = new Movement(); // Creates instance of motor logic
			Sound.beep(); // Client connect notification
			// System.out.println("Connected: " + socket);
			System.out.println("Client connected");
			String str = "";
			try {
				Scanner in = new Scanner(socket.getInputStream()); // Instance of input (client commands)
				PrintWriter out = new PrintWriter(socket.getOutputStream(), true); // Instance of output (to client)
				while (!str.equals("exit")) { // While client has not exited
					str = in.nextLine(); // Reads new command from client
					// out.println(str.toUpperCase()); //sends back command to client
					if (str != "") {
						String[] command = str.split(" ");
						switch (command[0]) {
						case "move": // Moves the vehicle forward, backwards and stop
							move.move(Integer.parseInt(command[1]));
							break;
						case "turn": // Turns the vehicle left or right
							move.turn(Integer.parseInt(command[1]), Integer.parseInt(command[2]));
							break;
						case "stop": // Stops the vehicle
							move.move(0);
						case "collect": // Turns the pickup mekanism on/off
							move.collect(Integer.parseInt(command[1]), Integer.parseInt(command[2]));
							break;
						default: // Stops movement of the car
							move.move(0);
							System.out.println("error command: " + command[0]); // Command does not excist
							break;
						}
					}
					if (move.isStalled()) { // Checks of motor is stalled
						out.println("stalled");
					}
				}
				in.close(); // Closes input
				out.close(); // Closes output
			} catch (Exception e) { // Client disconnect
				System.out.println("Client disconnect");
				// System.out.println("Error:" + socket);
			} finally { // Tries to close socket
				try {
					socket.close();
				} catch (IOException e) {
					System.out.println("Socket didn't close properly");
					// System.out.println("Closed: " + socket);
				}
				move.move(0); // Stops movement
				move.collect(0, 0); // Stop collection
			}
		}
	}
}