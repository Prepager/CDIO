package movement;

import java.util.concurrent.TimeUnit;

import lejos.hardware.lcd.LCD;
import lejos.hardware.motor.Motor;
import lejos.utility.Delay;
import lejos.hardware.motor.JavaMotorRegulator;

public class MovementTest {
			
	public void move(int speed)
	{
		Motor.A.setSpeed(speed);
		Motor.D.setSpeed(speed);
		if(speed > 0 ) {
		Motor.A.backward();
		Motor.D.backward();
		} else if(speed < 0) {
			Motor.D.forward();
			Motor.A.forward();
		} else {
			Motor.A.stop();
			Motor.D.stop();
		}
	}
	
	public void turn(int deg) throws InterruptedException{
		/*Motor.A.setSpeed(0);
		Motor.D.setSpeed(0);
		Motor.A.stop();
		Motor.D.stop();*/
		Motor.A.setSpeed(300);
		Motor.D.setSpeed(300);
		if(deg > 0) {
			Motor.A.backward();
			Motor.D.forward();
		}else if(deg < 0) {
			Motor.A.forward();
			Motor.D.backward();
		}
		//Delay.msDelay(Math.abs(deg)*13);
		TimeUnit.MICROSECONDS.sleep(deg*13780);
		/*Motor.A.setSpeed(0);
		Motor.D.setSpeed(0);
		Motor.A.stop();
		Motor.D.stop();*/
		Motor.A.backward();
		Motor.D.backward();
	}
	

	public void moveForward() 
	{
		Motor.A.setSpeed(500);
		Motor.D.setSpeed(500);
		Motor.D.backward();
		Motor.A.backward();
	}
	
	public void moveBackwards() 
	{
		Motor.A.setSpeed(500);
		Motor.D.setSpeed(500);
		Motor.D.forward();
		Motor.A.forward();
	}
	
	public void turnRight()
	{
		Motor.A.setSpeed(0);
		Motor.D.setSpeed(0);
		Motor.A.stop();
		Motor.D.stop();
		Motor.A.setSpeed(300);
		Motor.D.setSpeed(300);
		Motor.A.backward();
		Motor.D.forward();
	}
	
	public void turnLeft()
	{
		Motor.A.setSpeed(0);
		Motor.D.setSpeed(0);
		Motor.A.stop();
		Motor.D.stop();
		Motor.A.setSpeed(300);
		Motor.D.setSpeed(300);
		Motor.A.forward();
		Motor.D.backward();
	}
	
	public void moveForwardTime (int delay) 
	{
		Motor.A.setSpeed(500);
		Motor.D.setSpeed(500);
		Motor.A.backward();
		Motor.D.backward();
		Delay.msDelay(delay*500);
		Motor.A.setSpeed(0);
		Motor.D.setSpeed(0);
	}
	
	public void setMotorSpeed(int speed) 
	{
		   Motor.D.setSpeed(speed);
		   Motor.A.setSpeed(speed);
	}
	
	public void turnDegreeRight(int degree) 
	{
		Motor.A.setSpeed(0);
		Motor.D.setSpeed(0);
		Motor.A.stop();
		Motor.D.stop();
		Motor.A.setSpeed(300);
		Motor.D.setSpeed(300);
		Motor.A.backward();
		Motor.D.forward();
		Delay.msDelay(degree*13);
		Motor.A.setSpeed(0);
		Motor.D.setSpeed(0);
	}
	
	public void turnDegreeLeft(int degree) 
	{
		Motor.A.setSpeed(0);
		Motor.D.setSpeed(0);
		Motor.A.stop();
		Motor.D.stop();
		Motor.A.setSpeed(300);
		Motor.D.setSpeed(300);
		Motor.D.backward();
		Motor.A.forward();
		Delay.msDelay(degree*13);
		Motor.A.setSpeed(0);
		Motor.D.setSpeed(0);
//		Motor.A.stop();
//		Motor.D.stop();
	}
	
	public void stopAll()
	{
		Motor.A.setSpeed(0);
		Motor.B.setSpeed(0);
		Motor.C.setSpeed(0);
		Motor.D.setSpeed(0);
		Motor.A.stop();
		Motor.B.stop();
		Motor.C.stop();
		Motor.D.stop();
	}
	
	public void stopMovement()
	{
		Motor.A.setSpeed(0);
		Motor.D.setSpeed(0);
		Motor.A.stop();
		Motor.D.stop();
	}
	
	public void pickUp() {
		Motor.B.setSpeed(150);
		Motor.C.setSpeed(400);
		Motor.B.forward();
		Motor.C.forward();
	}
	
	public void release(){
		for (int i=0; i<10; i++) {
			Motor.B.setSpeed(400);
			Motor.C.setSpeed(400);
			Motor.B.backward();
			Motor.C.backward();
		}
	}
	
	public void setup()
	{
		Motor.A.setAcceleration(3000);
		Motor.B.setAcceleration(3000);
		Motor.C.setAcceleration(3000);
		Motor.D.setAcceleration(3000);
	}
	
//	public void turnTo(int rotate) {
//		Motor.A.rotateTo(rotate);
//		int angle = Motor.A.getTachoCount();
//		LCD.drawInt(angle,0,0);
//	}
}
