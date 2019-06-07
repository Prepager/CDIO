package sphinx;

import java.util.Arrays;

import org.opencv.core.Point;

public class PositionTransform {

	private Point center = new Point();
	private double carHeight = 74.29; // 23 cm * 3,23 px/cm = 74,29
	private double camHeight = 529.72; // 164 cm * 3,23 px/cm = 529,72
	
	/**
	 * Constructor for position transformation with default carHeigt preserved.
	 *
	 * @param double height of the playing field
	 * @param double width of the playing field
	 */
	public PositionTransform(double fieldHeight, double fieldWidth) {
		this.center.set(doubleArray(fieldWidth/2,fieldHeight/2));
	}

	/**
	 * Constructor for position transformation which overrides the car height.
	 *
	 * @param double height of the playing field
	 * @param double width of the playing field
	 * @param double height of the car
	 */
	public PositionTransform(double fieldHeight, double fieldWidth, double carHeight) {
		this.center.set(doubleArray(fieldWidth/2,fieldHeight/2));
		this.carHeight = carHeight;
	}
	
	/**
	 * Constructor for position transformation which overrides the car height.
	 *
	 * @param double height of the playing field
	 * @param double width of the playing field
	 * @param double height of the car
	 * @param double height camera
	 */
	public PositionTransform(double fieldHeight, double fieldWidth, double carHeight, double camHeight) {
		this.center.set(doubleArray(fieldWidth/2,fieldHeight/2));
		this.carHeight = carHeight;
		this.camHeight = camHeight;
	}
	
	
	/**
	 * Recalculates/transforms the z-position of a given plane.
	 *
	 * @param Point[] corner locations of object as point array
	 * @param double width of the playing field
	 * @param double height of the car
	 */
	public Point[] transformPosition(Point[] objectPosition, double fieldWidth, double fieldHeight) {
		this.center.set(doubleArray(fieldWidth/2,fieldHeight/2));
		
		// Find centroid - geometric center of car
		Point carCenter = findCenter(objectPosition);

		// Find length of line between points
		double UV = lineLength(carCenter, center); // Length from center to perceived car location
		double TV = camHeight;
		
		// Find the angle from the camera to the perceived location
		double theta = Math.atan2(UV,TV);
		
		// Calculate error from perceived location and actual z-location
		double AC = carHeight;
		double BC = Math.tan(theta)*AC;
		
		// Find length from center to z-axis transformed car location
		double newLength = UV - BC;
		
		// Move coordinate system to move center to origo
		Point[] transformedObjectPosition = Arrays.copyOf(transformCoordinateSystem(objectPosition), objectPosition.length);
		
		
		// Create scalar and relocate the location of object (perceived loc / actual loc)
		double scalar = newLength / UV;
		for(int i = 0; i < transformedObjectPosition.length; i++) {
			transformedObjectPosition[i].set(doubleArray(
					transformedObjectPosition[i].x * scalar,
					transformedObjectPosition[i].y * scalar					
			));
		}
		
		// Return to old coordinate system
		transformedObjectPosition = inverseTransformCoordinateSystem(transformedObjectPosition);
		
		
		return transformedObjectPosition;
	}

	/**
	 * Transforms a point array back to its original coordinate system.
	 *
	 * @param Point[] the points, that defines the given geometric object
	 */	
	private Point[] inverseTransformCoordinateSystem(Point[] objectPosition) {
		Point[] transformedObjectPosition = objectPosition;
		for(int i = 0; i < objectPosition.length; i++) {
			objectPosition[i].set(doubleArray(
					objectPosition[i].x += center.x,
					objectPosition[i].y += center.y					
			));
		}
		
		return transformedObjectPosition;
	}

	/**
	 * Transforms a point array to a coordinate system with center as origo.
	 *
	 * @param Point[] the points, that defines the given geometric object
	 */	
	private Point[] transformCoordinateSystem(Point[] objectPosition) {
		Point[] transformedObjectPosition = objectPosition;
		for(int i = 0; i < objectPosition.length; i++) {
			// Move location to coordinate system with center in origo 
			transformedObjectPosition[i].set(doubleArray(
					objectPosition[i].x - center.x,
					objectPosition[i].y - center.y
			));
		}
		return transformedObjectPosition;
	}

	/**
	 * Calculates the length between two points.
	 *
	 * @param Point A
	 * @param Point B
	 */
	private double lineLength(Point A, Point B) {
		// Square the coordinate pairs
		double xDiff = Math.pow((A.x - B.x),2);
		double yDiff = Math.pow((A.x - B.x),2);
		
		// Root the sum of the differences
		double Diff = Math.sqrt(xDiff + yDiff);
		
		return Diff;
	}

	/**
	 * Finds the center of a geometric form defined by a point array.
	 *
	 * @param point[] the points (corners) of the geometric form
	 */
	private Point findCenter(Point[] carPosition) {
		
		Point carCenter = new Point(0,0);
		
		for(int i = 0; i < carPosition.length; i++) {
			// Save new position 
			carCenter.set(doubleArray(
					(carCenter.x + carPosition[i].x),
					(carCenter.y + carPosition[i].y) 					
			));
		}
		
		// Calculate mean 
		carCenter.set(doubleArray(
				(carCenter.x / carPosition.length),
				(carCenter.y / carPosition.length) 
		));
		
		return carCenter;
	}
	
	/**
	 * Helper function to convert two doubles to a double array.
	 *
	 * @param double first value to be added to the array
	 * @param double second value to be added to the array
	 */
	private double[] doubleArray(double first, double second) {
		double r[] = {first, second};
		return r;
	}
	
}
