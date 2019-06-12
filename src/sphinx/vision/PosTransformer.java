package sphinx.vision;

import java.util.Arrays;

import org.opencv.core.Point;

public class PosTransformer {

	private Point center = new Point();
	
	/**
	 * The height of the object.
	 *
	 * @var double
	 */
	private double objHeight;
	
	/**
	 * The height of the source.
	 *
	 * @var double
	 */
	private double sourceHeight;
	
	/**
	 * Constructor for position transformation with sizes.
	 *
	 * @param objHeight
	 * @param sourceHeight
	 */
	public PosTransformer(double objHeight, double sourceHeight) {
		this.objHeight = objHeight;
		this.sourceHeight = sourceHeight;
	}

	/**
	 * Recalculates/transforms the z-position of a given plane.
	 *
	 * @param Point[] corner locations of object as point array
	 * @param double width of the playing field
	 * @param double height of the car
	 */
	public Point[] transformPosition(Point[] objectPosition, double fieldWidth, double fieldHeight) {
		// Set field center point from sizes.
		this.center.set(new double[] { fieldWidth/2, fieldHeight/2 });
		
		// Find centroid - geometric center of car.
		Point carCenter = this.findCenter(objectPosition);

		// Find length of line between center and perceived location.
		double UV = this.lineLength(carCenter, center);
		double TV = sourceHeight;
		
		// Find the angle from the camera to the perceived location.
		double theta = Math.atan2(UV, TV);
		
		// Calculate error from perceived location and actual z-location.
		double AC = objHeight;
		double BC = Math.tan(theta) * AC;
		
		// Find length from center to z-axis transformed car location.
		double newLength = UV - BC;
		
		// Move coordinate system to move center to origo.
		Point[] transformedObjectPosition = Arrays.copyOf(
			this.transformCoordinateSystem(objectPosition),
			objectPosition.length
		);
		
		// Create scalar and relocate the location of object (perceived loc / actual loc).
		double scalar = newLength / UV;
		for(int i = 0; i < transformedObjectPosition.length; i++) {
			transformedObjectPosition[i].set(new double[] {
				transformedObjectPosition[i].x * scalar,
				transformedObjectPosition[i].y * scalar					
			});
		}
		
		// Return to old coordinate system.
		return this.inverseTransformCoordinateSystem(transformedObjectPosition);
	}

	/**
	 * Transforms a point array back to its original coordinate system.
	 *
	 * @param Point[] the points, that defines the given geometric object
	 */	
	private Point[] inverseTransformCoordinateSystem(Point[] objectPosition) {
		Point[] transformedObjectPosition = objectPosition;
		for(int i = 0; i < objectPosition.length; i++) {
			objectPosition[i].set(new double[] {
				objectPosition[i].x += center.x,
				objectPosition[i].y += center.y					
			});
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
			transformedObjectPosition[i].set(new double[] {
				objectPosition[i].x - center.x,
				objectPosition[i].y - center.y
			});
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
		// Square the coordinate pairs.
		double xDiff = Math.pow((A.x - B.x), 2);
		double yDiff = Math.pow((A.y - B.y), 2);
		
		// Root the sum of the differences.
		return Math.sqrt(xDiff + yDiff);
	}

	/**
	 * Finds the center of a geometric form defined by a point array.
	 *
	 * @param point[] the points (corners) of the geometric form
	 */
	private Point findCenter(Point[] positions) {
		// Prepare new center point.
		Point center = new Point(0, 0);
		
		// Loop through the positions.
		for(int i = 0; i < positions.length; i++) {
			// Add new position.
			center.set(new double[] {
				(center.x + positions[i].x),
				(center.y + positions[i].y) 					
			});
		}
		
		// Calculate mean of positions.
		center.set(new double[] {
			(center.x / positions.length),
			(center.y / positions.length) 
		});
		
		// Return the center point.
		return center;
	}
	
}
