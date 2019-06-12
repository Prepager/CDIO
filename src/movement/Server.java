package movement;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import lejos.hardware.Sound;
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
        	Movement move = new Movement();
        	Sound.beep();
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
	                	case "move": //moves the vehicle foward, backwards and stop
	                		move.move(Integer.parseInt(command[1]));
	                		break;
	                	case "turn": //turns the vehicle left or right
	                		move.turn(Integer.parseInt(command[1]), Integer.parseInt(command[2]));
	                		break;
	                	case "stop": //stops the vehicle
	                		move.move(0);
	                	case "collect": //turns the pickup mekanism on
	                		move.collect(Integer.parseInt(command[1]), Integer.parseInt(command[2]));
	                		break;
	                	default: //stops movement of the car
	                		move.move(0);
	                		System.out.println("error command: "+ command[0]); //command does not excist
	                		break;
	                    }
                    }
                    if(move.isStalled()) {out.println("stalled");} //checks of motor is stalled
                }
                in.close();
                out.close();
            } catch (Exception e) {
            	System.out.println("Client disconnected");
                //System.out.println("Error:" + socket);
            } finally {
                try { socket.close(); } catch (IOException e) {
                	System.out.println("Socket didn't close properly");
                	//System.out.println("Closed: " + socket);
                }
                move.move(0);
                move.collect(0,0);
            }
        }
    }
}