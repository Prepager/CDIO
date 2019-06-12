package sphinx;

import sphinx.movement.Client;
import sphinx.vision.Camera;
import sphinx.vision.Frame;
import sphinx.vision.Vehicle;

import java.util.List;

import org.opencv.core.*;
import org.opencv.highgui.*;
import org.opencv.imgproc.Imgproc;

public class Vision {
	
	public int blurSize = 3;
	public int minRadius = 7;
	public int maxRadius = 14;
	public int minDistance = 5;
	public int cannyThreshold = 50;

	public int kernelSize = 3;
	public double DP = 1.4;
	
	public Camera camera;
	public Client client;
	public Graph graph;// = new Graph();
	public Vehicle vehicle = new Vehicle();
	
	public static void main(String[] args) {
		new Vision().boot();
	}

	/**
	 * Boots the program.
	 */
	public void boot() {
		// Load the OpenCV library.
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
		
		// Create and new client instance and connect.
		if (Config.Client.connect) {
			this.client = new Client();
		}
		
		// Initialize the video capture.
		this.camera = new Camera(
			Config.Camera.useWebcam,
			Config.Camera.source
		);
		
		// Create various frames.
		Frame frame = new Frame("Frame");
		Frame hsv = new Frame("HSV");
		Frame red = new Frame("Red");
		Frame blue = new Frame("Blue");
		Frame white = new Frame("White");

		// Start processing loop.
		while (true) {
			// Capture frame from camera and blur.
			camera.capture(frame);
			frame.blur(this.blurSize);
			
			// Convert frame to HSV color space
			frame.convertTo(hsv, Imgproc.COLOR_BGR2HSV);
			
			// Isolate the blue color from the image.
			hsv.isolateRange(blue,
				Config.Colors.blueLower,
				Config.Colors.blueUpper
			);
			
			// Detect the vehicle on the frame.
			vehicle.detect(blue);
			vehicle.draw(frame);
			
			// Isolate the red color from the image.
			hsv.isolateRange(red,
				Config.Colors.redLower,
				Config.Colors.redUpper
			);
			
			// Create array to contain the rotated rectangle corner points
			Point[] obstaclePoints = new Point[4];

			// Crop to red contour if requested
			if (Config.Camera.shouldCrop) {
				// Find sorted contours and find second largest.
				List<MatOfPoint> contours = red.sortedContours();
				RotatedRect rect = red.contourToRect(contours.get(1));
				
				// Crop the frame to the found playing area.
				frame.cropToRectangle(rect);
				hsv.cropToRectangle(rect);
				red.cropToRectangle(rect);
				blue.cropToRectangle(rect);
				
				// Get bounding boxes.
				List<MatOfPoint> obstacles = red.sortedContours();
				RotatedRect obstacleRect = red.contourToRect(obstacles.get(0));
				
				// Save corner points to point array
				red.contourToRect(obstacles.get(obstacles.size()-1)).points(obstaclePoints);

				// @wip - Remove later: Draw obstacle lines on frame.
				for (int i = obstacles.size()-1; i >= 0 ; i--) {
					// Get the rectangle for the given contour
					obstacleRect = red.contourToRect(obstacles.get(i));
					
					// Only process near square rectangles
					if(obstacleRect.size.width/obstacleRect.size.height <= 1.15 && obstacleRect.size.width/obstacleRect.size.height >= 0.85 && obstacleRect.size.width > 2) {
						// Save corner points to point array
						obstacleRect.points(obstaclePoints);
						
						// Draw rotated rectangle on frame (from corner points)
						for (int j = 0; j < 4; j++) {
							Imgproc.line(frame.getSource(), obstaclePoints[j], obstaclePoints[(j+1) % 4], new Scalar(255,0,0));
						}
						// break if smallest rect is found
						break;
					}
				}
			}

			// Isolate the white color from the image.
			hsv.isolateRange(white,
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
			this.drawCircles(frame.getSource(), circles);
			
			// Run graph algorithm.
			//this.graph.run(obstaclePoints, circles, new Point(), frame.getSource().cols(), frame.getSource().rows());
			
			//
			if (this.client != null) {
				this.client.run(vehicle, circles);
			}

			// Calculate frame width and height.
			int fw = Config.Preview.displayWidth / 2;
			int fh = (int) (Config.Preview.displayHeight / 1.5);
			
			// Show the various frames.
			frame.show(fw, fh, 0, 0);
			white.show(fw, fh, fw, 0);
			//red.show(fw, fh, 0, Config.Preview.displayHeight / 2);
			blue.show(fw, fh, fw, Config.Preview.displayHeight / 2);

			// Add small delay.
			HighGui.waitKey(1);
		}
	}
	
	/**
	 * Draws the passed circles on the frame.
	 *
	 * @param frame
	 * @param circles
	 * @return MAt
	 */
	public Mat drawCircles(Mat frame, Mat circles)
	{
		// Loop though the circles.
		for (int x = 0; x < circles.cols(); x++) {
			// Get the current circle.
            double[] c = circles.get(0, x);

            // Create new point for circle.
            Point center = new Point(Math.round(c[0]), Math.round(c[1]));

            // Add circle to center based on radius.
            int radius = (int) Math.round(c[2]);
            //System.out.print(radius + ", ");
            Imgproc.circle(frame, center, radius + 1, new Scalar(0, 255, 0), -1);
            Imgproc.circle(frame, center, 3, new Scalar(0, 0, 255), -1);
		}
		
		// Return the updated frame.
		return frame;
	}
	
}