package sphinx.vision;

import java.util.List;
import java.util.Comparator;
import java.util.ArrayList;

import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.imgproc.Imgproc;

public class Contour {
	
	/**
	 * Returns a list of contours on the frame.
	 *
	 * @param frame
	 * @param method
	 * @return List<MatOfPoint>
	 */
	public static List<MatOfPoint> getContours(Frame frame, int method, Mat hierarchy) {
		// Prepare array list for contours.
		List<MatOfPoint> contours = new ArrayList<>();
		
		// Find contours on frame.
		Imgproc.findContours(
			frame.getSource(), contours, hierarchy,	// Source & Destination
			method, Imgproc.CHAIN_APPROX_SIMPLE		// Methods
		);
		
		// Return the contour list.
		return contours;
	}
	
	/**
	 * Returns a list of contours on the frame.
	 *
	 * @param frame
	 * @param method
	 * @return List<MatOfPoint>
	 */
	public static List<MatOfPoint> getContours(Frame frame, int method) {
		return Contour.getContours(frame, method, new Mat());
	}

	/**
	 * Returns a list of contours sorted by area.
	 *
	 * @param frame
	 * @param method
	 * @return List<MatOfPoint>
	 */
	public static List<MatOfPoint> sortedContours(Frame frame, int method, Mat hierarchy) {
		// Get contours from current frame.
		List<MatOfPoint> contours = Contour.getContours(frame, method, hierarchy);
		
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
	 * @param frame
	 * @param method
	 * @return List<MatOfPoint>
	 */
	public static List<MatOfPoint> sortedContours(Frame frame, int method) {
		return Contour.sortedContours(frame, method, new Mat());
	}
	
}
