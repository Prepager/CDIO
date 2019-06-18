package sphinx.elements;

import java.util.List;

import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;
import org.opencv.imgproc.Moments;

import sphinx.Config;
import sphinx.vision.Frame;
import sphinx.vision.Projector;

public class Vehicle {
	
	/**
	 * The vehicles back point.
	 *
	 * @var Point
	 */
	public Point back;

	/**
	 * The vehicles front point.
	 *
	 * @var Point
	 */
	public Point front;
	
	/**
	 * The vehicles center point.
	 *
	 * @var Point
	 */
	public Point center;

	/**
	 * The three points of the triangle.
	 *
	 * @var Point[]
	 */
	public Point[] points;
	
	/**
	 * The rotation of the vehicle.
	 *
	 * @var Double
	 */
	public Double rotation = 0.0;
	
	/**
	 * The triangle points all combined.
	 *
	 * @var MatOfPoint2f
	 */
	public MatOfPoint2f triangle;
	
	/**
	 * The frame with the isolated color.
	 *
	 * @var Frame
	 */
	public Frame frame = new Frame("Blue - Vehicle");
	
	/**
	 * The position transformer instance.
	 *
	 * @var PositionTransform
	 */
	private Projector projector = new Projector(
		Config.Position.carHeight,
		Config.Position.cameraHeight
	);
	
	/**
	 * Attempt to detect the vehicle position.
	 *
	 * @param input
	 */
	public void detect(Frame input) {
		// Isolate the blue color from the image.
		input.isolateRange(this.frame,
			Config.Colors.blueLower,
			Config.Colors.blueUpper
		);
		
		// Find largest triangle and return out if missing.
		MatOfPoint2f triangle = this.findTriangle(this.frame);
		if (triangle == null) return;
		
		// Get list of points from triangle.
		this.points = triangle.toArray();
		
		// Find frame width and height.
		double width = this.frame.getSource().cols();
		double height = this.frame.getSource().rows();
		
		// Transform the found points.
		this.projector.transformPosition(this.points, width, height);
		this.triangle = new MatOfPoint2f(this.points);

		// Find the front point in the triangle.
		this.front = this.findFront(this.points);
		
		// Find the back point in the triangle.
		this.back = this.findBack(this.points);
		
		// Find the center point of the triangle.
		this.center = this.findCenter(this.triangle);
		
		// Find the rotation of the triangle.
		this.rotation = this.findRotation(this.front, this.back);
	}

	/**
	 * Draw the vehicle points on the screen.
	 *
	 * @param frame
	 */
	public void draw(Frame frame) {
		// Draw vehicle corner points.
		if (this.points != null) {
			for (int i = 0; i < 3; i++) {
				if (this.points[i] == null) continue;
				Imgproc.circle(frame.getSource(), this.points[i], 3, new Scalar(0, 0, 255));
			}
		}
		
		// Draw the vehicles back point.
		if (this.back != null) {
			Imgproc.circle(frame.getSource(), this.back, 3, new Scalar(0, 0, 255));
		}
		
		// Draw the vehicles front point.
		if (this.front != null) {
			Imgproc.circle(frame.getSource(), this.front, 6, new Scalar(0, 0, 255));
		}
		
		// Draw the vehicles center point.
		if (this.center != null) {
			Imgproc.circle(frame.getSource(), this.center, 3, new Scalar(255, 0, 0), Imgproc.FILLED);
		}
	}
	
	/**
	 * Returns the front point of the triangle.
	 *
	 * @param points
	 * @return Point
	 */
	private Point findFront(Point[] points) {
		// Loop through the passed points.
		double[] dists = new double[3];
		for (int i = 0; i < 3; i++) {
			// Get outer-loop point.
			Point a = points[i];
			
			// Loop through rest of points.
			for (int k = 0; k < 3; k++) {
				// Continue if current outer.
				if (i == k) continue;
				
				// Get inner-loop point.
				Point b = points[k];
				
				// Add distance between out and inner.
				dists[i] += Math.sqrt(
					Math.pow(a.x - b.x, 2) + Math.pow(a.y - b.y, 2)
				);
			}
		}
		
		// Prepare index and largest holder.
		int index = 0;
		double largest = 0;
		
		// Loop through the found distances.
		for (int i = 0; i < 3; i++) {
			// Skip if dist is lower than largest.
			if (dists[i] < largest) continue;
			
			// Save the index and largest value.
			index = i;
			largest = dists[i];
		}
		
		// Return the largest point index.
		return points[index];
	}

	/**
	 * Find the largest triangle on the passed frame.
	 *
	 * @param frame
	 * @return MatOfPoint2f
	 */
	private MatOfPoint2f findTriangle(Frame frame) {
		// Get all contours from frame.
		List<MatOfPoint> contours = frame.sortedContours();
		
		// Prepare vehicle and approx holder variable.
		MatOfPoint2f approx, vehicle = null;
		
		// Loop through all the found contours.
		for (MatOfPoint contour: contours) {
			// Approximate the contour poly.
			approx = frame.approximate(contour);
			
			// Check if approximate found and has 3 points.
			if (approx != null && approx.total() == 3) {
				// Save approximate and break out.
				vehicle = approx;
				break;
			}
		}
		
		// Return the found vehicle.
		return vehicle;
	}
	
	/**
	 * Find the center point of the triangle contour.
	 *
	 * @param triangle
	 * @return Point
	 */
	private Point findCenter(MatOfPoint2f triangle) {
		// Find moments for the triangle.
		Moments moments = Imgproc.moments(triangle);
		
		// Create point and set x, and y positions.
		Point center = new Point();
		center.x = moments.get_m10() / moments.get_m00();
		center.y = moments.get_m01() / moments.get_m00();
		
		// Return the found center point.
		return center;
	}

	/**
	 * Find the center point between the back triangle points.
	 *
	 * @param points
	 * @return Point
	 */
	private Point findBack(Point[] points) {
		// Find the first point.
		Point a = this.points[0];
		if (a == this.front) {
			a = this.points[1];
		}
		
		// Find the second point.
		Point b = this.points[1];
		if (b == this.front || b == a) {
			b = this.points[2];
		}
		
		// Create point and set x, and y positions.
		Point center = new Point();
		center.x = (a.x + b.x) / 2;
		center.y = (a.y + b.y) / 2;
		
		// Return the found center point.
		return center;
	}
	
	/**
	 * Find the rotation between the two points.
	 *
	 * @param a
	 * @param b
	 * @return double
	 */
	public double findRotation(Point a, Point b) {
		// Find rotation in radians using acttan2.
		double rad = Math.atan2(a.y - b.y, a.x - b.x);

		// Remove negative rotation.
		if (rad < 0) {
			rad += 2 * Math.PI;
		}
		
		// Convert the rotation to degrees.
		return rad * (180 / Math.PI);
	}
	
}
