package sphinx.device;

import java.io.InputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Scanner;

import org.opencv.core.Point;
import org.opencv.imgproc.Imgproc;

import sphinx.Config;
import sphinx.Graph;
import sphinx.elements.Vehicle;

public class Client {
	
	/**
	 * The speed the vehicle turns.
	 *
	 * @var int
	 */
	int turnSpeed = Config.Client.turnSpeed;
	
	/**
	 * The slow speed the vehicle turns.
	 *
	 * @var int
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
	 * The amount of inner distance imperfection.
	 *
	 * @var double
	 */
	double insideDistOffset = Config.Client.insideDistOffset;
	
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
	 * The millis counter used for pausing.
	 *
	 * @var long
	 */
	long pauser = 0;
	
	/**
	 * The upcoming should reverse state.
	 *
	 * @var boolean
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
		
		// Skip if currently paused.
		if (this.pauser > System.currentTimeMillis()) return;
		
		// Stop motors if missing targets.
		if (this.targets.isEmpty()) {
			// Reset motor statuses.
			this.stalled = false;
			this.collecting = false;
			
			// Stop the motors from running.
			this.move(0);
			this.collect(0, 0);
			return;
		}
		
		// Get the first target or skip.
		Point target = this.targets.get(0);
		
		// Find distance and rotation.
		double rotation = this.calculateRotation(vehicle, target);
		double distance = this.calculateDistance(vehicle, target);
		
		// Handle the movement and collecting.
		this.handleMovement(Math.abs(distance), rotation);
		this.handleCollecting(Math.abs(distance), rotation);
		
		// Handle target pathing.
		this.handlePathing(distance, target, vehicle, graph);

		// @wip
		//System.out.println("> Dist: " + distance + " pixels, Rot:" + rotation + " deg");		
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
		// Find the degree offset.
		int degree = this.shouldSlow(distance)
			? this.slowDegreeOffset
			: this.degreeOffset;
		
		// Check if rotation is above imperfection limit.
		if (Math.abs(rotation) > degree) {
			// Determine the turning speed.
			int turn = this.shouldSlow(distance)
				? this.turnSlowSpeed
				: this.turnSpeed;
			
			// Turn the vehicle to the found degree.
			this.turn((int) rotation, turn);
			
			// Skip movement while turning.
			return;
		}
		
		// Determine movement speed based on distance.
		int speed = this.shouldSlow(distance)
			? this.slowSpeed
			: this.moveSpeed;
		
		// Make vehicle move at found speed.
		this.move(speed);
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
			this.collect(this.collectInnerSpeed, this.collectOuterSpeed);
		}
		
		// Attempt to release balls if stalled.
		try {
			// Check if has any incoming data from stream.
			if (this.stream.available() > 0 && this.input.nextLine().equals("stalled")) {
				// Stop the collecting mechanism.
				this.stalled = true;
				this.collecting = false;
				
				// Stop inner spinner and clear targets to get to goal.
				this.collect(0, this.collectOuterSpeed);
				this.targets.clear();
			}
		} catch (Exception e) {
			// Print the error stack trace.
			e.printStackTrace();
		}
	}

	/**
	 * Handle the pathing when reaching targets.
	 *
	 * @param distance
	 * @param rotation
	 */
	private void handlePathing(double dist, Point target, Vehicle vehicle, Graph graph) {
		// Skip if target point is not inside triangle.
		if (dist <= this.insideDistOffset) return;

		// Remove the target from the list.
		this.targets.remove(0);
		
		// Keep moving forward to prevent turning.
		this.move(this.slowSpeed);
		
		// Check if at last path item and should reverse back.
		if (this.targets.isEmpty() && this.shouldReverse) {
			// Enable reversing and pause.
			this.move(-this.slowSpeed);
			this.pause(3000); 
			
			// Disable reversing state.
			this.shouldReverse = false;
		}
		
		// Set reversing state for upcoming targets.
		this.shouldReverse = graph.reverse;
		
		// Check if at last path item and is towards the goal.
		if (this.targets.isEmpty() && graph.towardsGoal) {
			// Disable movement and spit out the balls.
			this.move(0);
			this.collect(-this.collectInnerSpeed, -this.collectOuterSpeed);
			
			// Disable the engine stalled state.
			this.stalled = false;
			
			// Pause to wait for balls to roll out.
			this.pause(5000);
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
		return Imgproc.pointPolygonTest(vehicle.triangle, target, false);
		
		// Find difference between point and car.
		/*Point diff = new Point();
		diff.x = vehicle.x - target.x;
		diff.y = vehicle.y - target.y;
		
		// Find distance based on the diff point.
		return Math.round(Math.sqrt((diff.x * diff.x) + (diff.y * diff.y)));*/
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
	
	/**
	 * Move the vehicle with the passed speed.
	 *
	 * @param speed
	 */
	private void move(int speed) {
		this.output.println("move " + speed);
	}
	
	/**
	 * Turn the vehicle with the passed angle and speed.
	 *
	 * @param angle
	 * @param speed
	 */
	private void turn(int angle, int speed) {
		this.output.println("turn " + angle + " " + speed);
	}
	
	/**
	 * Enable collecting engines for inner and outer engines.
	 *
	 * @param inner
	 * @param outer
	 */
	private void collect(int inner, int outer) {
		this.output.println("collect " + inner + " " + outer);
	}
	
	/**
	 * Pause the execution of the pathing for the passed millis.
	 *
	 * @param millis
	 */
	private void pause(int millis) {
		this.pauser = System.currentTimeMillis() + millis;
	}
	
}
