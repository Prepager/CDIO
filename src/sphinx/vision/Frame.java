package sphinx.vision;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Scalar;
import org.opencv.highgui.HighGui;
import org.opencv.imgproc.Imgproc;

import sphinx.vision.partials.HasContours;

public class Frame extends HasContours {
	
	/**
	 * The name of the frame.
	 *
	 * @var String
	 */
	protected String name = "Untitled Frame";

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
	 * Isolates a two color ranges of the current frame.
	 *
	 * @param lowLower
	 * @param lowUpper
	 * @param highLower
	 * @param highUpper
	 */
	public void isolateRange(Frame destination, Scalar lowLower, Scalar lowUpper, Scalar highLower, Scalar highUpper) {
		// Isolate the lower range into destination.
		this.isolateRange(destination, lowLower, lowUpper);
		
		// Create additional frame and isolate upper.
		Frame additional = new Frame("Additional");
		this.isolateRange(additional, highLower, highUpper);
		
		// Bitwise or the two frames to get the combined result.
		Core.bitwise_or(destination.getSource(), additional.getSource(), destination.getSource());
	}
	
}
