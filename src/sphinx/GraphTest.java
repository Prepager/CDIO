package sphinx;

import org.opencv.core.Point; 


public class GraphTest extends Graph {

    public static void main(String args[]) {   
    	
    	GraphTest graph = new GraphTest();
    	
    	Point robot = new Point();
    	robot.x = 34;
    	robot.y = 103;
    	
        /*
         * double[][] coordins = { 
        		{ 1, 2.2, 3, 5.1, 5, 6, 7, 8, 1.3, 13, 1.05, 4.1, 4.3, 4.3, 3.8, 4.8 },
                { 1, 7, 5, 1.2, 4, 5.3, 6, 8, 6.8, 1.1, 1.05, 2.2, 4.8, 3.8, 4.3, 4.3 }, };
        */

        graph.findClosest();

        for (int i = 0; i < graph.path.size(); i++) {
        	System.out.print("\n" + graph.path.get(i));
        }
    }
}
