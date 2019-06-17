package sphinx;

import java.util.List;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.highgui.HighGui;
import org.opencv.imgproc.Imgproc;

import sphinx.movement.Client;
import sphinx.vision.Camera;
import sphinx.vision.Cropper;
import sphinx.vision.Frame;
import sphinx.vision.Obstacle;
import sphinx.vision.Targets;
import sphinx.vision.Vehicle;

public class Vision {
	
	public Graph graph;
	public Camera camera;
	public Client client;
	public Cropper cropper;
	public Targets targets = new Targets();
	public Vehicle vehicle = new Vehicle();
	public Obstacle obstacle = new Obstacle();
	
	public static void main(String[] args) {
		// Load the OpenCV library.
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
		
		// Boot the computer vision.
		new Vision().boot();
	}

	/**
	 * Boots the program.
	 */
	public void boot() {
		// Create and new client instance and connect.
		if (Config.Client.connect) {
			this.client = new Client();
		}
		
		// Create new graph instance.
		if (Config.Graph.enable) {
			this.graph = new Graph();
		}
		
		// Initialize the video capture.
		this.camera = new Camera(
			Config.Camera.useWebcam,
			Config.Camera.source
		);
		
		// @wip
		if (Config.Camera.shouldCrop) {
			this.cropper = new Cropper(this.camera);
		}
		
		// Create various frames.
		Frame frame = new Frame("Frame");
		Frame hsv = new Frame("HSV");

		// Start processing loop.
		while (true) {
			// Capture frame from camera and blur.
			camera.capture(frame);

			// Crop to playing area if found.
			if (this.cropper != null && this.cropper.rect != null) {
				this.cropper.cropFrame(frame);
			}
			
			// Convert frame to HSV color space
			frame.convertTo(hsv, Imgproc.COLOR_BGR2HSV);
			
			// @wip
			this.vehicle.detect(hsv);
			this.obstacle.detect(hsv);
			this.targets.detect(hsv);
			
			//
			this.vehicle.draw(frame);
			this.obstacle.draw(frame);
			this.targets.draw(frame);

			// Isolate the white color from the image.
			/*hsv.isolateRange(white,
				Config.Colors.whiteLower,
				Config.Colors.whiteUpper
			);

			// Dilate the white area.
			Mat element = Imgproc.getStructuringElement(Imgproc.CV_SHAPE_ELLIPSE, new Size(2 * this.kernelSize + 1, 2 * this.kernelSize + 1), new Point(this.kernelSize, this.kernelSize));
			Imgproc.dilate(white.getSource(), white.getSource(), element);

			// Find and save the circles in playing area.
			Mat circles = new Mat();
			Imgproc.HoughCircles(white.getSource(), circles, Imgproc.HOUGH_GRADIENT, this.DP, this.minDistance, this.cannyThreshold * 3, 14, this.minRadius, this.maxRadius); // @wip - param2?
			
			// Find the circles in the frame.
			this.drawCircles(frame.getSource(), circles);*/
			
			// @wip
			/*ArrayList<Point> targets = new ArrayList<Point>();
			targets.add(new Point(100, 140));
			targets.add(new Point(480, 140));
			targets.add(new Point(480, 360));
			targets.add(new Point(100, 360));
			targets.add(new Point(250, 250));
			
			this.drawTestCircles(frame.getSource(), targets);*/
			
			
			/*this.graph.findClosest();
			this.drawTestCircles(frame.getSource(), this.graph.path);*/
			
			//
			if (this.client != null) {
				this.client.run(this.vehicle, this.graph);
			}
			
			// Run graph algorithm.
			if (this.graph != null) {
				//
				//this.graph.run(obstaclePoints, circles, this.vehicle.center, frame.getSource().cols(), frame.getSource().rows());
				//this.graph.findClosest();
				if (this.client != null && ! this.client.stalled && ! this.targets.circles.empty() && this.graph.towardsGoal) {
					//
					this.graph.run(this.obstacle.points, this.targets.circles, this.vehicle.center, frame.getSource().cols(), frame.getSource().rows());
					this.graph.findClosest();
					this.client.targets = this.graph.path;
				} else if (this.client != null && this.client.targets.size() == 0) {
					//
					this.graph.run(this.obstacle.points, this.targets.circles, this.vehicle.center, frame.getSource().cols(), frame.getSource().rows());
					
					//
					if (this.targets.circles.empty() || this.client.stalled) {
						this.graph.findGoal(0);
					} else {
						this.graph.findClosest();
					}
					
					//
					this.client.targets = this.graph.path;
				}
				
				//
				this.drawTestCircles(frame.getSource(), this.graph.path);
				if (!this.graph.path.isEmpty()) {
					Imgproc.arrowedLine(frame.getSource(), vehicle.center, this.graph.path.get(0), new Scalar(0, 0, 255));					
				}
			}

			// Calculate frame width and height.
			int fw = Config.Preview.displayWidth / 2;
			int fh = (int) (Config.Preview.displayHeight / 1.5);
			
			// Show the various frames.
			frame.show(fw, fh, 0, 0);
			this.targets.frame.show(fw, fh, fw, 0);
			this.obstacle.frame.show(fw, fh, 0, Config.Preview.displayHeight / 2);
			this.vehicle.frame.show(fw, fh, fw, Config.Preview.displayHeight / 2);

			// Add small delay.
			HighGui.waitKey(1);
		}
	}
	
	/**
	 * Draws the passed test circles on the frame.
	 *
	 * @param frame
	 * @param circles
	 * @return Mat
	 */
	public Mat drawTestPoints(Mat frame, Point[] circles)
	{
		// Loop though the circles.
		for (int x = 0; x < circles.length; x++) {
			// Get the current circle.
            Point center = circles[x];
            Imgproc.circle(frame, center, 3, new Scalar(255, 0, 255), -1);
		}
		
		// Return the updated frame.
		return frame;
	}
	
	/**
	 * Draws the passed test circles on the frame.
	 *
	 * @param frame
	 * @param circles
	 * @return Mat
	 */
	public Mat drawTestCircles(Mat frame, List<Point> circles)
	{
		// Loop though the circles.
		for (int x = 0; x < circles.size(); x++) {
			// Get the current circle.
            Point center = circles.get(x);

            // Add circle to center based on radius.
            int radius = (int) Math.round(10);
            //System.out.print(radius + ", ");
            Imgproc.circle(frame, center, radius + 1, new Scalar(0, 255, 0), -1);
            Imgproc.circle(frame, center, 3, new Scalar(0, 0, 255), -1);
		}
		
		// Return the updated frame.
		return frame;
	}
	
}