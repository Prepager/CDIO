package sphinx;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.opencv.core.Core;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.highgui.HighGui;
import org.opencv.imgproc.Imgproc;

import sphinx.device.Client;
import sphinx.elements.Obstacle;
import sphinx.elements.Targets;
import sphinx.elements.Vehicle;
import sphinx.vision.Camera;
import sphinx.vision.Cropper;
import sphinx.vision.Frame;

public class Vision {
	
	/**
	 * The starting time of the run.
	 *
	 * @var long
	 */
	private long start = 0;
	
	/**
	 * The cropper timing delay.
	 *
	 * @var long
	 */
	private long cropTimer;
	
	/**
	 * The running state of the vehicle.
	 *
	 * @var boolean
	 */
	private boolean running = Config.Client.autoStart;
	
	/**
	 * The incoming console data stream.
	 *
	 * @var InputStreamReader
	 */
	private InputStreamReader stream;
	
	/**
	 * The console stream data reader.
	 *
	 * @var BufferedReader
	 */
	private BufferedReader reader;
	
	/**
	 * @wip
	 */
	int doneTimes = 0;
	
	/**
	 * Main static entry to program.
	 *
	 * @param args
	 */
	public static void main(String[] args) {
		// Load the OpenCV library.
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
		
		// Boot the computer vision.
		new Vision().boot();
	}

