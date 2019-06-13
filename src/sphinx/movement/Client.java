package sphinx.movement;

import java.io.InputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Scanner;

import org.opencv.core.Point;

import sphinx.Config;
import sphinx.vision.Vehicle;

public class Client {
	
	/**
	 * The speed the vehicle turns.
	 *
	 * @var int
	 */
	int turnSpeed = Config.Client.turnSpeed;
	
	/**
	 * The speed the vehicle moves.
	 *
	 * @var int
	 */
	int moveSpeed = Config.Client.moveSpeed;
	
	/**
	 * The slow speed when the vehicle moves.
	 *
	 * @var int
	 */
	int slowSpeed = Config.Client.slowSpeed;
	
	/**
	 * The speed for the inner vehicle collection motors.
	 *
	 * @var int
	 */
	int collectInnerSpeed = Config.Client.collectInnerSpeed;
	
	/**
	 * The speed for the outer vehicle collection motors.
	 *
	 * @var int
	 */
	int collectOuterSpeed = Config.Client.collectOuterSpeed;
	
	/**
	 * The dist threshold when the vehicle should slow down.
	 *
	 * @var int
	 */
	int slowThreshold = Config.Client.slowThreshold;
	
	/**
	 * The amount of distance imperfection.
	 *
	 * @var int
	 */
	int distOffset = Config.Client.distOffset;
	
	/**
	 * The amount of degree imperfection.
	 *
	 * @var int
	 */
	int degreeOffset = Config.Client.degreeOffset;
	
	/**
	 * The stalled status of the pickup engine.
	 *
	 * @var Boolean
	 */
	boolean stalled = false;
	
	/**
	 * The running status of the pickup engine.
	 *
	 * @var Boolean
	 */
	boolean collecting = false;
	
	/**
	 * The socket for the connection.
	 *
	 * @var Socket
	 */
	Socket socket;
	
	/**
	 * The incoming stream using for the scanner.
	 *
	 * @var InputStream
	 */
	InputStream stream;
	
	/**
	 * The input stream for the socket.
	 *
	 * @var Scanner
	 */
	Scanner input;
	
	/**
	 * The output stream for the socket.
	 *
	 * @var PrintWriter
	 */
	PrintWriter output;
	
	/**
	 * The current vehicle paths.
	 *
	 * @var List<Point>
	 */
	public ArrayList<Point> targets = new ArrayList<Point>();
	
	/**
	 * Attempt to connect to the EV3 and open stream.
	 */
	public Client() {
		try {
			// Show connection warning.
			System.out.println("Attempting to connect to server!");
			
			// @wip - Create testing targets.
			/*this.targets.add(new Point(100, 140));
			this.targets.add(new Point(480, 140));
			this.targets.add(new Point(480, 360));
			this.targets.add(new Point(100, 360));
			this.targets.add(new Point(250, 250));*/
			
			// Open socket connection.
			this.socket = new Socket(Config.Client.ip, Config.Client.port);

			// Open input and output steams.
			this.stream = this.socket.getInputStream();
			this.input = new Scanner(this.stream);

			this.output = new PrintWriter(this.socket.getOutputStream(), true);
			
			// Show connection complete.
			System.out.println("Successfully connected to server!");
		} catch (Exception e) {
			// Print the exceptions stack.
			e.printStackTrace();
			
			// Show connection failed error.
			System.out.println("Failed to connect to server!");
		}
	}
	
	/**
	 * Handle the movement.
	 *
	 * @param vehicle
	 */
	public void run(Vehicle vehicle) {
		// Skip if socket is unconnected.
		if (this.socket == null) return;
		
		// Stop motors if missing targets.
		if (this.targets.isEmpty()) {
			// Reset motor statuses.
			this.stalled = false;
			this.collecting = false;
			
			// Stop the motors from running.
			this.output.println("move 0");
			this.output.println("collect 0 0");
			return;
		}
		
		// Get the first target or skip.
		Point target = this.targets.get(0);
		
		// Find distance and rotation.
		double distance = this.calculateDistance(vehicle, target);
		double rotation = this.calculateRotation(vehicle, target);
		
		// Handle the movement and collecting.
		//this.handleMovement(distance, rotation);
		this.handleCollecting(distance, rotation);

		// @wip
		System.out.println("> Deg:" + distance + " pixels, Rot:" + rotation + " deg");
		
		// Remove target if below offset.
		if (distance < this.distOffset) {
			this.targets.remove(0);
		}		
	}

	/**
	 * Returns whether or not the vehicle should slow down.
	 *
	 * @param distance
	 * @return boolean
	 */
	private boolean shouldSlow(double distance) {
		return distance < this.slowThreshold;
	}

	/**
	 * Handles the movement mechanism logic.
	 *
	 * @param distance
	 * @param rotation
	 */
	private void handleMovement(double distance, double rotation) {
		// Check if rotation is above imperfection limit.
		if (Math.abs(rotation) > this.degreeOffset) {
			// Turn the vehicle to the found degree.
			this.output.println("turn " + (int) rotation + " " + this.turnSpeed);
			
			// Skip movement while turning.
			return;
		}
		
		// Determine speed based on distance.
		int speed = this.shouldSlow(distance)
			? this.slowSpeed
			: this.moveSpeed;
		
		// Make vehicle move at found speed.
		this.output.println("move " + speed);
	}

	/**
	 * Handles the collecting mechanism logic.
	 *
	 * @param distance
	 * @param rotation
	 */
	private void handleCollecting(double distance, double rotation) {
		// Collect if not active and stalled.
		if (! this.collecting && ! this.stalled) {
			this.collecting = true;
			this.output.println("collect " + this.collectInnerSpeed + " " + this.collectOuterSpeed);
		}
		
		// Attempt to release balls if stalled.
		try {
			// Check if has any incoming data from stream.
			if (this.stream.available() > 0 && this.input.nextLine().equals("stalled")) {
				// Stop the collecting mechanism.
				this.stalled = true;
				this.collecting = false;
				//this.output.println("collect 0 0");
				
				// @wip - Release instantly for now.
				this.output.println("collect " + -this.collectInnerSpeed + " " + -this.collectOuterSpeed);
			}
		} catch (Exception e) {
			// Print the error stack trace.
			e.printStackTrace();
		}
	}

	/**
	 * Returns the distance between the vehicle and target.
	 *
	 * @param vehicle
	 * @param target
	 * @return double
	 */
	private double calculateDistance(Vehicle vehicle, Point target) {
		// Find difference between point and car.
		Point diff = new Point();
		diff.x = vehicle.front.x - target.x;
		diff.y = vehicle.front.y - target.y;
		
		// Find distance based on the diff point.
		return Math.round(Math.sqrt((diff.x * diff.x) + (diff.y * diff.y)));
	}
	
	/**
	 * Returns the rotation between the vehicle and target.
	 *
	 * @param vehicle 
	 * @param target
	 * @return double
	 */
	private double calculateRotation(Vehicle vehicle, Point target) {
		// Calculate the distance and degree between them.
		double degree = vehicle.findRotation(target, vehicle.back);
		
		// Find the degree difference between requested and current.
		double degreeDiff = Math.round(degree - vehicle.rotation);
		
		// Find most optimal rotation direction.
		if (degreeDiff > 180) {
			degreeDiff = degreeDiff - 360;
		} else if (degreeDiff < -180) {
			degreeDiff = degreeDiff + 360;
		}
		
		// Return the found rotation.
		return degreeDiff;
	}
	
}
