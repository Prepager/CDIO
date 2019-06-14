package sphinx.movement;

import java.io.InputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Scanner;

import org.opencv.core.Point;

import sphinx.Config;
import sphinx.Graph;
import sphinx.vision.Vehicle;

public class Client {
	
	/**
	 * The speed the vehicle turns.
	 *
	 * @var int
	 */
	int turnSpeed = Config.Client.turnSpeed;
	
	/**
	 * @wip
	 */
	int turnSlowSpeed = Config.Client.turnSlowSpeed;
	
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
	 * The amount of degree imperfection when slowed down.
	 *
	 * @var int
	 */
	int slowDegreeOffset = Config.Client.slowDegreeOffset;
	
	/**
	 * The stalled status of the pickup engine.
	 *
	 * @var Boolean
	 */
	public boolean stalled = false;
	
	/**
	 * The running status of the pickup engine.
	 *
	 * @var Boolean
	 */
	public boolean collecting = false;
	
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
	 * @wip
	 */
	long pauser;
	
	/**
	 * @wip
	 */
	boolean shouldReverse = false;
	
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
			
			// Kill program if exit on failure is enabled.
			if (Config.Client.exitFailed) {
				System.exit(0);
			}
		}
	}
	
	/**
	 * Handle the movement.
	 *
	 * @param vehicle
	 */
	public void run(Vehicle vehicle, Graph graph) {
		// Skip if socket is unconnected.
		if (this.socket == null) return;
		
		// SKip if currently paused.
		if (this.pauser > System.currentTimeMillis()) return;
		
		// Stop motors if missing targets.
		if (this.targets.isEmpty()) {
			// Reset motor statuses.
			this.stalled = false;
			this.collecting = false;
			
			// Stop the motors from running.
			this.output.println("move 0");
			this.output.println("collect " + this.collectInnerSpeed + " " + this.collectOuterSpeed);
			return;
		}
		
		// Get the first target or skip.
		Point target = this.targets.get(0);
		
		// Find distance and rotation.
		double rotation = this.calculateRotation(vehicle, target);
		double distance = this.calculateDistance(vehicle.front, target);
		
		// Handle the movement and collecting.
		this.handleMovement(distance, rotation, graph);
		this.handleCollecting(distance, rotation);

		// @wip
		System.out.println("> Dist: " + distance + " pixels, Rot:" + rotation + " deg");
		
		// Remove target if below offset.
		double distanceCenter = this.calculateDistance(vehicle.center, target);
		if (distance < this.distOffset || distanceCenter < this.distOffset) {
			//
			this.targets.remove(0);
			pauser = System.currentTimeMillis() + 1000;
			
			// @wip
			if (this.targets.isEmpty() && this.shouldReverse) {
				this.output.println("move " + -this.slowSpeed);
				pauser = System.currentTimeMillis() + 2000;
				this.shouldReverse = false;
			}
			
			// @wip
			this.shouldReverse = graph.reverse;
			
			/*if (this.targets.isEmpty() && graph.reverse) {
				long start = System.currentTimeMillis();
				while ((System.currentTimeMillis() - start ) < 200) {
					this.output.println("move " + -this.slowSpeed);
				}
			}*/
			
			//
			if (graph.towardsGoal && this.targets.isEmpty()) {
				//
				this.output.println("move 0 0");
				this.output.println("collect " + -this.collectInnerSpeed + " " + -this.collectOuterSpeed);
				this.stalled = false;

				//
				pauser = System.currentTimeMillis() + 6000;
			}
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
	private void handleMovement(double distance, double rotation, Graph graph) {
		//
		int degree = this.shouldSlow(distance)
			? this.slowDegreeOffset
			: this.degreeOffset;
		
		// Check if rotation is above imperfection limit.
		if (Math.abs(rotation) > degree) {
			//
			int turn = this.shouldSlow(distance)
				? this.turnSlowSpeed
				: this.turnSpeed;
			
			// Turn the vehicle to the found degree.
			this.output.println("turn " + (int) rotation + " " + turn);
			
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
				
				// Stop inner spinner and clear targets to get to goal.
				this.output.println("collect 0 " + this.collectOuterSpeed);
				this.targets.clear();
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
	private double calculateDistance(Point vehicle, Point target) {
		// Find difference between point and car.
		Point diff = new Point();
		diff.x = vehicle.x - target.x;
		diff.y = vehicle.y - target.y;
		
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
