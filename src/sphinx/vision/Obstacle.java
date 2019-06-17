package sphinx.vision;

import java.util.List;
import org.opencv.core.Point;
import org.opencv.core.RotatedRect;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;
import org.opencv.core.MatOfPoint;

public class Obstacle {
	
	/**
	 * The points creating the obstacle.
	 *
	 * @var Point[4]
	 */
	public Point[] points = new Point[4];

	/*
	 * Attempt to detect the obstacle position.
	 *
	 * @param input
	 */
	public void detect(Frame input) {
		// Find largest contours or skip.
		List<MatOfPoint> obstacles = input.sortedContours();
		if (obstacles.isEmpty()) return;
		
		// Convert obstacle to rect and save points.
		RotatedRect rect = input.contourToRect(obstacles.get(1));
		rect.points(this.points);
	}

	/**
	 * Draw the obstacle points on the screen.
	 *
	 * @param frame
	 */
	public void draw(Frame frame) {
		// Loop through the four points.
		for (int j = 0; j < 4; j++) {
			// Draw the bounding lines.
			Imgproc.line(frame.getSource(), this.points[j], this.points[(j+1) % 4], new Scalar(255,0,0));
			
			// Draw the corner circles.
			Imgproc.circle(frame.getSource(), this.points[j], 3, new Scalar(255, 0, 255), -1);
		}
	}
	
}
