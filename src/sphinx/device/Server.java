package sphinx.device;

import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import lejos.hardware.Sound;
import lejos.hardware.motor.Motor;
import lejos.hardware.motor.NXTRegulatedMotor;

public class Server {

	/**
	 * Starts the server sockets and listens for requests.
	 *
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
		// Attempt to start new server socket.
		try (ServerSocket listener = new ServerSocket(59898)) {
			// Output running state and play sound.
			System.out.println("Server is running");
			Sound.beep();
			
			// Create new thread pool and handle incoming requests.
			ExecutorService pool = Executors.newFixedThreadPool(20);
			while (true) {
				pool.execute(new ClientRuntime(listener.accept()));
			}
		}
	}
	
	/**
	 * Runtime for the client socket connection.
	 */
	private static class ClientRuntime implements Runnable {
		
		/**
		 * The incoming socket connection.
		 *
		 * @var Socket
		 */
		private Socket socket;
		
		/**
		 * The incoming data connection.
		 *
		 * @var Scanner
		 */
		private Scanner input;
		
		/**
		 * The outgoing data connection.
		 *
		 * @var Scanner
		 */
		private PrintWriter output;
		
		/**
		 * The vehicle motor connections.
		 *
		 * @var NXTRegulatedMotor
		 */
		public NXTRegulatedMotor left = Motor.A;
		public NXTRegulatedMotor right = Motor.D;
		public NXTRegulatedMotor pickUp = Motor.B;
		public NXTRegulatedMotor front = Motor.C;


		/**
		 * Saves the socket on the instance.
		 *
		 * @param socket
		 */
		public ClientRuntime(Socket socket) {
			this.socket = socket;
		}
		
		/**
		 * Handle incoming socket requests.
		 */
		@Override
		public void run() {
			// Output connect state and play sound.
			System.out.println("Client connected");
			Sound.beep();
			
			// Attempt to handle socket requests.
			try {
				// Prepare input and output streams.
				this.input = new Scanner(this.socket.getInputStream());
				this.output = new PrintWriter(this.socket.getOutputStream(), true);
				
				// Start socket infinity loop.
				while (true) {
					// Read in the incoming data line.
					String str = this.input.nextLine();
					
					//  Check and output if collecting is stalled.
					if (this.front.isStalled()) {
						this.output.println("stalled outer");
					}
					
					if (this.pickUp.isStalled()) {
						this.output.println("stalled inner");
					}
					
					// Skip if missing command and split parameters.
					if (str == "") return;
					String[] cmd = str.split(" ");
					
					// Switch the found primary command.
					switch (cmd[0]) {
					
						// Handle move <speed> command.
						case "move":
							this.move(Integer.parseInt(cmd[1]));
							break;
						
						// Handle turn <degree, speed> command.
						case "turn":
							this.turn(Integer.parseInt(cmd[1]), Integer.parseInt(cmd[2]));
							break;
						
						// Handle collect <pickup spped, front speed> command.
						case "collect":
							this.collect(Integer.parseInt(cmd[1]), Integer.parseInt(cmd[2]));
							break;
						
						// Handle beeping command.
						case "beep":
							Sound.beep();
							break;
							
					}
				}
			} catch (Exception e) {
				// Show disconnection message.
				System.out.println("Client disconnect");
			} finally {
				// Disconnct streams if connected.
				if (this.input != null) this.input.close();
				if (this.output != null) this.output.close();
				
				// Attempt to close the socket.
				try {
					this.socket.close();
				} catch (Exception e) {
					System.out.println("Socket crashed");
				}
			}
		}
		
		/**
		 * Set the motor movement speed.
		 *
		 * @param speed
		 */
		public void move(int speed) // Setting speed of vehicle
		{
			// Set movement speed.
			left.setSpeed(speed);
			right.setSpeed(speed);
			
			// Handle direction based on speed.
			if (speed > 0) {
				left.backward();
				right.backward();
			} else if (speed < 0) {
				left.forward();
				right.forward();
			} else {
				left.stop();
				right.stop();
			}
		}

		/**
		 * Turns the motors by degrees.
		 *
		 * @param deg
		 * @param speed
		 * @throws InterruptedException
		 */
		public void turn(int deg, int speed) throws InterruptedException { // Turns the vehicle
			// Set movement speed.
			left.setSpeed(speed);
			right.setSpeed(speed);
			
			// Check requested direction.
			if (deg > 0) {
				// Turn vehicle right.
				left.backward();
				right.forward();
			} else if (deg < 0) {
				// Turn vehicle left.
				left.forward();
				right.backward();
			}
		}

		/**
		 * Set the collection motors movement speed.
		 *
		 * @param pickUpSpeed
		 * @param frontSpeed
		 */
		public void collect(int pickUpSpeed, int frontSpeed) { // Collects/empty balls
			// Set movement speed.
			front.setSpeed(frontSpeed);
			pickUp.setSpeed(pickUpSpeed);

			// Handle direction based on speed.
			if (pickUpSpeed > 0) {
				// Collects balls to vehicle.
				front.forward();
				pickUp.forward();
			} else if (pickUpSpeed < 0) {
				// Empty balls from vehicle.
				front.backward();
				pickUp.backward();
			} else {
				// Stop collecting balls on vehicle.
				front.stop();
				pickUp.stop();
			}
		}
		
	}
	
}