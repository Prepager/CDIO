package sphinx.vision;

import java.util.List;

import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

import sphinx.PositionTransform;

public class Vehicle {
	
	protected Point front;
	
	protected Point center;

	protected Point[] points;
	
	protected MatOfPoint2f vehicle;
	
	protected PositionTransform transformer = null;
	
	/**
	 * @wip
	 *
	 * @param input
	 * @param output
	 */
	public void detect(Frame input, Frame output) {
		// Find largest triangle and return out if missing.
		MatOfPoint2f triangle = this.findTriangle(input);
		if (triangle == null) return;
		
		// Get list of points from triangle.
		this.points = triangle.toArray();
		
		// @wip
		this.draw(output);
		
		// Find frame width and height.
		double width = input.getSource().cols();
		double height = input.getSource().rows();
		
		// @wip
		if (this.transformer == null) {
			this.transformer = new PositionTransform(width, height);
		}
		
		// Transform the found points.
		this.transformer.transformPosition(this.points, width, height);
		
		// @wip
		this.draw(output);

		// Find the front point in the triangle.
		this.front = this.findFront(this.points);
	}
	
	/**
	 * @wip
	 */
	public void draw(Frame frame) {
		// Draw small circles for each corner.
		for (int i = 0; i < 3; i++) {
			if (this.points[i] == null) continue;
			Imgproc.circle(frame.getSource(), this.points[i], 3, new Scalar(0, 0, 255));
		}
		
		// Draw larger circle for front point.
		if (this.front != null) {
			Imgproc.circle(frame.getSource(), this.front, 8, new Scalar(0, 0, 255));
		}
	}
	
	/**
	 * Returns the front point of the triangle.
	 *
	 * @param points
	 * @return Point
	 */
	protected Point findFront(Point[] points) {
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
	protected MatOfPoint2f findTriangle(Frame frame) {
		// Get all contours from frame.
		List<MatOfPoint> contours = Contour.sortedContours(frame, Imgproc.RETR_TREE);
		
		// Prepare vehicle and approx holder variable.
		MatOfPoint2f approx, vehicle = null;
		
		// Loop through all the found contours.
		for (MatOfPoint contour: contours) {
			// Approximate the contour poly.
			approx = Contour.approximate(contour);
			
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
	
}
