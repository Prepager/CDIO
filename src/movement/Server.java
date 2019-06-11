package movement;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


import lejos.hardware.lcd.LCD;
import lejos.hardware.motor.Motor;
import lejos.utility.Delay;
import lejos.hardware.motor.JavaMotorRegulator;

/**
 * A server program which accepts requests from clients to capitalize strings. When
 * a client connects, a new thread is started to handle it. Receiving client data,
 * capitalizing it, and sending the response back is all done on the thread, allowing
 * much greater throughput because more clients can be handled concurrently.
 */
public class Server {

    /**
     * Runs the server. When a client connects, the server spawns a new thread to do
     * the servicing and immediately returns to listening. The application limits the
     * number of threads via a thread pool (otherwise millions of clients could cause
     * the server to run out of resources by allocating too many threads).
     */
    public static void main(String[] args) throws Exception {
        try (ServerSocket listener = new ServerSocket(59898)) {
            System.out.println("Server is running");
            ExecutorService pool = Executors.newFixedThreadPool(20);
            while (true) {
                pool.execute(new Capitalizer(listener.accept()));
            }
        }
    }

    private static class Capitalizer implements Runnable {
        private Socket socket;

        Capitalizer(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
        	MovementTest move = new MovementTest();
            //System.out.println("Connected: " + socket);
        	System.out.println("Client connected");
        	String str = "";
            try {
                Scanner in = new Scanner(socket.getInputStream());
                PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                while (!str.equals("exit")) {
                    str = in.nextLine();
                    out.println(str.toUpperCase());
                    if(str != "") {
                    String[] command = str.split(" ");
                    switch(command[0]) {
	                	case "move":
	                		move.move(Integer.parseInt(command[1]));
	                		break;
	                	case "turn":
	                		if(Math.abs(Integer.parseInt(command[1]))>10) {
	                			move.turn(Integer.parseInt(command[1]));
	                		}
	                		break;
	                	default:
	                		System.out.println("error command: "+ command[0]); //command does not excist
	                		break;
	                    }
                    }
                }
                in.close();
                out.close();
            } catch (Exception e) {
                System.out.println("Error:" + socket);
            } finally {
                try { socket.close(); } catch (IOException e) {}
                System.out.println("Closed: " + socket);
            }
        }
    }
}