package sphinx;

import org.opencv.core.Core;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.highgui.HighGui;
import org.opencv.imgproc.Imgproc;

import sphinx.device.Client;
import sphinx.vision.Camera;
import sphinx.vision.Cropper;
import sphinx.vision.Frame;
import sphinx.elements.Obstacle;
import sphinx.elements.Targets;
import sphinx.elements.Vehicle;

public class Vision {
	
	/**
	 * @wip
	 *
	 * @var long
	 */
	private long start;
	
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
		
		// Set cropping time,
		this.start = System.currentTimeMillis();
		
		// Start infinity loop.
		while (true) {
			// Capture frame from camera.
			camera.capture(frame);
			
			// Check if should detch playin area.
			if (cropper.shouldDetect(this.start)) {
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
			
			// Handle client movement if enabled.
			if (client != null) client.run(vehicle, graph);
			
			// Check if graph and client is enabled.
			if (graph != null && client != null) {
				// Force find balls if not stalled, has targets, and towards goals.
				// Used to find balls blocked by the vehicle when going towards goal.
				boolean forceFind = (! client.stalled && ! targets.circles.empty() && graph.towardsGoal);
				
				// Find goal if has no path, and, has no targets, or client is stalled.
				// Used to move towards goal when vehicle is done, and is stalled or no more targets.
				boolean findGoal = (client.targets.size() == 0 && (targets.circles.empty() || client.stalled));
				
				// Find closest target if has no path, and has targets.
				// Used to find the next ball when vehicle is done, and more targets exists.
				boolean findClosest = (client.targets.size() == 0 && ! targets.circles.empty());
				
				// Determine if graph should run for current execution.
				if (forceFind || findGoal || findClosest) {
					// Run graph for vision objects.
					graph.run(
						obstacle.points, targets.circles, vehicle.center,
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
					graph.findGoal(0);
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
			}
			
			// @wip - graph testing
			if (graph != null && client == null) {
				graph.run(
					obstacle.points, targets.circles, vehicle.center,
					frame.getSource().cols(), frame.getSource().rows()
				);

				graph.findClosest();
				
				if (! graph.path.isEmpty()) {
					for (int i = 0; i < graph.path.size(); i++) {
						Point target = graph.path.get(i);
			            Imgproc.circle(frame.getSource(), target, 5, new Scalar(255, 0, 0), -1);
			            
			            if (i < graph.path.size()-1) {
			            	Point next = graph.path.get(i+1);
			            	Imgproc.line(frame.getSource(), target, next, new Scalar(0, 0, 255));
			            }
					}
					
					Imgproc.line(frame.getSource(), vehicle.center, graph.path.get(0), new Scalar(0, 0, 255));
					
					
					/*for (Point target : graph.path) {
			            Imgproc.circle(frame.getSource(), target, 3, new Scalar(0, 0, 255), -1);
			            if (graph.path)
					}*/
					
					
					// Draw active color for path points.
					/*for (Point target : graph.path) {
			            Imgproc.circle(frame.getSource(), target, 3, new Scalar(0, 0, 255), -1);
					}
					
					// Draw arrowed line towards next graph point.
					Imgproc.arrowedLine(frame.getSource(), vehicle.center, graph.path.get(0), new Scalar(0, 0, 255));*/
				}
			}

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