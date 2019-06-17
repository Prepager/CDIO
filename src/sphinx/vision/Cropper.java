package sphinx.vision;

import java.util.ArrayList;
import java.util.List;

import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.RotatedRect;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import sphinx.Config;

public class Cropper {
	
	/**
	 * The found cropped rect.
	 *
	 * @var RotatedRect
	 */
	public RotatedRect rect;
	
	/**
	 * @wip
	 */
	private long start;
	
	/**
	 * @wip
	 */
	private double x = 0;
	
	/**
	 * @wip
	 */
	private double y = 0;
	
	/**
	 * @wip
	 */
	private double angle = 0;
	
	/**
	 * @wip
	 */
	private double width = 0;
	
	/**
	 * @wip
	 */
	private double height = 0;
	
	/**
	 * @wip
	 */
	private Camera camera;
	
	/**
	 * @wip
	 */
	private ArrayList<RotatedRect> rects = new ArrayList<RotatedRect>();
	
	/**
	 * @wip
	 */
	public Cropper(Camera camera) {
		// Set passed camera instance.
		this.camera = camera;
		
		// Find the crop rectangle.
		this.findCrop();
	}
	
	/**
	 * Find rotated rect for optimal crop.
	 */
	public void findCrop() {
		// Prepare new frame for cropper.
		Frame frame = new Frame("HSV Frame");
		
		// Get the program starting time.
		this.start = System.currentTimeMillis();
		
		// Show loop starting warning.
		System.out.println("Starting cropper loop");
		
		// Start infinite loop.
		while (true) {
			// Break if has been running for full cropping time.
			long time = System.currentTimeMillis();
			if ((time - this.start) >= (Config.Camera.croppingTime * 1000)) {
				break;
			}
			
			// Capture frame from camera.
			this.camera.capture(frame);
			
			// Convert frame to HSV color space.
			frame.convertTo(frame, Imgproc.COLOR_BGR2HSV);
			
			// Isolate the red colors.
			frame.isolateRange(frame,
				Config.Colors.redLowLower,
				Config.Colors.redLowUpper,
				Config.Colors.redHighLower,
				Config.Colors.redHighUpper
			);
			
			// Find contours and continue if missing.
			List<MatOfPoint> contours = frame.sortedContours();
			
			// Get first contour and convert to rect.
			int index = Math.min(Config.Obstacle.areaIndex, contours.size() - 1);
			RotatedRect rect = frame.contourToRect(contours.get(index));

			// Add rect to list.
			this.rects.add(rect);
		}
		
		// Loop through the rects and do mass averages.
		int t = 1;
		for (RotatedRect rect : this.rects) {
			// Find average for center x and y positions.
			this.x += (rect.center.x - this.x) / t;
			this.y += (rect.center.y - this.y) / t;
			
			// Find average for the rect angle.
			this.angle += (rect.angle - this.angle) / t;
			
			// Find average for the rect width and height.
			this.width += (rect.size.width - this.width) / t;
			this.height += (rect.size.height - this.height) / t;
			
			// Increment counter for average.
			++t;
		}
		
		//
		if (this.angle <= -45) {
			//
			this.angle += 90;
			
			//
			double temp = this.width;
			this.width = this.height;
			this.height = temp;
			
			//
			temp = this.x;
			this.x = this.y;
			this.y = temp;
		}
		
		// Create new rect for found variables.
		this.rect = new RotatedRect(
			new Point(this.x, this.y),
			new Size(this.width, this.height),
			this.angle
		);
	}
	
	/**
	 * Crop a frame to the found rotated rect.
	 *
	 * @param frame
	 */
	public void cropFrame(Frame frame) {
		// Skip if missing rect.
		if (this.rect == null) return;
		
		// Find bounding box for rotated rect.
		Rect bounding = rect.boundingRect();
		
		// Restrict frame area to bounding box.
		frame.linkSource(frame.getSource().submat(bounding));
		
		// Get the rotation matrix for the rectangle.
		Mat rotation = Imgproc.getRotationMatrix2D(rect.center, rect.angle, 1.0);
		
		// Rotate frame to rectnagle rotation.
		Imgproc.warpAffine(frame.getSource(), frame.getSource(), rotation, rect.size);
	}
	
	/**
	 * Returns whether or not it can crop.
	 *
	 * @return boolean
	 */
	public boolean canCrop() {
		return this.rect != null;
	}
	
}
