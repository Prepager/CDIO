package sphinx;

import sphinx.vision.Camera;
import sphinx.vision.Contour;
import sphinx.vision.Frame;
import sphinx.vision.Vehicle;

import java.util.List;

import org.opencv.core.*;
import org.opencv.highgui.*;
import org.opencv.imgproc.Imgproc;

public class Vision {
	
	public boolean crop = false;
	public boolean useWebcam = false;
	public String source = "./src/video3.mov";
	
	public int blurSize = 3;
	public int minRadius = 7;
	public int maxRadius = 14;
	public int minDistance = 5;
	public int cannyThreshold = 50;
	
	public int kernelSize = 3;
	public int whiteSensitivity = 35;
	public double DP = 1.4;
	
	public int displayWidth = 1280;
	public int displayHeight = 720;
	
	public Camera camera;
	public Graph graph = new Graph();
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
		
		// Initialize the video capture.
		this.camera = new Camera(this.useWebcam, this.source);
		
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
				new Scalar(95, 100, 100),
				new Scalar(130, 255, 255)
			);
			
			// Detect the vehicle on the frame.
			vehicle.detect(blue);
			vehicle.draw(frame);
			
			// Isolate the red color from the image.
			hsv.isolateRange(red,
				new Scalar(0, 80, 80),
				new Scalar(10, 255, 255)
			);
			
			// Create array to contain the rotated rectangle corner points
			Point[] obstaclePoints = new Point[4];

			// Crop to red contour if requested
			if (this.crop) {
				// Find sorted contours and find second largest.
				List<MatOfPoint> contours = Contour.sortedContours(red, Imgproc.RETR_TREE);
				RotatedRect rect = this.contourToRect(contours.get(1));
				
				// Crop the frame to the found playing area.
				frame.cropToRectangle(rect);
				hsv.cropToRectangle(rect);
				red.cropToRectangle(rect);
				blue.cropToRectangle(rect);
				
				// Get bounding boxes.
				List<MatOfPoint> obstacles = Contour.sortedContours(red, Imgproc.RETR_TREE);
				RotatedRect obstacleRect = this.contourToRect(obstacles.get(0));
				
				// Save corner points to point array
				this.contourToRect(obstacles.get(obstacles.size()-1)).points(obstaclePoints);

				// @wip - Remove later: Draw obstacle lines on frame.
				for (int i = obstacles.size()-1; i >= 0 ; i--) {
					// Get the rectangle for the given contour
					obstacleRect = this.contourToRect(obstacles.get(i));
					
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
				new Scalar(0, 0, 255 - whiteSensitivity),
				new Scalar(255, whiteSensitivity, 255)
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
			this.graph.run(obstaclePoints, circles, new Point(), frame.getSource().cols(), frame.getSource().rows());
			
			// Calculate frame width and height.
			int fw = displayWidth / 2;
			int fh = (int) (displayHeight / 1.5);
			
			// Show the various frames.
			frame.show(fw, fh, 0, 0);
			white.show(fw, fh, displayWidth / 2, 0);
			blue.show(fw, fh, 0, displayHeight / 2);
			red.show(fw, fh, displayWidth / 2, displayHeight / 2);

			// Add small delay.
			HighGui.waitKey(1);
		}
	}
	
	/**
	 * Convert a MatOfPoint to Rotated Rect.
	 *
	 * @param point
	 * @return RotatedRect
	 */
	public RotatedRect contourToRect(MatOfPoint point)
	{
		// Convert point to 2f.
		MatOfPoint2f points = new MatOfPoint2f(point.toArray());
		
		// Find and return the min area rect.
		return Imgproc.minAreaRect(points);
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
	

	/**
	 * Draws the passed contours on the frame.
	 *
	 * @param frame
	 * @param circles
	 * @return MAt
	 */
	public Mat drawContours(Mat frame, MatOfPoint[] contours)
	{
		// Loop though the contours.
		for (int x = 0; x < contours.length; x++) {
			// @wip
			frame = this.drawContour(frame, contours[x]);
		}
		
		// Return the updated frame.
		return frame;
	}
	
	/**
	 * @wip
	 */
	public Mat drawContour(Mat frame, MatOfPoint contour)
	{
		// Convert contour to rotated rect.
		RotatedRect rect = this.contourToRect(contour);

		// Get the points for the rectangle.
		Point[] points = new Point[4];
		rect.points(points);

		// Loop through the points and add lines between them.
		for (int i = 0; i < 4; i++) {
			Imgproc.line(frame, points[i], points[(i + 1) % 4], new Scalar(255, 0, 0), 1, 8);
		}

		// Return the updated frame.
		return frame;
	}
}