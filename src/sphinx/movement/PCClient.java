package sphinx.movement;

import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

import org.opencv.core.Mat;

import sphinx.vision.Vehicle;

import java.util.ArrayList;

public class PCClient {
	
	Socket socket;

	public PCClient() {
		try {
			this.socket = new Socket("192.168.43.44", 59898);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public double calcDeg(double x, double y, double xBack, double yBack) { //calculates deg vector from origo
		try {
			//return Math.toDegrees(Math.tanh((y-yBack)/(x-xBack)));
			return Math.toDegrees(Math.atan2(y-yBack, x-xBack));
		} catch (Exception e) {
        	return 0;
        }
	}
	
	public double calcDist(double x, double y) { //calculate the distance between car and point
		return (double) Math.sqrt(((x*x)+(y*y)));
	}
	
	public void movement(double degDiff, double distance, PrintWriter out, ArrayList<Double[]> coordinates) { //moves the car
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
	
    public void run(Vehicle vehicle, Mat balls) throws Exception { //@wip
        double carDeg;
        double coordinateDeg;
        double degDiff;
        double x;
        double y;
        double distance;
    	 
        ArrayList<Double[]> coordinates = new ArrayList<>();
        coordinates.add(new Double[] {
    		248.5, 171.36
		});
        //coordinates.add(new Integer[] {4,5});
        //coordinates.add(new Integer[] {9,9});
        
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
        
        //try (Socket socket = new Socket(ip, 59898)) { //socket connection to server
        	//System.out.println("Enter lines of text then Ctrl+D or Ctrl+C to quit");
            //Scanner scanner = new Scanner(System.in);
            Scanner in = new Scanner(this.socket.getInputStream());
            PrintWriter out = new PrintWriter(this.socket.getOutputStream(), true);
            //while (scanner.hasNextLine()) {
                
            	
            	carCoordinates coord = new carCoordinates(vehicle.front.x, vehicle.front.y, vehicle.back.x, vehicle.back.y);
                //String[] cord = scanner.nextLine().split(":"); //splits command
                //carCoordinates coord = new carCoordinates(Integer.parseInt(cord[0]),Integer.parseInt(cord[1]));
                if(!coordinates.isEmpty()) { //Calculate distance if coordinates are not empty
	                carDeg = calcDeg(coord.x, coord.y, coord.xBack, coord.yBack);//+calcDeg(coord.xBack,coord.yBack);
	                coordinateDeg = calcDeg(coordinates.get(0)[0], coordinates.get(0)[1], coord.xBack, coord.yBack);
	                degDiff = coordinateDeg-carDeg;
	                
	                
	                x = coordinates.get(0)[0]-coord.x;
	                y = coordinates.get(0)[1]-coord.y;
	                distance = calcDist(x,y);
	                
	                movement(degDiff, distance, out, coordinates);
                }
            //}
        //}
    }
}