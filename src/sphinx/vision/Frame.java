package sphinx.vision;

import org.opencv.core.Mat;
import org.opencv.core.Core;
import org.opencv.core.Size;
import org.opencv.highgui.HighGui;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;
import org.opencv.core.RotatedRect;

public class Frame {
	
	protected Mat source = new Mat();
	
	protected String name = "Untitled Frame";

	/**
	 * Default frame constructor.
	 */
	public Frame() {
		// Left blank intentionally.
	}
	
	/**
	 * Frame constructor to set frame name.
	 *
	 * @param name
	 */
	public Frame(String name) {
		this.name = name;
	}
	
	/**
	 * Returns the original frame source.
	 *
	 * @return Mat
	 */
	public Mat getSource() {
		return this.source;
	}
	
	/**
	 * Copies passed source into object.
	 */
	public void loadSource(Mat frame) {
		frame.copyTo(source);
	}
	
	/**
	 * Copies passed frame source into object.
	 */
	public void loadSource(Frame frame) {
		this.loadSource(frame.getSource());
	}
	
	/**
	 * Links passed source into object.
	 */
	public void linkSource(Mat frame) {
		this.source = frame;
	}
	
	/**
	 * Links passed frame source into object.
	 */
	public void linkSource(Frame frame) {
		this.linkSource(frame.getSource());
	}
	
	/**
	 * Displays the frame source.
	 *
	 * @param width
	 * @param height
	 * @param x
	 * @param y
	 */
	public void show(int width, int height, int x, int y) {
		// Show the frame source.
        HighGui.imshow(this.name, this.getSource());
        
        // Resize frame to sizes.
		HighGui.resizeWindow(this.name, width, height);
		
		// Move frame to position.
		HighGui.moveWindow(this.name, x, y);
	}
	
	/**
	 * Adds blur to the current frame.
	 *
	 * @param size
	 */
	public void blur(int size) {
		Imgproc.medianBlur(this.getSource(), this.getSource(), size);
	}
	
	/**
	 * Converts frame into passed Imgproc type.
	 *
	 * @param type
	 */
	public void convertTo(Frame destination, int type) {
		// Convert the colors into type.
		Imgproc.cvtColor(this.getSource(), destination.getSource(), type);
	}

	/**
	 * Isolates a color range of the current frame.
	 *
	 * @param lower
	 * @param upper
	 */
	public void isolateRange(Frame destination, Scalar lower, Scalar upper) {
		// Copy contents within range into destination.
		Core.inRange(this.getSource(), lower, upper, destination.getSource());
	}
	
	/**
	 * Crops the source to the passed rectangle.
	 *
	 * @param rect
	 */
	public void cropToRectangle(RotatedRect rect) {
		// Get rectangle properties.
		Size size = rect.size;
		double angle = rect.angle;
		
		// Check if rectangle has rotation.
		if (angle < -45.) {
			// Reverse rotation.
			angle += 90.;
			
			// Swap rect width and height.
			double temp = size.width;
			size.width = size.height;
			size.height = temp;
		}
		
		// Find rectangle rotation values.
		Mat rotation = Imgproc.getRotationMatrix2D(rect.center, angle, 1.0);

		// Warp the frame to the rectangle angle.
		Imgproc.warpAffine(
			this.getSource(), this.getSource(),	// Sources
			rotation, this.getSource().size(),	// Rotation & Sizes
			Imgproc.INTER_CUBIC					// Method
		);

		// Crop the frame to the rectangle size.
		Imgproc.getRectSubPix(this.getSource(), size, rect.center, this.getSource());
	}
	
}