	/**
	 * Boots the main program.
	 */
	public void boot() {
		// Initialize the video capture.
		Camera camera = new Camera(
			Config.Camera.useWebcam,
			Config.Camera.source
		);
		
		// Initialize video cropper.
		Cropper cropper = new Cropper();
		
		// Initialize EV3 server connection.
		Client client = Config.Client.connect
				? new Client()
				: null;
		
		// Initialize path finding graph.
		Graph graph = Config.Graph.enable
			? new Graph()
			: null;
			
		// Initialize vision objects.
		Targets targets = new Targets();
		Vehicle vehicle = new Vehicle();
		Obstacle obstacle = new Obstacle();
			
		// Create frame holders.
		Frame frame = new Frame("Frame");
		Frame hsv = new Frame("HSV");
		
		// Start input stream and reader for system input.
		this.stream = new InputStreamReader(System.in);
		this.reader = new BufferedReader(this.stream);
		
		// Start program cropping time.
		this.cropTimer = System.currentTimeMillis();
		
		// Start infinity loop.
		while (true) {
			// Capture frame from camera.
			camera.capture(frame);
			
			// Check if should detch playin area.
			if (cropper.shouldDetect(this.cropTimer)) {
				cropper.detect(frame);
				continue;
			}
			
			// Crop the frame if enabled.
			if (Config.Camera.shouldCrop) {
				cropper.crop(frame);
			}
			
			// Convert frame to HSV color space.
			frame.convertTo(hsv, Imgproc.COLOR_BGR2HSV);
			
			// Detect red center obstacle.
			obstacle.detect(hsv);
			obstacle.draw(frame);
			
			// Detect white target circles.
			targets.detect(hsv);
			targets.draw(frame);
			
			// Detect blue vehicle triangle.
			vehicle.detect(hsv);
			vehicle.draw(frame);
			
			// Check if client is done.
			if (client != null && client.done && this.doneTimes < 1) {
				this.doneTimes++;
				client.done = false;
			} else if (client != null && client.done) {
				// Stop run and reset done state.
				this.running = false;
				client.done = false;
				
				// Emit decending beep.
				client.beep(2);
			}
			
			// Handle enabeling running state.
			try { 
				if (this.reader.ready()) {
					// Clear the buffer.
					String text = this.reader.readLine();
					
					// Enable the running state.
					this.running = ! this.running;
					
					//@wip
					if (graph != null) graph.path.clear();
					if (client != null) client.targets.clear();
					
					// Restart the starting time if empty or resetting.
					if (this.start == 0 || text.equals("reset")) {
						this.start = System.currentTimeMillis();
					}
					
					// Beep if just starting
					if (this.running) {
						client.beep(3);
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}

			// Output running time.
			if (Config.Client.printTimer && this.start != 0) {
				System.out.println(new SimpleDateFormat("mm:ss:SS").format(
					new Date(System.currentTimeMillis() - this.start)
				));
			}
			
			// Check if graph and client is enabled.
			if (this.running && vehicle.points != null && graph != null && client != null) {
				// Handle client movement.
				client.run(vehicle, graph, frame.getSource().cols(), frame.getSource().rows());
				
				// Force find balls if not stalled, has targets, and towards goals.
				// Used to find balls blocked by the vehicle when going towards goal.
				boolean forceFind = (! client.stalled && ! targets.points.isEmpty() && graph.towardsGoal);
				
				// Find goal if has no path, and, has no targets, or client is stalled.
				// Used to move towards goal when vehicle is done, not just at goal, and is stalled or no more targets.
				boolean findGoal = (client.targets.size() == 0 && ! client.wasTowardsGoal && ! client.doneGoalCheck && (targets.points.isEmpty() || client.stalled));
				
				// Find closest target if has no path, and has targets.
				// Used to find the next ball when vehicle is done, and more targets exists.
				boolean findClosest = (client.targets.size() == 0 && ! targets.points.isEmpty());
				
				// Determine if graph should run for current execution.
				if (forceFind || findGoal || findClosest) {
					// Run graph for vision objects.
					graph.run(
						obstacle.points, targets.points, vehicle.center,
						frame.getSource().cols(), frame.getSource().rows()
					);
				}
				
				// Make graph create path based on state.
				if (forceFind) {
					// Find the closest target.
					graph.findClosest();
					client.targets = graph.path;
				} else if (findGoal) {
					// Find the left goal.
					graph.findGoal(Config.Client.goalDirection);
					client.targets = graph.path;
				} else if (findClosest) {
					// Find the closest target.
					graph.findClosest();
					client.targets = graph.path;
				}
				
				// Draw path circles and direction.
				if (! graph.path.isEmpty()) {
					// Draw active color for path points.
					for (Point target : graph.path) {
			            Imgproc.circle(frame.getSource(), target, 3, new Scalar(0, 0, 255), -1);
					}
					
					// Draw arrowed line towards next graph point.
					Imgproc.arrowedLine(frame.getSource(), vehicle.front, graph.path.get(0), new Scalar(0, 0, 255));
				}
			} else {
				// Stop the vehicle from moving.
				if (client != null) client.stop();
			}
			
			// Draw wall and corner unsafe distances.
			int width = frame.getSource().cols();
			int height = frame.getSource().rows();
			
			int wallDist = Config.Client.wallSafeDistance;
			Imgproc.line(frame.getSource(), new Point(0, wallDist), new Point(width, wallDist), new Scalar(175, 175, 175));
			Imgproc.line(frame.getSource(), new Point(0, height-wallDist), new Point(width, height-wallDist), new Scalar(175, 175, 175));
			Imgproc.line(frame.getSource(), new Point(wallDist, 0), new Point(wallDist, height), new Scalar(175, 175, 175));
			Imgproc.line(frame.getSource(), new Point(width-wallDist, 0), new Point(width-wallDist, height), new Scalar(175, 175, 175));
			
			int cornerDist = Config.Client.cornerSafeDistance;
			Imgproc.line(frame.getSource(), new Point(cornerDist, 0), new Point(cornerDist, cornerDist), new Scalar(255, 255, 255));
			Imgproc.line(frame.getSource(), new Point(0, cornerDist), new Point(cornerDist, cornerDist), new Scalar(255, 255, 255));
			Imgproc.line(frame.getSource(), new Point(width-cornerDist, 0), new Point(width-cornerDist, cornerDist), new Scalar(255, 255, 255));
			Imgproc.line(frame.getSource(), new Point(width, cornerDist), new Point(width-cornerDist, cornerDist), new Scalar(255, 255, 255));
			Imgproc.line(frame.getSource(), new Point(cornerDist, height), new Point(cornerDist, height-cornerDist), new Scalar(255, 255, 255));
			Imgproc.line(frame.getSource(), new Point(0, height-cornerDist), new Point(cornerDist, height-cornerDist), new Scalar(255, 255, 255));
			Imgproc.line(frame.getSource(), new Point(width, height-cornerDist), new Point(width-cornerDist, height-cornerDist), new Scalar(255, 255, 255));
			Imgproc.line(frame.getSource(), new Point(width-cornerDist, height), new Point(width-cornerDist, height-cornerDist), new Scalar(255, 255, 255));

			// Calculate frame width and height.
			int fw = Config.Preview.displayWidth / 2;
			int fh = (int) (Config.Preview.displayHeight / 1.5);
			
			// Show the various frames.
			frame.show(fw, fh, 0, 0);
			targets.frame.show(fw, fh, fw, 0);
			obstacle.frame.show(fw, fh, 0, Config.Preview.displayHeight / 2);
			vehicle.frame.show(fw, fh, fw, Config.Preview.displayHeight / 2);

			// Add small delay.
			HighGui.waitKey(1);
		}
	}

}