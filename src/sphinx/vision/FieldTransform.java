package sphinx.vision;

import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

public class FieldTransform {

	// Ca. linje 93 i Vision
	//RotatedRect rect = this.contourToRect(contours.get(1));
//	frame.loadSource(FieldTransform.transformField(contours.get(1), frame));
	
	// Crop the frame to the found playing area.
	/*frame.cropToRectangle(rect);
	hsv.cropToRectangle(rect);
	red.cropToRectangle(rect);
	blue.cropToRectangle(rect);*/

	/*RotatedRect rect = red.contourToRect(contours.get(1));
	
	// Crop the frame to the found playing area.
	frame.cropToRectangle(rect);
	hsv.cropToRectangle(rect);
	red.cropToRectangle(rect);
	blue.cropToRectangle(rect);*/
	
	public static Mat transformField(MatOfPoint fieldContour, Frame output) {
		// Find the corner points of the field
		MatOfPoint2f field = output.approximate(fieldContour);
//		Point[] fieldCorners = sort(field.toArray());
		Point[] fieldCorners = field.toArray();
		
		// Draw them on the frame
		draw(fieldCorners, output);
		
		// Find new width and height
		double[] dimensions = greatestDistance(fieldCorners);
		
		// Create matofpoint2f for source corners
        MatOfPoint2f src = new MatOfPoint2f (
        		fieldCorners[0], // BR - TL
                fieldCorners[1], // TR - TR
                fieldCorners[2], // TL - BR
                fieldCorners[3]  // BL
        );
        
		// Create matofpoint2f for destination corners
        // ref: TL, TR, BR, and BL
        // this BR, TR, TL, BL
        double test = dimensions[1] * 1.80;
        dimensions[0] *= 0.9;
        MatOfPoint2f dst = new MatOfPoint2f (
        		new Point(0, 0),							// TL
        		new Point(test, 0),				// TR - BL?
        		new Point(test, dimensions[0]),	// BR - BR
                new Point(0, dimensions[0])					// BL - TR?
        );
		
        // Create warpmat for source->destination transformation
        Mat warpMat = Imgproc.getPerspectiveTransform(src,dst);
        Mat destImage = new Mat();
        
        // Warp source mat to destination mat
        Imgproc.warpPerspective(output.getSource(), destImage, warpMat, output.getSource().size());
		return destImage;
	}
	
	private static Point[] sort(Point[] corners) {
		// Only squares
		if(corners.length > 4) return null;
		
		// Create x-y sum array
		double[] sums = new double[4];
		for(int i = 0; i < corners.length; i++) sums[i] = corners[i].x + corners[i].y;
		
		// Initialize new array
		Point[] sortedPoints = new Point[4];

		// Top-left and bottom-right points have respectively smallest and largest value
		double[] smallestLargest = getSmallestLargest(sums);
		
		// Top-left
		sortedPoints[0] = corners[findIndex(sums, smallestLargest[0])];
		// Bottom-right
		sortedPoints[2] = corners[findIndex(sums, smallestLargest[1])];
		
		
		// Top-right and bottom-left can be found from min/max from x-y
		double[] diffs = new double[4];
		for(int i = 0; i < corners.length; i++) diffs[i] = corners[i].x - corners[i].y;
		smallestLargest = getSmallestLargest(diffs);
		
		// Top-right
		sortedPoints[1] = corners[findIndex(diffs, smallestLargest[0])];
		
		// Bottom-left
		sortedPoints[3] = corners[findIndex(diffs, smallestLargest[1])];
		
		return sortedPoints;
		
	}
	
	private static int findIndex(double[] array, double value) {
		for(int i = 0; i < array.length; i++) {
			if(array[i] == value) return i;
		}
		return -1;
	}
	
	private static double[] getSmallestLargest(double[] sums) {
		// Asume first as both smallest at largest number
		double[] smallestLargest = {sums[0], sums[0]};
		
		// Iterate over remaining sums to find smallest and largest
		for(int i = 1; i < sums.length; i++) {
			if(sums[i] < smallestLargest[0]) smallestLargest[0] = sums[i];
			if(sums[i] > smallestLargest[1]) smallestLargest[1] = sums[i];
		}
		
		return smallestLargest;
	}

	private static void draw(Point[] corners, Frame frame) {
		// Draw small circles for each corner.
		for (int i = 0; i < 4; i++) {
			if (corners[i] == null) continue;
			Scalar color[] = { 
					new Scalar(255,0,0),
					new Scalar(0,255,0),
					new Scalar(0,0,255),
					new Scalar(255,0,255),
			};
			Imgproc.circle(frame.getSource(), corners[i], 5, color[i]);
		}
	}
	
	private static double[] greatestDiagonalDistance(Point[] corners) {
		// First Axis
//		double A1 = lineLength (corners[0], corners[3]);
//		double A2 = lineLength (corners[1], corners[2]);
		double A1 = lineLength (corners[0], corners[1]);
		double A2 = lineLength (corners[0], corners[2]);
		double A3 = lineLength (corners[0], corners[3]);
		
		double A = A1;
		
		if(A < A2) A = A2;
		if(A < A3) A = A3;
		
		double[] dim = {0.0, 0.0};
		if (A == A1)	   dim = findVector(corners[0], corners[1]);
		else if (A == A2)  dim = findVector(corners[0], corners[2]);
		else if (A == A3)  dim = findVector(corners[0], corners[3]);
				
		
		System.out.printf("W:%f H: %f\n", dim[0], dim[1]);
		// Width, height returned
		return dim;
	}
	

	private static double[] findVector(Point A, Point B) {
		double[] d = {Math.abs(A.x-B.x) , Math.abs(A.y-B.y)};
		return d;
	}
	
	private static double[] greatestDistance(Point[] corners) {
		// First Axis
//		double A1 = lineLength (corners[0], corners[3]);
//		double A2 = lineLength (corners[1], corners[2]);
		double A1 = lineLength (corners[0], corners[1]);
		double A2 = lineLength (corners[3], corners[2]);

		double A = A1 < A2 ? A1 : A2;
		System.out.printf("A1: %f, A2: %f, A: %f\n", A1,A2,A);
		
		// Second Axis
		double B1 = lineLength (corners[0], corners[3]);
		double B2 = lineLength (corners[2], corners[1]);
		double B = B1 < B2 ? B1 : B2;
		System.out.printf("B1: %f, B2: %f, B: %f\n", B1,B2,B);
		
		double[] dim;
		
		if(A > B) {
			dim = new double[]{A,B};
		} else {
			dim = new double[]{B,A};
		}
		System.out.printf("W:%f H: %f\n", A, B);
		// Width, height returned
		return dim;
	}
	
	private static double lineLength(Point A, Point B) {
		// Square the coordinate pairs
		double xDiff = Math.pow(Math.abs((A.x - B.x)),2);
		double yDiff = Math.pow(Math.abs((A.y - B.y)),2);
		
		// Root the sum of the differences
		double Diff = Math.sqrt(xDiff + yDiff);
		
		return Diff;
	}
}
