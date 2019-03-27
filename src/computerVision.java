import java.util.List;
import java.util.ArrayList;

import org.opencv.core.*;
import org.opencv.highgui.*;
import org.opencv.imgproc.Imgproc;
import org.opencv.videoio.VideoCapture;
import org.opencv.videoio.Videoio;

public class computerVision {
	
	public static boolean webcam = false;
	
	public static int blurSize = 5;
	public static int minRadius = 0;
	public static int maxRadius = 10;
	public static int minDistance = 10;
	public static int cannyThreshold = 30; //50;
	
	public static void main(String[] args) {
		// Load the OpenCV library.
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
		
		// Create new video capture object.
		VideoCapture capture = new VideoCapture(
			webcam ? "0" : "./src/video.mov"
		);
		
		// Set capture width and height.
		capture.set(Videoio.CAP_PROP_FRAME_WIDTH, 640);
		capture.set(Videoio.CAP_PROP_FRAME_HEIGHT, 480);
		capture.set(Videoio.CAP_PROP_AUTOFOCUS, 0);

		// Prepare capture frame holder.
		Mat frame = new Mat();
		Mat gray  = new Mat();
		Mat canny = new Mat();

		// Start processing loop.
		while (true) {
			// Read in frame from capture.
			if (! capture.read(frame) && ! webcam) {
				capture.set(Videoio.CAP_PROP_POS_FRAMES, 0);
				capture.read(frame);
			}

			// Add blur to frame.
			Imgproc.medianBlur(frame, frame, blurSize);

			// Convert frame to gray.
			Imgproc.cvtColor(frame, gray, Imgproc.COLOR_BGR2GRAY);

			// Convert gray to canny.
			Imgproc.Canny(gray, canny, cannyThreshold, cannyThreshold * 3);

			// Find circles via hough circles gradient.
			/*Mat circles = new Mat();
			Imgproc.HoughCircles(gray, circles, Imgproc.HOUGH_GRADIENT, 1, minDistance, cannyThreshold * 3, 20, minRadius, maxRadius);

			// Loop though the circle columns.
			for (int x = 0; x < circles.cols(); x++) {
				// Get the current circle.
	            double[] c = circles.get(0, x);

	            // Create new point for circle.
	            Point center = new Point(Math.round(c[0]), Math.round(c[1]));

	            // Add circle to center based on radius.
	            int radius = (int) Math.round(c[2]);
	            Imgproc.circle(frame, center, radius + 1, new Scalar(0, 255, 0), -1);
			}*/

			
			
			
			// @wip
			Mat hierarchy = new Mat();
			List<MatOfPoint> contours = new ArrayList<>();
			Imgproc.findContours(canny, contours, hierarchy, Imgproc.RETR_TREE, Imgproc.CHAIN_APPROX_SIMPLE);

			// @wip
			double maxArea = 0;
			MatOfPoint maxContour = new MatOfPoint();
			
			for(int i = 0; i< contours.size(); i++) {
				//
				double area = Imgproc.contourArea(contours.get(i));
				if (area > maxArea) {
					maxArea = area;
					maxContour = contours.get(i);
				}
			}
			
			//
	        Rect rect = Imgproc.boundingRect(maxContour);
			Imgproc.rectangle(frame, new Point(rect.x, rect.y), new Point(rect.x + rect.width, rect.y + rect.height), new Scalar(255, 0, 0, 255), 3);

			
			
			
			// Show the frame on the screen.
			HighGui.imshow("Frame", frame);
			HighGui.imshow("Canny", canny);

			// Move frames into view.
			HighGui.moveWindow("Frame", 0, 0);
			HighGui.moveWindow("Canny", 640, 0);

			// Add small delay.
			HighGui.waitKey(1);
		}
	}
}
