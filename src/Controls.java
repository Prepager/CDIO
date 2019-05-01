import org.opencv.core.Mat;
import org.opencv.core.Point;

public class Controls {
	/**
	 * Init the controls object.
	 */
	public Controls() {
		// @wip - init
	}
	
	/**
	 * Handle controls for frame.
	 *
	 * @param area
	 * @param circles
	 */
	public void run(Point[] obstacle, Mat circles, int width, int height) {
		// @wip - control
		System.out.println("W: " + width + " H: " + height);
		for(int i = 0; i < 4; i++)
			System.out.println("Obstacle corners: " + obstacle[i].x + "," + obstacle[i].y);
	}
}
