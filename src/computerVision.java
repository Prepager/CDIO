import java.util.List;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Arrays;

import org.opencv.core.*;
import org.opencv.highgui.*;
import org.opencv.imgproc.Imgproc;
import org.opencv.videoio.VideoCapture;
import org.opencv.videoio.Videoio;

public class computerVision {
	
	public static boolean webcam = false;
	
	public static int blurSize = 3;
	public static int minRadius = 4;
	public static int maxRadius = 20;
	public static int minDistance = 5;
	public static int cannyThreshold = 50;
	
	public static int kernelSize = 3;
	public static int whiteSensitivity = 35;
	public static double DP = 1.4;

	public static void main(String[] args) {
		new computerVision().boot();
	}

	/**
	 * Boots the program.
	 */
	public void boot() {
		// Load the OpenCV library.
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
		
		// Initialize the video capture.
		VideoCapture capture = this.initCamera(webcam, "./src/video.mov", 640, 480);

		// Create new controls object.
		Controls control = new Controls();
		
		// Prepare capture frame holder.
		Mat frame = new Mat();
		Mat gray  = new Mat();
		Mat canny = new Mat();
		Mat red   = new Mat();
		Mat hsv	  = new Mat();
		Mat white = new Mat();

		// Start processing loop.
		while (true) {
			// Read in frame from capture.
			if (! capture.read(frame) && ! webcam) {
				capture.set(Videoio.CAP_PROP_POS_FRAMES, 0);
				capture.read(frame);
			}

			// Add blur to frame.
			Imgproc.medianBlur(frame, frame, blurSize);
			
			// Convert frame to HSV color space.
			Imgproc.cvtColor(frame, hsv, Imgproc.COLOR_BGR2HSV);

			// Isolate the red color from the image.
			red = this.isolateColorRange(hsv,
				new Scalar(0, 100, 100),
				new Scalar(10, 255, 255),
				new Scalar(160, 100, 100),
				new Scalar(180, 255, 255)
			);
			
			// Find sorted contours and find second largest.
			MatOfPoint[] contours = this.sortContours(red);
			RotatedRect rect = this.contourToRect(contours[1]);
			
			
			// Crop the frame to the found playing area.
            frame = this.cropToRectangle(frame, rect);
			red = this.cropToRectangle(red, rect);
			hsv = this.cropToRectangle(hsv, rect);
            
			
			// 		Finding the obstacle
			// Get bounding boxes
			MatOfPoint[] obstacle = this.sortContours(red);
			RotatedRect obstacleRect = this.contourToRect(obstacle[0]);
			
			// Create array to contain the rotated rectangle corner points
			Point[] obstaclePoints = new Point[4];
			
			// Save corner points to point array
			contourToRect(obstacle[obstacle.length-1]).points(obstaclePoints);
					
			// Go through all found contours
			for (int i = obstacle.length-1; i >= 0 ; i--) {
				// Get the rectangle for the given contour
				obstacleRect = this.contourToRect(obstacle[i]);
				
				// Only process near square rectangles
				if(obstacleRect.size.width/obstacleRect.size.height <= 1.15 && obstacleRect.size.width/obstacleRect.size.height >= 0.85 && obstacleRect.size.width > 2) {
					// Save corner points to point array
					obstacleRect.points(obstaclePoints);
					
					// Draw rotated rectangle on frame (from corner points)
					for (int j = 0; j < 4; j++) {
						Imgproc.line(frame, obstaclePoints[j], obstaclePoints[(j+1) % 4], new Scalar(255,0,0));
					}
					// break if smallest rect is found
					break;
				}
				
			}
			// Convert frame to gray.
			Imgproc.cvtColor(frame, gray, Imgproc.COLOR_BGR2GRAY);
			
			// Isolate the white color from the image.
			white = this.isolateColorRange(hsv,
				new Scalar(0, 0, 255 - whiteSensitivity),
				new Scalar(255, whiteSensitivity, 255),
				new Scalar(0, 0, 255 - whiteSensitivity),
				new Scalar(255, whiteSensitivity, 255)
			);

			// Dilate the white area.
			Mat element = Imgproc.getStructuringElement(Imgproc.CV_SHAPE_ELLIPSE, new Size(2 * kernelSize + 1, 2 * kernelSize + 1), new Point(kernelSize, kernelSize));
			Imgproc.dilate(white, white, element);

			// Convert gray to canny.
			Imgproc.Canny(white, canny, cannyThreshold, cannyThreshold * 3);

			// Find and save the circles in playing area.
			Mat circles = new Mat();
			Imgproc.HoughCircles(white, circles, Imgproc.HOUGH_GRADIENT, DP, minDistance, cannyThreshold * 3, 14, minRadius, maxRadius); // @wip - param2?

			// Find the circles in the frame.
			this.drawCircles(frame, circles);
			
			// Pass red and circles to controls.
			control.run(obstaclePoints, circles, frame.cols(), frame.rows());
			
			// Show the frame on the screen.
	        HighGui.imshow("Frame", frame);
			HighGui.imshow("Canny", canny);

	        HighGui.imshow("White", white);
	        
			// Resize and move frames to fit screen.
			HighGui.resizeWindow("Frame", 1280/2, (int) (720/1.5));
			HighGui.resizeWindow("Canny", 1280/2, (int) (720/1.5));
			HighGui.moveWindow("Canny", 1280/2, 0);

			// Add small delay.
			HighGui.waitKey(1);
		}
	}
	
