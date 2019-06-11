package movement;

import java.util.concurrent.TimeUnit;

import lejos.hardware.lcd.LCD;
import lejos.hardware.motor.Motor;
import lejos.hardware.motor.NXTRegulatedMotor;
import lejos.utility.Delay;
import lejos.hardware.motor.JavaMotorRegulator;

public class MovementTest {
	
	public NXTRegulatedMotor left = Motor.A;
	public NXTRegulatedMotor right = Motor.D;
	public NXTRegulatedMotor pickUp = Motor.B;
	public NXTRegulatedMotor front = Motor.C;
			
	public void move(int speed) //setting speed
	{
		left.setSpeed(speed);
		right.setSpeed(speed);
		if(speed > 0 ) { //forward
		left.backward();
		right.backward();
		} else if(speed < 0) { //backwards
			right.forward();
			left.forward();
		} else { //stop
			left.stop();
			right.stop();
		}
	}
	
	public void turn(int deg, int speed) throws InterruptedException{
		left.setSpeed(speed);
		right.setSpeed(speed);
		if(deg > 0) { //right turn
			left.backward();
			right.forward();
		}else if(deg < 0) { //left turn
			left.forward();
			right.backward();
		}
	}
		
	public void pickUp(int pickUpSpeed, int frontSpeed) {
		pickUp.setSpeed(pickUpSpeed);
		front.setSpeed(frontSpeed);
		pickUp.forward();
		front.forward();
	}
	
	public void release(int pickUpSpeed, int frontSpeed){
		for (int i=0; i<10; i++) {
			pickUp.setSpeed(pickUpSpeed);
			front.setSpeed(frontSpeed);
			pickUp.backward();
			front.backward();
		}
	}
}
