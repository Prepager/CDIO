package sphinx;

import org.opencv.core.Mat;
import org.opencv.core.Point;
import java.util.*; 


public class Graph {

    public ArrayList<Point> path = new ArrayList<Point>();
    public List<Point> balls = new ArrayList<Point>();
    public Point[] obstacles = new Point[4];
    public Point robot;
    public int width, height;
    public boolean reverse;
    public boolean towardsGoal = false;
    public final double safeDistance = 60;
    public final double wallDistance = 60;
    public double crossDistance;
    public final double offset = 12; //Change this to something real.
    
    public void run(Point[] obstacles, Mat circles, Point robot, int width, int height) {
    	balls.clear();
    	for(int i=0; i < circles.cols(); i++) {
    		double[] c= circles.get(0,i);
    		balls.add(new Point(c[0],c[1]));
    	}
    	
    	double longestDist = 0;
    	int longestNum = 0;
    	for(int i = 0; i<3; i++) {									//find out which obstacle point is opposite
    		double dist = calcDistance(obstacles[0], obstacles[i]);
    		if(dist>longestDist) {
    			longestDist=dist;
    			longestNum=i;
    		}
    	}
    	Point temp = obstacles[1];
    	obstacles[1] = obstacles[longestNum];
    	obstacles[longestNum] = temp;
    	for(int i = 0; i<4; i++) {		
    		this.obstacles[i]=obstacles[i];
    	}
    	crossDistance = calcDistance(obstacles[0],obstacles[1])*1.2;
    	this.robot=robot;
    	this.width=width;
    	this.height=height;
    }
   

    private double calcDistance(Point node1, Point node2) {
        double tempValue = Math.sqrt(Math.pow(node1.x - node2.x, 2) + Math.pow(node1.y - node2.y, 2)); // Distance formlen
        return tempValue;
    }

    public void findClosest() {
    	if (balls.isEmpty()) return;
    	
    	ArrayList<ArrayList<Point>> paths = new ArrayList<ArrayList<Point>>();
    	ArrayList<Double> lengths = new ArrayList<Double>();
    	
    	towardsGoal = false;
    	reverse = false;
    	path.clear();
        Point node = new Point();

        for (int i = 0; i < balls.size(); i++) {
        		ArrayList<Point> tempPath = new ArrayList<Point>();
                node = balls.get(i);
                paths.add(tempPath);
                crosses(robot, node, paths.get(i));
                wall(node, paths.get(i));
                obstacle(node, paths.get(i));
                paths.get(i).add(node);
        }
        double shortestLength = Double.MAX_VALUE;
        int shortestPath = 0;
        
        for(int i = 0; i<paths.size(); i++) {
        	double length = 0;
        	length += calcDistance(robot, paths.get(i).get(0));
        	for (int j = 1; j<paths.get(i).size(); j++) {
        		length += calcDistance(paths.get(i).get(j-1), paths.get(i).get(j));
        	}
        	lengths.add(length);
        	if(lengths.get(i)<shortestLength) {
        		shortestLength = lengths.get(i);
        		shortestPath = i;
        	}
        }
        for (int i = 0; i<paths.get(shortestPath).size(); i++){
        	path.add(paths.get(shortestPath).get(i));
        }
        
        
        
        
    }
    
    public void findGoal(int side) {
    	towardsGoal = true;
    	reverse = true;
    	path.clear();
    	Point pos = new Point();
    	Point goal = new Point();
    	if(side==1) {
    		pos.x = width - 80;
    		goal.x = width - 25;
    	}
    	else {
    		pos.x = 80;
    		goal.x = 25;
    	}
    	pos.y=height/2;
    	goal.y=height/2;
    	crosses(robot, goal, path);
        path.add(pos);
    	path.add(goal);
    }


