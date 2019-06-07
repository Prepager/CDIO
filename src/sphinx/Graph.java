package sphinx;

import org.opencv.core.Mat;
import org.opencv.core.Point;

public class Graph {

    public double[][] coordinates = new double[2][15]; // 0: Robot 1-10: Balls 12-15: obstacles
    private double[][] graph = new double[11][11];
    public int[] path = {0, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99 }; // new int[10];

    public void run(Point[] obstacle, Mat circles, int width, int height) {
    	// @wip
    }
    
    public void loadCoordinates(double[][] coordinates) {
        this.coordinates = coordinates;
    }

    public void createGraph() {
        for (int i = 0; i < 11; i++) {
            for (int j = 0; j < 11; j++) {
                if (i != j) {
                    graph[i][j] = calcDistance(coordinates[0][i], coordinates[1][i], coordinates[0][j],
                            coordinates[1][j]);
                }
            }
        }
    }

    private double calcDistance(double x1, double y1, double x2, double y2) {
        double tempValue = Math.sqrt(Math.pow(x1 - x2, 2) + Math.pow(y1 - y2, 2)); // Distance formlen
        return tempValue;
    }

    public int findClosest(int node) {
        int closestNode = 0;
        double length = 1000;

        for (int i = 0; i < 11; i++) {
            if (graph[node][i] < length) {
                closestNode = i;
                length = graph[node][i];
            }
        }
        return closestNode;
    }

    public void findClosestAll(int[] path) { // Run this function
        int node = 0;
        for (int i = 0; i < 10; i++) {
            node = findClosestNext(node);
            path[i+1] = node;
        }

    }

    public int findClosestNext(int node) {
        int closestNode = 0;
        double length = 1000;

        for (int i = 0; i < 11; i++) {
            if (!isInPath(i)) {
                if (crosses(node, i)) {
                    // Do stuff
                	
                }
                if (graph[node][i] < length) {
                    closestNode = i;
                    length = graph[node][i];
                }
            }
        }
        return closestNode;
    }

    public boolean isInPath(int node) {
        for (int i = 0; i < 10; i++) {
            if (path[i] == node) {
                return true;
            }
        }
        return false;
    }

    public boolean crosses(int node1, int node2) {
        slope pathToBall = new slope();
        findSlope(node1, node2, pathToBall); // find function for path between two balls
        slope obstacle1 = new slope(); // This should be in a constructor
        findSlope(12, 13, obstacle1); // find function for first obstacle section
        slope obstacle2 = new slope();
        findSlope(14, 15, obstacle2); // find function for second obstacle section

        double P1 = (obstacle1.b - pathToBall.b) / (pathToBall.a - obstacle1.a); // point of intersection X-axis
                                                                                 // (d-c)/(a-b)
        double P2 = (pathToBall.a * obstacle1.b - obstacle1.a * pathToBall.b) / (pathToBall.a - obstacle1.a); // point
                                                                                                              // of
                                                                                                              // intersection
                                                                                                              // Y-axis
                                                                                                              // (ad-bc)/(a-b)

        if ((coordinates[0][12] <= P1 && P1 <= coordinates[0][13])
                || (coordinates[0][12] >= P1 && P1 >= coordinates[0][13])) {
            if ((coordinates[1][12] <= P2 && P2 <= coordinates[1][13])
                    || (coordinates[1][12] >= P2 && P2 >= coordinates[1][13])) {
                if ((coordinates[0][node1] <= P1 && P1 <= coordinates[0][node2])
                        || (coordinates[0][node1] >= P1 && P1 >= coordinates[0][node2])) {
                    if ((coordinates[1][node1] <= P2 && P2 <= coordinates[1][node2])
                            || (coordinates[1][node1] >= P2 && P2 >= coordinates[1][node2])) {
                        return true;// They cross, do something!
                    }
                }
            }
        }

        return false;
    }

    public void findSlope(int node1, int node2, slope slope) { // Receives two points and a slope object, and writes the
                                                               // function for the line in the slope object
        slope.a = (coordinates[1][node1] - coordinates[1][node2]) / (coordinates[0][node1] - coordinates[0][node2]); // (y1-y2)/(x1-x2)

        slope.b = -slope.a * coordinates[0][node1] + coordinates[1][node1]; // y=ax+b
    }

    public class slope {
        double a;
        double b;
    }
}