	/**
	 * Initialize and returns a video capture.
	 *
	 * @param webcam
	 * @param fallback
	 * @param width
	 * @param height
	 * @return VideoCapture
	 */
	public VideoCapture initCamera(boolean webcam, String fallback, int width, int height)
	{
		// Create new video capture object.
		VideoCapture capture = webcam
			? new VideoCapture(0)
			: new VideoCapture(fallback);
		
		// Set capture width and height.
		capture.set(Videoio.CAP_PROP_FRAME_WIDTH, width);
		capture.set(Videoio.CAP_PROP_FRAME_HEIGHT, height);
		capture.set(Videoio.CAP_PROP_AUTOFOCUS, 0);
		
		// Return the created capture.
		return capture;
	}
	
	/**
	 * Isolates the color range in a copy of the passe frame.
	 *
	 * @param frame
	 * @param lowerLow
	 * @param lowerHigh
	 * @param upperLow
	 * @param upperHigh
	 * @return Mat
	 */
	public Mat isolateColorRange(Mat frame, Scalar lowerLow, Scalar lowerHigh, Scalar upperLow, Scalar upperHigh)
	{
		// Prepare destination frame.
		Mat destination = new Mat();

		// Create lower and upper mask holder.
		Mat maskLower = new Mat();
		Mat maskUpper = new Mat();

		// Find areas between lower and upper range.
		Core.inRange(frame, lowerLow, lowerHigh, maskLower);
		Core.inRange(frame, upperLow, upperHigh, maskUpper);
		
		// Combine the two masks together into destination.
		Core.bitwise_or(maskLower, maskUpper, destination);
		
		// Return the combined mask.
		return destination;
	}
	
	/**
	 * Find and sort the contours in the passed frame.
	 *
	 * @param frame
	 * @return MatOfPoint[]
	 */
	public MatOfPoint[] sortContours(Mat frame)
	{
		// Find the contours on the passed frame.
		List<MatOfPoint> contours = new ArrayList<>();
		Imgproc.findContours(frame, contours, new Mat(), Imgproc.RETR_TREE, Imgproc.CHAIN_APPROX_SIMPLE);

		// Convert contour list to array of MatOfPoints
		MatOfPoint[] list = new MatOfPoint[contours.size()];
		contours.toArray(list);
		
		// Sort the contours by largest area.
		Arrays.sort(list, (a, b) -> {
			// Find area of two contours.
			Double aArea = Imgproc.contourArea((Mat) a);
			Double bArea = Imgproc.contourArea((Mat) b);
			
			// Return comparison.
			return bArea.compareTo(aArea);
		});
		
		// Return the sorted list.
		return list;
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
            Imgproc.circle(frame, center, radius + 1, new Scalar(0, 255, 0), -1);
		}
		
		// Return the updated frame.
		return frame;
	}
	
	/**
	 * Crops a copy of the passed frame to the rectangle.
	 *
	 * @param frame
	 * @param rect
	 * @return Mat
	 */
	public Mat cropToRectangle(Mat frame, RotatedRect rect)
	{
		// Create new destination frame.
		Mat destination = new Mat();
		
		// Check if image has rotated.
		double angle = rect.angle;
		Size rect_size = rect.size;
		if (rect.angle < -45.) {
			// Reverse rotation.
			angle += 90.0;
			
			// Swap width and height.
			double temp = rect_size.width;
			rect_size.width = rect_size.height;
			rect_size.height = temp;
		}

		// Find rectangle rotation values.
		Mat rotation = Imgproc.getRotationMatrix2D(rect.center, angle, 1.0);

		// Warp the frame to the rectangle angle.
		Imgproc.warpAffine(frame, destination, rotation, frame.size(), Imgproc.INTER_CUBIC);

		// Crop the frame to the rectangle size.
		Imgproc.getRectSubPix(frame, rect_size, rect.center, destination);
		
		// Return the updated frame.
		return destination;
	}
}
