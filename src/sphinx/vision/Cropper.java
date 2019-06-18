package sphinx.vision;

import java.util.List;

import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import sphinx.Config;

public class Cropper {
	
	/**
	 * The width of the cropped area.
	 *
	 * @var int
	 */
	private double width;
	
	/**
	 * The height of the cropped area.
	 *
	 * @var int
	 */
	private double height;
	
	/**
	 * The warper mat to perform the transformation.
	 *
	 * @var Mat
	 */
	private Mat warper;
	
	/**
	 * The frame with the isolated color.
	 *
	 * @var Frame
	 */
	public Frame frame = new Frame("Red - Cropper");
	
	/**
	 * Detect the playing area based on camera.
	 *
	 * @param camera
	 */
	public void detect(Camera camera) {
		// Capture frame from camera.
		camera.capture(this.frame);
		
		// Detect the playing area.
		this.detect(this.frame);
	}
	
	/**
	 * Detect the playing area based on frame.
	 *
	 * @param frame
	 */
	public void detect(Frame frame) {
		// Convert frame to HSV color space.
		frame.convertTo(this.frame, Imgproc.COLOR_BGR2HSV);

		// Isolate the red colors.
		this.frame.isolateRange(this.frame,
			Config.Colors.redLowLower,
			Config.Colors.redLowUpper,
			Config.Colors.redHighLower,
			Config.Colors.redHighUpper
		);
		
		// Find contours and skip if missing.
		List<MatOfPoint> contours = this.frame.sortedContours();
		if (contours.isEmpty()) return;
	
		// Find the corner points of the field
		int index = Math.min(Config.Obstacle.areaIndex, contours.size() - 1);
		MatOfPoint2f field = this.frame.approximate(contours.get(index));
		
		// Skip if field is not rectangle.
		if (field.total() < 4) return;
		
		// Sort the field corners.
		Point[] corners = this.sortCorners(field.toArray());
		
		// Get the minimum width and height.
		double[] dimensions = this.getDimensions(corners);
		
		// Save dimensions on instance.
		this.width = dimensions[0];
		this.height = dimensions[1];
		
		// Create warp material for source and destination corners.
		this.warper = Imgproc.getPerspectiveTransform(
			new MatOfPoint2f(corners),
			new MatOfPoint2f(
	    		new Point(0, 0),
	    		new Point(this.width - 1, 0),
	    		new Point(this.width - 1, this.height - 1),
	            new Point(0, this.height - 1)
			)
		);
	}
	
	/**
	 * Crop passed frame to found contour.
	 *
	 * @param frame
	 */
	public void crop(Frame frame) {
		// Skip if warper was not found.
		if (this.warper == null) return;
		
        // Warp source to found contour.
        Imgproc.warpPerspective(
    		frame.getSource(), frame.getSource(),
    		this.warper,
    		new Size(this.width, this.height)
		);
	}
	
	/**
	 * Sort the corners by TL, TR, BR, BL.
	 *
	 * @param corners
	 * @return Point[]
	 */
	private Point[] sortCorners(Point[] corners) {
		// Prepare list of sorted points.
		Point[] sorted = new Point[4];
		
		// PRepare list of sums and diffs.
		double[] sums = new double[4];
		double[] diffs = new double[4];
		
		// Find sums and diffs for all points.
		for(int i = 0; i < corners.length; i++) sums[i] = corners[i].x + corners[i].y;
		for(int i = 0; i < corners.length; i++) diffs[i] = corners[i].x - corners[i].y;
		
		// Find top-left and bottom-right points from sums.
		double[] smallestLargest = getSmallestLargest(sums);
		sorted[0] = corners[findIndex(sums, smallestLargest[0])]; // Top-left
		sorted[2] = corners[findIndex(sums, smallestLargest[1])]; // Bottom-right

		// Find top-right and bottom-left points from diffs.
		smallestLargest = getSmallestLargest(diffs);
		sorted[1] = corners[findIndex(diffs, smallestLargest[1])]; // Top-right
		sorted[3] = corners[findIndex(diffs, smallestLargest[0])]; // Bottom-left
		
		// Return sorted points.
		return sorted;
	}
	
	/**
	 * Returns the index in the array for a found value.
	 *
	 * @param array
	 * @param value
	 * @return int
	 */
	private int findIndex(double[] array, double value) {
		//  Return index if match value.
		for(int i = 0; i < array.length; i++) {
			if(array[i] == value) return i;
		}
		
		// Return failed result.
		return -1;
	}
	
	/**
	 * Returns the smallest and largest from array.
	 *
	 * @param sums
	 * @return
	 */
	private double[] getSmallestLargest(double[] sums) {
		// Asume both as smallest at largest number.
		double[] smallestLargest = {sums[0], sums[0]};
		
		// Iterate over remaining sums to find smallest and largest
		for(int i = 1; i < sums.length; i++) {
			if(sums[i] < smallestLargest[0]) smallestLargest[0] = sums[i];
			if(sums[i] > smallestLargest[1]) smallestLargest[1] = sums[i];
		}
		
		// Return the smallest and largest values.
		return smallestLargest; 
	}

	/**
	 * Returns the minimum width and height.
	 *
	 * @param corners
	 * @return double[]
	 */
	private double[] getDimensions(Point[] corners) {
		// Returns the minimum width and height.
		return new double[] {
			Math.min(lineLength(corners[0], corners[1]), lineLength(corners[3], corners[2])),
			Math.min(lineLength(corners[0], corners[3]), lineLength(corners[2], corners[1])),
		};
	}
	
	/**
	 * Returns the length between two points.
	 *
	 * @param A
	 * @param B
	 * @return double
	 */
	private double lineLength(Point A, Point B) {
		// Square the coordinate pairs
		double xDiff = Math.pow((A.x - B.x),2);
		double yDiff = Math.pow((A.y - B.y),2);
		
		// Root the sum of the differences
		double Diff = Math.sqrt(xDiff + yDiff);
		
		return Diff;
	}
	
	/**
	 * Returns if the cropper should detect.
	 *
	 * @param timer
	 * @return boolean
	 */
	public boolean shouldDetect(long timer) {
		return Config.Camera.shouldCrop
			&& (System.currentTimeMillis() - timer) <= (Config.Camera.croppingTime * 1000);
	}

}
