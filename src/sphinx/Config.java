package sphinx;

import org.opencv.core.Scalar;

public class Config {

	/**
	 * Settings for the camera.
	 */
	public static class Camera {
		
		// Toggle
		public static final boolean shouldCrop = false;
		
		// Video
		public static final int width = 640;
		public static final int height = 480;
		
		public static final boolean useWebcam = true;
		public static final String source = "./src/video3.mov";
		
	}

	/**
	 * Settings for the movement client.
	 */
	public static class Client {
		
		// Toggle
		public static final boolean connect = true;

		// Server
		public static final String ip = "192.168.43.44";
		public static final int port = 59898;
		
		// Speeds
		public static final int turnSpeed = 100;
		public static final int slowSpeed = 100;
		public static final int moveSpeed = 200;
		public static final int collectSpeed = 100;
		
		// Offsets
		public static final int distOffset = 6;
		public static final int degreeOffset = 4;
		
		// Thresholds
		public static final int slowThreshold = 30;
		
	}

	/**
	 * Settings for the color detection.
	 */
	public static class Colors {
		
		// Red
		public static final Scalar redLower = new Scalar(0, 80, 80);
		public static final Scalar redUpper = new Scalar(10, 255, 255);
		
		// Blue
		public static final Scalar blueLower = new Scalar(85, 130, 130);
		public static final Scalar blueUpper = new Scalar(140, 255, 255);
		
		// White
		public static final Scalar whiteLower = new Scalar(0, 0, 220);
		public static final Scalar whiteUpper = new Scalar(255, 35, 255);
		
	}

	/**
	 * Settings for the position transformer.
	 */
	public static class Position {
		
		// Sizes
		public static final double carHeight = 74.29; 		// 23 cm * 3,23 px/cm = 74,29
		public static final double cameraHeight = 529.72; 	// 164 cm * 3,23 px/cm = 529,72
		
	}

	/**
	 * Settings for the GUI preview.
	 */
	public static class Preview {
		
		// Sizes
		public static final int displayWidth = 1280;
		public static final int displayHeight = 720;
		
	}
	
}
