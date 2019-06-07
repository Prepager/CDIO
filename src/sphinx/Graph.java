package sphinx;

import org.opencv.core.Mat;
import org.opencv.core.Point;
import java.util.*; 


public class Graph {

    public List<Point> path = new ArrayList<Point>();
    public List<Point> balls = new ArrayList<Point>();
    public Point[] obstacles = new Point[4];
    public Point robot;
    
    public void run(Point[] obstacle, Mat circles, Point robot, int width, int height) {
    	for(int i=0; i < circles.cols(); i++) {
    		double[] c= circles.get(0,i);
    		balls.add(new Point(c[0],c[1]));
    	}
    	for(int i=0; i < 4; i++){
    		obstacles[i] = obstacle[i];
    	}
    	this.robot=robot;
    }
   

    private double calcDistance(Point node1, Point node2) {
        double tempValue = Math.sqrt(Math.pow(node1.x - node2.x, 2) + Math.pow(node1.y - node2.y, 2)); // Distance formlen
        return tempValue;
    }

    public void findClosest() {
    	path.clear();
        Point closestNode = new Point();
        double length = 1000;

        for (int i = 0; i < 10; i++) {
        	double distance = calcDistance(robot, balls.get(i));
            if (distance < length) {
                closestNode = balls.get(i);
                length = distance;
            }
        }
        crosses(robot, closestNode);
        path.add(closestNode);
        return;
    }


    public int crosses(Point node1, Point node2) {
        slope pathToBall = new slope();
        findSlope(node1, node2, pathToBall); // find function for path between two balls
        slope obstacle1 = new slope(); // This should be in a constructor
        findSlope(obstacles[0], obstacles[1], obstacle1); // find function for first obstacle section
        slope obstacle2 = new slope();
        findSlope(obstacles[2], obstacles[3], obstacle2); // find function for second obstacle section
        
        Point intersect1 = new Point();

        intersect1.x=(obstacle1.b - pathToBall.b) / (pathToBall.a - obstacle1.a);// point of intersection X-axis (d-c)/(a-b)
        intersect1.y=(pathToBall.a * obstacle1.b - obstacle1.a * pathToBall.b) / (pathToBall.a - obstacle1.a); // point of intersection Y-axis (ad-bc)/(a-b)
        		
        if ((obstacles[0].x <= intersect1.x && intersect1.x <= obstacles[1].x)                 //check if the line sections cross
                || (obstacles[0].x >= intersect1.x && intersect1.x >= obstacles[1].x )) {
            if ((obstacles[0].y  <= intersect1.y && intersect1.y <= obstacles[1].y )
                    || (obstacles[0].y  >= intersect1.y && intersect1.y >= obstacles[1].y )) {
                if ((node1.x <= intersect1.x && intersect1.x <= node2.x)
                        || (node1.x >= intersect1.x && intersect1.x >= node2.x)) {
                    if ((node1.y <= intersect1.y && intersect1.y <= node2.y)
                            || (node1.y >= intersect1.y && intersect1.y >= node2.y)) {
                    	Point point= new Point();
                        if(calcDistance(intersect1, obstacles[0]) < calcDistance(intersect1, obstacles[1])) {  	//Check which end it is closer to. Set point on outside of that end
                        	point.x=obstacles[0].x+((obstacles[0].x-obstacles[1].x)/2);
                        	point.y=obstacles[0].y+((obstacles[0].y-obstacles[1].y)/2);
                        }
                        else {
                        	point.x=obstacles[1].x+((obstacles[1].x-obstacles[0].x)/2);
                        	point.y=obstacles[1].y+((obstacles[1].y-obstacles[0].y)/2);
                        }
                        path.add(point);
                    }
                }
            }
        }
        
        Point intersect2 = new Point();
        
        intersect2.x = (obstacle2.b - pathToBall.b) / (pathToBall.a - obstacle2.a); 
        intersect2.y = (pathToBall.a * obstacle2.b - obstacle2.a * pathToBall.b) / (pathToBall.a - obstacle2.a);
        
        if ((obstacles[2].x <= intersect2.x && intersect2.x <= obstacles[3].x)                 //check if the line sections cross
                || (obstacles[2].x >= intersect2.x && intersect2.x >= obstacles[3].x )) {
            if ((obstacles[2].y  <= intersect2.y && intersect2.y <= obstacles[3].y )
                    || (obstacles[2].y  >= intersect2.y && intersect2.y >= obstacles[3].y )) {
                if ((node1.x <=intersect2.x && intersect2.x <= node2.x)
                        || (node1.x >= intersect2.x && intersect2.x >= node2.x)) {
                    if ((node1.y <= intersect2.y && intersect2.y <= node2.y)
                            || (node1.y >= intersect2.y && intersect2.y >= node2.y)) {
                    	Point point= new Point();
                        if(calcDistance(intersect2, obstacles[2]) < calcDistance(intersect2, obstacles[3])) {  	//Check which end it is closer to. Set point on outside of that end
                        	point.x=obstacles[2].x+((obstacles[2].x-obstacles[3].x)/2);
                        	point.y=obstacles[2].y+((obstacles[2].y-obstacles[3].y)/2);
                        }
                        else {
                        	point.x=obstacles[3].x+((obstacles[3].x-obstacles[2].x)/2);
                        	point.y=obstacles[3].y+((obstacles[3].y-obstacles[2].y)/2);
                        }
                        path.add(point);
                    }
                }
            }
         }
        
        return 0;
    }

    public void findSlope(Point node1, Point node2, slope slope) { // Receives two points and a slope object, and writes the
                                                               // function for the line in the slope object
        slope.a = (node1.y - node2.y) / (node1.x - node2.x); // (y1-y2)/(x1-x2)

        slope.b = -slope.a * node1.x + node1.y; // y=ax+b
    }

    public class slope {
        double a;
        double b;
    }

}
