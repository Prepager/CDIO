package sphinx.movement;

import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import org.opencv.core.Mat;
import org.opencv.core.Point;

import sphinx.vision.Vehicle;

public class Client {
	
	/**
	 * The speed the vehicle turns.
	 *
	 * @var int
	 */
	int turnSpeed = 150;
	
	/**
	 * The speed the vehicle moves.
	 *
	 * @var int
	 */
	int moveSpeed = 200;
	
	/**
	 * The slow speed when the vehicle moves.
	 *
	 * @var int
	 */
	int slowSpeed = 100;
	
	/**
	 * The dist threshold when the vehicle should slow down.
	 *
	 * @var int
	 */
	int slowThreshold = 40;
	
	/**
	 * The amount of distance imperfection.
	 *
	 * @var int
	 */
	int distOffset = 10;
	
	/**
	 * The amount of degree imperfection.
	 *
	 * @var int
	 */
	int degreeOffset = 6;
	
	/**
	 * The socket for the connection.
	 *
	 * @var Socket
	 */
	Socket socket;
	
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
	ArrayList<Point> targets = new ArrayList<Point>();
	
	/**
	 * Attempt to connect to the EV3 and open stream.
	 */
	public Client() {
		try {
			// Show connection warning.
			System.out.println("Attempting to connect to server!");
			
			// Create testing targets.
			this.targets.add(new Point(100, 140));
			this.targets.add(new Point(480, 140));
			this.targets.add(new Point(480, 360));
			this.targets.add(new Point(100, 360));
			
			// Open connection and stream.
			this.socket = new Socket("192.168.43.44", 59898);
			this.output = new PrintWriter(this.socket.getOutputStream(), true);
		} catch (Exception e) {
			// Show connection failed error.
			e.printStackTrace();
			System.out.println("Failed to connect to server!");
		}
	}
	
	/**
	 * Handle the movement.
	 *
	 * @param vehicle
	 * @param balls
	 */
	public void run(Vehicle vehicle, Mat balls) {
		// Skip if socket connection failed.
		if (this.socket == null) return;
		
		// Get the first target or skip.
		Point target = this.targets.get(0);
		if (target == null) return;
		
		// Find difference between point and car.
		Point diff = new Point();
		diff.x = vehicle.front.x - target.x;
		diff.y = vehicle.front.y - target.y;
		
		// Calculate the distance and degree between them.
		double dist = Math.sqrt((diff.x * diff.x) + (diff.y * diff.y));
		double degree = vehicle.findRotation(target, vehicle.back);
		
		// Find the degree difference between requested and current.s
		int degreeDiff = (int) Math.round(degree - vehicle.rotation);
		
		// Output debug messages.
		System.out.println("Dist: " + dist + ", Degrees: " + degree + " (Diff: " + degreeDiff + ")");
		
		// Check if degree is above imperfection limit.
		if (Math.abs(degreeDiff) > this.degreeOffset) {
			// Turn the vehicle to the found degree.
			System.out.println("turn " + degreeDiff + " " + this.turnSpeed);
			this.output.println("turn " + degreeDiff + " " + this.turnSpeed);
			
			// Skip movement while turning.
			return;
		}
		
		// Remove target if below offset.
		if (dist < this.distOffset) {
			this.targets.remove(0);
			return;
		}
		
		// Determine speed based on distance.
		int speed = dist > this.slowThreshold
			? this.moveSpeed
			: this.slowSpeed;
		
		// Make vehicle move at found speed.
		System.out.println("move " + speed);
		this.output.println("move " + speed);
	}
	
}