    public void wall(Point node, ArrayList<Point> tempPath) {
    	Point point1 = new Point();
    	Point point2 = new Point();
		point1.x=node.x;
		point1.y=node.y;
		point2.x=node.x;
		point2.y=node.y;
		int change = 0;
    	if (node.x < safeDistance) {  //Close to left wall
    		point1.x=node.x+wallDistance;
    		point2.x=node.x+(wallDistance/2);
    		change = 1;
    	}
    	if (node.x > width-safeDistance) {  //Close to right wall
			point1.x=node.x-wallDistance;
			point2.x=node.x-(wallDistance/2);
			change = 1;
    	}
    	if (node.y < safeDistance) {  //Close to upper wall
    		if (change == 1) {
    			point1.y=node.y+offset;
    			point2.y=node.y+offset;
    			node.y=node.y+offset;
    		}
    		else {
    			point1.y=node.y+wallDistance;
    			point2.y=node.y+(wallDistance/2);
    			change = 1;
    		}
    	}
    	if (node.y > height-safeDistance) {  //Close to lower wall
    		if (change == 1) {
    			point1.y=node.y-offset;
    			point2.y=node.y-offset;
    			node.y=node.y-offset;
    		}
    		else {
    			point1.y=node.y-wallDistance;
    			point1.y=node.y-(wallDistance/2);
    			change = 1;
    		}
    	}
    	
    	if (change==1) {
    		reverse = true;
    		tempPath.add(point1);
    		tempPath.add(point2);
    	}
    }
    
   
    public void obstacle(Point node, ArrayList<Point> tempPath) {
    	Point point = new Point();
    	double[] distances = new double[4];
    	for(int i = 0; i<4; i++) {
    		distances[i] = calcDistance(node, obstacles[i]);
    	}
    	
    	if (distances[0]<crossDistance && distances[1]<crossDistance && distances[2]<crossDistance && distances[3]<crossDistance){
    		reverse = true;
    		double shortest = distances[0];
    		int shortNum = 0;
    		for(int i = 1; i<4; i++) {       //Find cross corner nearest the ball
    			if (distances[i]<shortest) {
    				shortest = distances[i];
    				shortNum=i;
    			}
    		}
    		
    		slope slope = new slope();
    		
    		int pair;
    		if(shortNum%2!=0) {
    			pair = -1;
    		}
    		else {
    			pair = 1;
    		}
    		findSlope(obstacles[shortNum], obstacles[shortNum+pair], slope);
    		
    		if (obstacles[shortNum].x < obstacles[shortNum+pair].x) {
    			node.x = node.x - (offset)*(1/(Math.abs(slope.a)+1));
    		}
    		else {
    			node.x = node.x + (offset)*(1/(Math.abs(slope.a)+1));
    		}
    		if (obstacles[shortNum].y < obstacles[shortNum+pair].y) {
    			node.y = node.y - (offset)*(Math.abs(slope.a)/(Math.abs(slope.a)+1));
    		}
    		else {
    			node.y = node.y + (offset)*(Math.abs(slope.a)/(Math.abs(slope.a)+1));
    		}
    		
    		int secondShortest = 0;
    		double length = Double.MAX_VALUE;
    		for(int i = 0; i<4; i++) {       //Find cross corner nearest the ball
    			if (i != shortNum) {
	    			if (calcDistance(node,obstacles[i])<length) {
	    				length = calcDistance(node,obstacles[i]);
	    				secondShortest=i;
	    			}
    			}
    		}
    		slope pointSlope = new slope();
    		int otherPair;
    		if(secondShortest%2!=0) {
    			otherPair = -1;
    		}
    		else {
    			otherPair = 1;
    		}
    		
    		Point point2 = new Point();
    		findSlope(obstacles[secondShortest], obstacles[secondShortest+otherPair], pointSlope);
    		
    		if (obstacles[secondShortest].x < obstacles[secondShortest+otherPair].x) {
    			point.x = node.x - (safeDistance)*(1/(Math.abs(pointSlope.a)+1));
    			point2.x = node.x - (safeDistance*2)*(1/(Math.abs(pointSlope.a)+1));
    		}
    		else {
    			point.x = node.x + (safeDistance)*(1/(Math.abs(pointSlope.a)+1));
    			point2.x = node.x + (safeDistance*2)*(1/(Math.abs(pointSlope.a)+1));
    		}
    		if (obstacles[secondShortest].y < obstacles[secondShortest+otherPair].y) {
    			point.y = node.y - (safeDistance)*(Math.abs(pointSlope.a)/(Math.abs(pointSlope.a)+1));
    			point2.y = node.y - (safeDistance*2)*(Math.abs(pointSlope.a)/(Math.abs(pointSlope.a)+1));
    		}
    		else {
    			point.y = node.y + (safeDistance)*(Math.abs(pointSlope.a)/(Math.abs(pointSlope.a)+1));
    			point2.y = node.y + (safeDistance*2)*(Math.abs(pointSlope.a)/(Math.abs(pointSlope.a)+1));
    		}

    		tempPath.add(point);
    		tempPath.add(point2);
    	}		
    }
    
