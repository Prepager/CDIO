package movement;

import java.util.concurrent.TimeUnit;

import lejos.hardware.lcd.LCD;
import lejos.hardware.motor.Motor;
import lejos.hardware.motor.NXTRegulatedMotor;
import lejos.utility.Delay;
import lejos.hardware.motor.JavaMotorRegulator;

public class Movement {
	// Renaming motor name(s)
	public NXTRegulatedMotor left = Motor.A;
	public NXTRegulatedMotor right = Motor.D;
	public NXTRegulatedMotor pickUp = Motor.B;
	public NXTRegulatedMotor front = Motor.C;

	public void move(int speed) // Setting speed of vehicle
	{
		left.setSpeed(speed);
		right.setSpeed(speed);
		if (speed > 0) { // Vehicle forward
			left.backward();
			right.backward();
		} else if (speed < 0) { // Vehicle backwards
			right.forward();
			left.forward();
		} else { // Vehicle speed stop
			left.stop();
			right.stop();
		}
	}

	public void turn(int deg, int speed) throws InterruptedException { // Turns the vehicle
		left.setSpeed(speed);
		right.setSpeed(speed);
		if (deg > 0) { // Turn vehicle right
			left.backward();
			right.forward();
		} else if (deg < 0) { // Turn vehicle left
			left.forward();
			right.backward();
		}
	}

	public void collect(int pickUpSpeed, int frontSpeed) { // Collects/empty balls
		pickUp.setSpeed(pickUpSpeed);
		front.setSpeed(frontSpeed);
		if (pickUpSpeed > 0) { // Collects balls to vehicle
			pickUp.forward();
			front.forward();
		} else if (pickUpSpeed < 0) { // Empty balls from vehicle
			pickUp.backward();
			front.backward();
		} else { // Stop collecting balls on vehicle
			pickUp.stop();
			front.stop();
		}
	}

	public boolean isStalled() { // Checks if pickUp is stalled
		return pickUp.isStalled();
	}
}
