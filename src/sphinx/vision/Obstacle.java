package sphinx.vision;

import java.util.List;
import org.opencv.core.Point;
import org.opencv.core.RotatedRect;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

import sphinx.Config;

import org.opencv.core.MatOfPoint;

public class Obstacle {
	
	/**
	 * The points creating the obstacle.
	 *
	 * @var Point[4]
	 */
	public Point[] points = new Point[4];
	
	/**
	 * The frame with the isolated color.
	 *
	 * @var Frame
	 */
	public Frame frame = new Frame("Red - Obstacle");

	/*
	 * Attempt to detect the obstacle position.
	 *
	 * @param input
	 */
	public void detect(Frame input) {
		// Isolate the red color from the image.
		input.isolateRange(this.frame,
			Config.Colors.redLowLower,
			Config.Colors.redLowUpper,
			Config.Colors.redHighLower,
			Config.Colors.redHighUpper
		);

		// Find largest contours or skip.
		List<MatOfPoint> obstacles = this.frame.sortedContours();
		if (obstacles.isEmpty()) return;
		
		// Convert obstacle to rect and save points.
		int index = Math.min(Config.Obstacle.crossIndex, obstacles.size() - 1);
		RotatedRect rect = this.frame.contourToRect(obstacles.get(index));
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
			// Skip if point is not set.
			if (this.points[j] == null) continue;
			
			// Draw the bounding lines.
			if (this.points[(j+1) % 4] != null) {
				Imgproc.line(frame.getSource(), this.points[j], this.points[(j+1) % 4], new Scalar(255,0,0));
			}
			
			// Draw the corner circles.
			Imgproc.circle(frame.getSource(), this.points[j], 3, new Scalar(255, 0, 255), -1);
		}
	}
	
}