    public void crosses(Point node1, Point node2, ArrayList<Point> tempPath) {
        slope pathToBall = new slope();
        findSlope(node1, node2, pathToBall); // find function for path between two balls
        
        Point botPoint1 = new Point();
        Point botPoint2 = new Point();
        Point ballPoint1 = new Point();
        Point ballPoint2 = new Point();
        botPoint1.x = node1.x + (safeDistance)*(pathToBall.a/pathToBall.a+1);  			//Places points perpendicular to path, giving the robot width
        botPoint1.y = node1.y - (safeDistance)*(1/pathToBall.a+1);
        botPoint2.x = node1.x - (safeDistance)*(pathToBall.a/pathToBall.a+1);  			
        botPoint2.y = node1.y + (safeDistance)*(1/pathToBall.a+1);
        ballPoint1.x = node1.x + (safeDistance)*(pathToBall.a/pathToBall.a+1);  			
        ballPoint1.y = node1.y - (safeDistance)*(1/pathToBall.a+1);
        ballPoint2.x = node1.x - (safeDistance)*(pathToBall.a/pathToBall.a+1);  			
        ballPoint2.y = node1.y + (safeDistance)*(1/pathToBall.a+1);
        
        ArrayList<Point> tempPath1 = new ArrayList<Point>();			//These two hold path around cross before we find out which points to take first.
        ArrayList<Point> tempPath2 = new ArrayList<Point>();
        
	        if(intersect(obstacles[0],obstacles[1], node1, node2, tempPath1)==0) {
	        	if(intersect(obstacles[0],obstacles[1], botPoint1, ballPoint1, tempPath1)==0) {
	        		intersect(obstacles[0],obstacles[1], botPoint2, ballPoint2, tempPath1);
	        	}
	        }
	        if(intersect(obstacles[2],obstacles[3], node1, node2, tempPath2)==0) {
	        	if(intersect(obstacles[2],obstacles[3], botPoint1, ballPoint1, tempPath2)==0) {
	        		intersect(obstacles[2],obstacles[3], botPoint2, ballPoint2, tempPath2);
	        	}
	        }
	        
	        if(!tempPath1.isEmpty()  && !tempPath2.isEmpty()) {
		        if(calcDistance(node1, tempPath1.get(0))<calcDistance(node1, tempPath2.get(0))) {  //Find out which is closest.
		        	tempPath.add(tempPath1.get(0));
		        	tempPath.add(tempPath1.get(1));
		        	tempPath.add(tempPath2.get(0));
		        	tempPath.add(tempPath2.get(1));
		        }
		        else {
		        	tempPath.add(tempPath2.get(0));
		        	tempPath.add(tempPath2.get(1));
		        	tempPath.add(tempPath1.get(0));
		        	tempPath.add(tempPath1.get(1));
		        }
	        }
	        else if(!tempPath1.isEmpty()) {
	        	tempPath.add(tempPath1.get(0));
	        	tempPath.add(tempPath1.get(1));
	        }
	        else if(!tempPath2.isEmpty()) {
	        	tempPath.add(tempPath2.get(0));
	        	tempPath.add(tempPath2.get(1));
	        }

        
        return;
    }
    
