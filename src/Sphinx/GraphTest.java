package sphinx;

public class GraphTest extends Graph {

    public static void main(String args[]) {   
    	
    	GraphTest graph = new GraphTest();
    	
        double[][] coordins = { 
        		{ 1, 2.2, 3, 5.1, 5, 6, 7, 8, 1.3, 13, 1.05, 4.1, 4.3, 4.3, 3.8, 4.8 },
                { 1, 7, 5, 1.2, 4, 5.3, 6, 8, 6.8, 1.1, 1.05, 2.2, 4.8, 3.8, 4.3, 4.3 }, };

        graph.loadCoordinates(coordins);
        graph.createGraph();
        graph.findClosestAll(graph.path);

        for (int i = 0; i < 11; i++) {
            // java.util.print.out
        	System.out.print("\n" + graph.path[i]);
           // System.out.print("\n" + graph.coordinates[graph.path[i]][0]);
        }
    }
}
