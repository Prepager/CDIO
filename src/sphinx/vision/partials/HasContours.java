package sphinx.vision.partials;

import java.util.List;
import java.util.Comparator;
import java.util.ArrayList;

import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.imgproc.Imgproc;

abstract public class HasContours {
	
	/**
	 * The frame source mat.
	 *
	 * @var Mat
	 */
	protected Mat source = new Mat();
	
	/**
	 * Returns a list of contours on the frame.
	 *
	 * @param method
	 * @return List<MatOfPoint>
	 */
	public List<MatOfPoint> getContours(int method, Mat hierarchy) {
		// Prepare array list for contours.
		List<MatOfPoint> contours = new ArrayList<>();
		
		// Find contours on frame.
		Imgproc.findContours(
			this.source, contours, hierarchy,	// Source & Destination
			method, Imgproc.CHAIN_APPROX_SIMPLE	// Methods
		);
		
		// Return the contour list.
		return contours;
	}
	
	/**
	 * Returns a list of contours on the frame.
	 *
	 * @param method
	 * @return List<MatOfPoint>
	 */
	public List<MatOfPoint> getContours(int method) {
		return this.getContours(method, new Mat());
	}

	/**
	 * Returns a list of contours sorted by area.
	 *
	 * @param method
	 * @return List<MatOfPoint>
	 */
	public List<MatOfPoint> sortedContours(int method, Mat hierarchy) {
		// Get contours from current frame.
		List<MatOfPoint> contours = this.getContours(method, hierarchy);
		
		// Sort the contours by area.
		contours.sort(new Comparator<MatOfPoint>() {
		    @Override
		    public int compare(MatOfPoint a, MatOfPoint b) {
				// Find area of two contours.
				Double aArea = Imgproc.contourArea((Mat) a);
				Double bArea = Imgproc.contourArea((Mat) b);
				
				// Return comparison.
				return bArea.compareTo(aArea);
		    }
		});
		
		// Return the sorted contour list.
		return contours;
	}

	/**
	 * Returns a list of contours sorted by area.
	 *
	 * @return List<MatOfPoint>
	 */
	public List<MatOfPoint> sortedContours() {
		return this.sortedContours(Imgproc.RETR_TREE, new Mat());
	}

	/**
	 * Returns a list of contours sorted by area.
	 *
	 * @param method
	 * @return List<MatOfPoint>
	 */
	public List<MatOfPoint> sortedContours(int method) {
		return this.sortedContours(method, new Mat());
	}
	
	/**
	 * Returns an approximated poly for the contour.
	 *
	 * @param contour
	 * @return MatOfPoint2f
	 */
	public MatOfPoint2f approximate(MatOfPoint contour) {
		// Calculate contour epsilon.
		MatOfPoint2f contour2f = new MatOfPoint2f(contour.toArray());
		double epsilon = .1 * Imgproc.arcLength(contour2f, true);
		
		// Prepare approx holder variable.
		MatOfPoint2f approx = new MatOfPoint2f();
		
		// Approximate the contour poly.
		Imgproc.approxPolyDP(new MatOfPoint2f(contour.toArray()), approx, epsilon, true);

		// Return the approximated poly.
		return approx;
	}
	
}