    public int intersect(Point pointa, Point pointb, Point origin, Point target, ArrayList<Point> tempPath) {
    	slope pointsSlope = new slope();
        findSlope(pointa, pointb, pointsSlope);
        slope travelSlope = new slope();
        findSlope(origin, target, travelSlope);
        
        Point intersect = new Point();
        

        intersect.x=(pointsSlope.b - travelSlope.b) / (travelSlope.a - pointsSlope.a);// point of intersection X-axis (d-c)/(a-b)
        intersect.y=(travelSlope.a * pointsSlope.b - pointsSlope.a * travelSlope.b) / (travelSlope.a - pointsSlope.a); // point of intersection Y-axis (ad-bc)/(a-b)
        int change = 0;
        
    	if ((pointa.x <= intersect.x && intersect.x <= pointb.x)                 //check if the line sections cross
                || (pointa.x >= intersect.x && intersect.x >= pointb.x )) {
            if ((pointa.y  <= intersect.y && intersect.y <= pointb.y )
                    || (pointa.y  >= intersect.y && intersect.y >= pointb.y )) {
                if ((origin.x <=intersect.x && intersect.x <= target.x)
                        || (origin.x >= intersect.x && intersect.x >= target.x)) {
                    if ((origin.y <= intersect.y && intersect.y <= target.y)
                            || (origin.y >= intersect.y && intersect.y >= target.y)) {
                    	change = 1;
                    	Point point1= new Point();
                    	Point point2= new Point();
                        if(calcDistance(intersect, pointa) < calcDistance(intersect, pointb)) {  	//Check which end it is closer to. Set points on outside of that end
                        	point1.x=pointa.x+((pointa.x-pointb.x))+offset*(pointsSlope.a/(pointsSlope.a+1));          //make two points on a line perpendicular to the obstacle 
                        	point1.y=pointa.y+((pointa.y-pointb.y))-offset*(1/(pointsSlope.a+1));
                        	point2.x=pointa.x+((pointa.x-pointb.x))-offset*(pointsSlope.a/(pointsSlope.a+1));
                        	point2.y=pointa.y+((pointa.y-pointb.y))+offset*(1/(pointsSlope.a+1));
                        }
                        else {
                        	point1.x=pointb.x+((pointb.x-pointa.x))+offset*(pointsSlope.a/(pointsSlope.a+1));
                        	point1.y=pointb.y+((pointb.y-pointa.y))-offset*(1/(pointsSlope.a+1));
                        	point2.x=pointb.x+((pointb.x-pointa.x))-offset*(pointsSlope.a/(pointsSlope.a+1));
                        	point2.y=pointb.y+((pointb.y-pointa.y))+offset*(1/(pointsSlope.a+1));
                        }
                        if(calcDistance(origin, point1)<calcDistance(origin, point2)) {   // drive to the nearest first
                        	tempPath.add(point1);
                        	tempPath.add(point2);
                        }
                        else {
                        	tempPath.add(point2);
                        	tempPath.add(point1);
                        }
                    }
                }
            }
         }
    	return change;
    }

    public void findSlope(Point node1, Point node2, slope slope) { // Receives two points and a slope object, and writes the function for the line in the slope object
    	if(node1.x == node2.x) {			//prevent potential for divide by zero error    
        	slope.a = 1000000;
        }
        else { 
        	slope.a = (node1.y - node2.y) / (node1.x - node2.x); // (y1-y2)/(x1-x2)
        }

        slope.b = -slope.a * node1.x + node1.y; // y=ax+b
    }

    public class slope {
        double a;
        double b;
    }

}
