package sphinx.movement;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

import sphinx.vision.Vehicle;

import java.util.ArrayList;
import java.util.List;
//import java.text.DecimalFormat;

public class PCClient {

	public static double calcDeg(double x, double y) { //calculates deg vector from origo
		try {
			return Math.toDegrees(Math.tanh(y/x));
		} catch (Exception e) {
        	return 0.0;
        }
	}
	
	public static double calcDist(double x, double y) { //calculate the distance between car and point
		return (double) Math.sqrt((x*x)+(y*y));
	}
	
	public static void movement(double degDiff, double distance, PrintWriter out, ArrayList<Integer[]> coordinates) { //moves the car
		if(degDiff != 0) {
        	out.println("turn " + Math.round(degDiff*100)/100);
        }
        
        if(distance>0) {
        	out.println("move 300");
        }else if(distance <0) {
        	out.println("move -300");
        } else {
        	out.println("move 0");
        	coordinates.remove(0);
        }
        System.out.println("distance: " + distance + " deg: " + Math.round(degDiff*100)/100);
	}
	
    public static void run(Vehicle vehicle) throws Exception {
    	String ip = "192.168.43.44";
        double carDeg;
        double coordinateDeg;
        double degDiff;
        double x;
        double y;
        double distance;
    	 
        ArrayList<Integer[]> coordinates = new ArrayList<>();
        coordinates.add(new Integer[] {4,5});
        coordinates.add(new Integer[] {9,9});
        
        class carCoordinates { //struct for carCoorinates
            public double x;
            public double y;
            public double xBack;
            public double yBack;
            public carCoordinates(double x, double y, double xBack, double yBack){
                this.x = x;
                this.y = y;
            }
        }
        
        try (Socket socket = new Socket(ip, 59898)) { //socket connection to server
        	System.out.println("Enter lines of text then Ctrl+D or Ctrl+C to quit");
            Scanner scanner = new Scanner(System.in);
            Scanner in = new Scanner(socket.getInputStream());
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            while (scanner.hasNextLine()) {
                
            	
            	carCoordinates coord = new carCoordinates(vehicle.front.x, vehicle.front.y, vehicle.back.x, vehicle.back.y);
                //String[] cord = scanner.nextLine().split(":"); //splits command
                //carCoordinates coord = new carCoordinates(Integer.parseInt(cord[0]),Integer.parseInt(cord[1]));
                if(!coordinates.isEmpty()) { //Calculate distance if coordinates are not empty
	                carDeg = calcDeg(coord.x, coord.y)+calcDeg(coord.xBack,coord.yBack);
	                coordinateDeg = calcDeg(coordinates.get(0)[0], coordinates.get(0)[1]);
	                degDiff = coordinateDeg-carDeg;
	                
	                
	                x = coordinates.get(0)[0]-coord.x;
	                y = coordinates.get(0)[1]-coord.y;
	                distance = calcDist(x,y);
	                
	                movement(degDiff, distance, out, coordinates);
                }
            }
        }
    }
}