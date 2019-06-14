package sphinx;

import org.opencv.core.Scalar;

public class Config {

	/**
	 * Settings for the camera.
	 */
	public static class Camera {
		
		// Cropping
		public static final int croppingTime = 2;
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
		public static final boolean exitFailed = true;

		// Server
		public static final String ip = "192.168.43.44";
		public static final int port = 59898;
		
		// Speeds
		public static final int turnSpeed = 100;
		public static final int turnSlowSpeed = 50;
		
		public static final int slowSpeed = 250;
		public static final int moveSpeed = 600;
		public static final int collectOuterSpeed = 500;
		public static final int collectInnerSpeed = 200;
		
		// Angle
		public static final int degreeOffset = 6;
		public static final int slowDegreeOffset = 2;
		
		// Distance
		public static final int distOffset = 1;
		
		// Thresholds
		public static final int slowThreshold = 40;
		
	}
	
	/**
	 * Settings for the graph.
	 */
	public static class Graph {
		
		// Toggle
		public static final boolean enable = true;
		
	}

	/**
	 * Settings for the color detection.
	 */
	public static class Colors {
		
		// Red
		public static final Scalar redLowLower = new Scalar(0, 105, 105);
		public static final Scalar redLowUpper = new Scalar(10, 255, 255);
		public static final Scalar redHighLower = new Scalar(160, 105, 105);
		public static final Scalar redHighUpper = new Scalar(180, 255, 255);
		
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
		
		// Sizes - 3.23 px/cm
		public static final double carHeight = 79.135;
		public static final double cameraHeight = 562;
		
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
