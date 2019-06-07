package sphinx.vision;

import org.opencv.videoio.VideoCapture;
import org.opencv.videoio.Videoio;

public class Camera {
	
	protected int width = 640;
	
	protected int height = 480;
	
	protected String source;
	
	protected Boolean useWebcam;

	protected VideoCapture capture;
	
	/**
	 * Sets parameters for camera and initialize.
	 *
	 * @param webcam
	 * @param source
	 */
	public Camera(Boolean webcam, String source) {
		// Set parameters.
		this.source = source;
		this.useWebcam = webcam;
		
		// Initialize camera.
		this.initialize();
	}
	
	/**
	 * Initialize the camera.
	 */
	public void initialize() {
		// Create new video capture object.
		this.capture = useWebcam
			? new VideoCapture(0)
			: new VideoCapture(source);
		
		// Set capture width, height and disable focus.
		this.capture.set(Videoio.CAP_PROP_AUTOFOCUS, 0);
		this.capture.set(Videoio.CAP_PROP_FRAME_WIDTH, this.width);
		this.capture.set(Videoio.CAP_PROP_FRAME_HEIGHT, this.height);
	}
	
	/**
	 * Captures a frame form the camera.
	 *
	 * @param destination
	 */
	public void capture(Frame destination) {
		// Capture frame from camera and save result.
		Boolean result = this.capture.read(destination.getSource());
		
		// Reset frame counter and recapture if video.
		if (! result && ! this.useWebcam) {
			this.capture.set(Videoio.CAP_PROP_POS_FRAMES, 0);
			this.capture.read(destination.getSource());
		}
	}

}
