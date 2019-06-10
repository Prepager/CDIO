package sphinx.movement;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;


import java.util.ArrayList;
import java.util.List;
//import java.text.DecimalFormat;

public class PCClient {

	
    public static void main(String[] args) throws Exception {
    	String ip = "192.168.43.44";
    	 
        ArrayList<Integer[]> coordinates = new ArrayList<>();
        coordinates.add(new Integer[] {4,5});
         
        class carCoordinates {
            public int x;
            public int y;
            public carCoordinates(int x, int y){
                this.x = x;
                this.y = y;
            }
        }

        try (Socket socket = new Socket(ip, 59898)) {
        	System.out.println("Enter lines of text then Ctrl+D or Ctrl+C to quit");
            Scanner scanner = new Scanner(System.in);
            Scanner in = new Scanner(socket.getInputStream());
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            while (scanner.hasNextLine()) {
                
                String[] cord = scanner.nextLine().split(":");
                carCoordinates coord = new carCoordinates(Integer.parseInt(cord[0]),Integer.parseInt(cord[1]));

                
                double carDeg = 0;
                double coordinateDeg = 0;
                try{
                	carDeg = Math.toDegrees(Math.tanh(coord.y/coord.x));
                }catch (Exception e) {
                	carDeg = 0;
                }
                try {
                	coordinateDeg = Math.toDegrees(Math.tanh(coordinates.get(0)[1]/coordinates.get(0)[0]));	
                }catch (Exception e) {
                	coordinateDeg = 0;
                }
                
                double degDiff = coordinateDeg-carDeg;
                
                
                double x = coordinates.get(0)[0]-coord.x;
                double y = coordinates.get(0)[1]-coord.y;
                double distance = (double) Math.sqrt((x*x)+(y*y));
                
                if(degDiff != 0) {
                	out.println("turn " + Math.round(degDiff*100)/100);
                }
                
                if(distance>0) {
                	out.println("move 300");
                }else if(distance <0) {
                	out.println("move -300");
                } else {
                	out.println("move 0");
                }
                System.out.println("distance: " + distance + " deg: " + Math.round(degDiff*100)/100);
            }
        }
    }
}
