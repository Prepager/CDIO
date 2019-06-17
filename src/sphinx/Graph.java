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
    public final double safeDistance = 40;
    public final double wallDistance = 60;
    public double crossDistance;
    public final double offset = 8; //Change this to something real.
    
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
    	
    	towardsGoal = false;
    	reverse = false;
    	path.clear();
        Point closestNode = new Point();
        double length = 100000;

        for (int i = 0; i < balls.size(); i++) {
        	double distance = calcDistance(robot, balls.get(i));
            if (distance < length) {
                closestNode = balls.get(i);
                length = distance;
            }
        }
        crosses(robot, closestNode);
        wall(closestNode);
        //obstacle(closestNode);
        path.add(closestNode);
    }
    
    public void findGoal(int side) {
    	towardsGoal = true;
    	reverse = true;
    	path.clear();
    	Point pos = new Point();
    	Point goal = new Point();
    	if(side==1) {
    		pos.x = width - 80;
    		goal.x=width - 40;
    	}
    	else {
    		pos.x = 80;
    		goal.x=40;
    	}
    	pos.y=height/2;
    	goal.y=height/2;
    	crosses(robot, goal);
        path.add(pos);
    	path.add(goal);
    }


    public void wall(Point node) {
    	Point point = new Point();
		point.x=node.x;
		point.y=node.y;
		int change = 0;
    	if (node.x < safeDistance) {  //Close to left wall
    		point.x=node.x+wallDistance;
    		change = 1;
    	}
    	if (node.x > width-safeDistance) {  //Close to right wall
			point.x=node.x-wallDistance;
			change = 1;
    	}
    	if (node.y < safeDistance) {  //Close to upper wall
    		if (change == 1) {
    			point.y=node.y+offset;
    			node.y=node.y+offset;
    		}
    		else {
    			point.y=node.y+wallDistance;
    			change = 1;
    		}
    	}
    	if (node.y > height-safeDistance) {  //Close to lower wall
    		if (change == 1) {
    			point.y=node.y-offset;
    			node.y=node.y-offset;
    		}
    		else {
    			point.y=node.y-wallDistance;
    			change = 1;
    		}
    	}
    	
    	if (change==1) {
    		reverse = true;
    		path.add(point);
    	}
    }
    
   
    public void obstacle(Point node) {
    	Point point = new Point();
    	double[] distances = new double[4];
    	for(int i = 0; i<4; i++) {
    		distances[0] = calcDistance(node, obstacles[i]);
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
    		double x;
    		double y;
    		slope slope = new slope();
    		if(shortNum%2==0) {														//find direction of obstacle
    			findSlope(obstacles[shortNum], obstacles[shortNum-1], slope);
    			x = obstacles[shortNum].x-obstacles[shortNum-1].x;
    			y = obstacles[shortNum].y-obstacles[shortNum-1].y;
    		}
    		else {
    			findSlope(obstacles[shortNum], obstacles[shortNum+1], slope);
    			x = obstacles[shortNum].x-obstacles[shortNum+1].x;
    			y = obstacles[shortNum].y-obstacles[shortNum+1].y;
    		}
    		
    		if (x>0) {				//Check direction. Offset the ball parallel to obstacle and point perpendicular
    			node.x = node.x + (offset)*(1/slope.a+1);  			
    			point.x = node.x - (safeDistance)*(slope.a/slope.a+1);
    		}
    		else {
    			node.x = node.x - (offset)*(1/slope.a+1); 			
    			point.x = node.x + (safeDistance)*(slope.a/slope.a+1);
    		}
    		if (y>0) {
    			node.y = node.y + (offset)*(slope.a/slope.a+1);
    			point.y = node.y + (safeDistance)*(1/slope.a+1);
    		}
    		else {
    			node.y = node.y - (offset)*(slope.a/slope.a+1);
    			point.y = node.y - (safeDistance)*(1/slope.a+1);
    		}
    		
    		path.add(point);
    	}		
    }
    
    public void crosses(Point node1, Point node2) {
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
        
        double[] distances = new double[4];
        for(int i = 0; i<4; i++) {
    		distances[0] = calcDistance(node1, obstacles[i]);
    	}     
        double shortest = distances[0];
		int shortNum = 0;
		for(int i = 1; i<4; i++) {       //Find cross corner nearest the ball
			if (distances[i]<shortest) {
				shortest = distances[i];
				shortNum=i;
			}
		}
        
        if (shortNum == 0 || shortNum == 1) {
	        if(intersect(obstacles[0],obstacles[1], node1, node2)==0) {
	        	if(intersect(obstacles[0],obstacles[1], botPoint1, ballPoint1)==0) {
	        		intersect(obstacles[0],obstacles[1], botPoint2, ballPoint2);
	        	}
	        }
	        if(intersect(obstacles[2],obstacles[3], node1, node2)==0) {
	        	if(intersect(obstacles[2],obstacles[3], botPoint1, ballPoint1)==0) {
	        		intersect(obstacles[2],obstacles[3], botPoint2, ballPoint2);
	        	}
	        }
        }
        else {
	        if(intersect(obstacles[2],obstacles[3], node1, node2)==0) {
	        	if(intersect(obstacles[2],obstacles[3], botPoint1, ballPoint1)==0) {
	        		intersect(obstacles[2],obstacles[3], botPoint2, ballPoint2);
	        	}
	        }
        	if(intersect(obstacles[0],obstacles[1], node1, node2)==0) {
	        	if(intersect(obstacles[0],obstacles[1], botPoint1, ballPoint1)==0) {
	        		intersect(obstacles[0],obstacles[1], botPoint2, ballPoint2);
	        	}
	        }
        }
        
        return;
    }
    
    public int intersect(Point pointa, Point pointb, Point origin, Point target) {
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
                        	point1.x=pointa.x+((pointa.x-pointb.x))+offset*(pointsSlope.a/pointsSlope.a+1);          //make two points on a line perpendicular to the obstacle 
                        	point1.y=pointa.y+((pointa.y-pointb.y))-offset*(1/pointsSlope.a+1);
                        	point2.x=pointa.x+((pointa.x-pointb.x))-offset*(pointsSlope.a/pointsSlope.a+1);
                        	point2.y=pointa.y+((pointa.y-pointb.y))+offset*(1/pointsSlope.a+1);
                        }
                        else {
                        	point1.x=pointb.x+((pointb.x-pointa.x))+offset*(pointsSlope.a/pointsSlope.a+1);
                        	point1.y=pointb.y+((pointb.y-pointa.y))-offset*(1/pointsSlope.a+1);
                        	point2.x=pointb.x+((pointb.x-pointa.x))-offset*(pointsSlope.a/pointsSlope.a+1);
                        	point2.y=pointb.y+((pointb.y-pointa.y))+offset*(1/pointsSlope.a+1);
                        }
                        if(calcDistance(origin, point1)<calcDistance(origin, point2)) {   // drive to the nearest first
                        	path.add(point1);
                        	path.add(point2);
                        }
                        else {
                        	path.add(point2);
                        	path.add(point1);
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
