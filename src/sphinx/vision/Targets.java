package sphinx.vision;

import java.util.ArrayList;

import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import sphinx.Config;

public class Targets {
	
	/**
	 * The mat containing the circles.
	 *
	 * @var Mat
	 */
	public Mat circles;
	
	/**
	 * The points of the circles.
	 *
	 * @var 
	 */
	public ArrayList<Point> points = new ArrayList<Point>();
	
	/**
	 * The frame with the isolated color.
	 *
	 * @var Frame
	 */
	public Frame frame = new Frame("White - Targets");
	
	/**
	 * The minimum radius of the circles.
	 *
	 * @var int
	 */
	private int minRadius = 7;
	
	/**
	 * The maximum radius of the circles.
	 *
	 * @var int
	 */
	private int maxRadius = 14;
	
	/**
	 * The minimum distance between the circles.
	 *
	 * @var int
	 */
	private int minDistance = 5;
	
	/**
	 * The matrix size used for the dilation (3x3 matrix).
	 *
	 * @var int
	 */
	private int kernelSize = 3;
	
	/**
	 * The higher threshold for the canny edge detection.
	 * Describes how strong the edges have to be.
	 *
	 * @var int
	 */
	private int param1 = 50 * 3;
	
	/**
	 * The accumulator threshold for the circle centers at the detection stage.
	 * Describes the amount of edge points to declare it as a circle.
	 *
	 * @var int
	 */
	private int param2 = 14;
	
	/**
	 * The accumulator resolution to the image resolution.
	 * The lower the more unreliable. 1 = same res, 2 = half res.
	 *
	 * @var int
	 */
	private double DP = 1.4;
	
	/*
	 * Attempt to detect the target positions.
	 *
	 * @param input
	 */
	public void detect(Frame input) {		
		// Reset lists before detection.
		this.points.clear();
		this.circles = new Mat();

		// Isolate the white color from the image.
		input.isolateRange(this.frame,
			Config.Colors.whiteLower,
			Config.Colors.whiteUpper
		);

		// Find structured list of ellipse elements.
		Mat element = Imgproc.getStructuringElement(
			Imgproc.CV_SHAPE_ELLIPSE,
			new Size(2 * this.kernelSize + 1, 2 * this.kernelSize + 1),
			new Point(this.kernelSize, this.kernelSize)
		);
		
		// Dilate the found elements.
		Imgproc.dilate(this.frame.getSource(), this.frame.getSource(), element);

		// Find and save the circles in passed frame.
		Imgproc.HoughCircles(
			this.frame.getSource(), this.circles, Imgproc.HOUGH_GRADIENT,
			this.DP, this.minDistance,
			this.param1, this.param2,
			this.minRadius, this.maxRadius
		);
		
		// Loop through the circle length.
    	for(int i = 0; i < this.circles.cols(); i++) {
    		// Find center point of the target.
    		double[] center = circles.get(0, i);
    		
    		// Add new target center point to list.
    		this.points.add(new Point(center[0], center[1]));
    	}
	}

	/**
	 * Draw the target points on the screen.
	 *
	 * @param frame
	 */
	public void draw(Frame frame) {
		// Loop though the circles.
		for (int x = 0; x < this.circles.cols(); x++) {
			// Get the current circle.
            double[] c = this.circles.get(0, x);

            // Create new point for circle.
            Point center = new Point(Math.round(c[0]), Math.round(c[1]));

            // Add circle to center based on radius.
            int radius = (int) Math.round(c[2]);
            Imgproc.circle(frame.getSource(), center, radius + 1, new Scalar(0, 255, 0), -1);
            Imgproc.circle(frame.getSource(), center, 3, new Scalar(0, 0, 100), -1);
		}
	}
	
}
