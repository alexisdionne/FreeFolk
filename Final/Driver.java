import lejos.robotics.SampleProvider;
import lejos.robotics.chassis.Chassis;
import lejos.robotics.chassis.Wheel;
import lejos.robotics.chassis.WheeledChassis;
import lejos.robotics.navigation.MovePilot;
import lejos.utility.Delay;
import lejos.hardware.Keys;
import lejos.hardware.Sound;
import lejos.hardware.ev3.LocalEV3;
import lejos.hardware.lcd.LCD;
import lejos.hardware.port.Port;
import lejos.hardware.motor.*;
import lejos.hardware.sensor.NXTUltrasonicSensor;

public class Driver{
	
	//					  y, x
	static int[] goal = {40, 30};
	static float[] obstacle_data = new float[3];
	int dataCounter = 0;
	
	// initialize ports
	static Port port4 = LocalEV3.get().getPort("S4");
	static Port portA = LocalEV3.get().getPort("A");  // right
	static Port portB = LocalEV3.get().getPort("B");
	static Port portD = LocalEV3.get().getPort("D");  // left
	static NXTUltrasonicSensor uSensor = new NXTUltrasonicSensor(port4);
	static EV3LargeRegulatedMotor motorRight = new EV3LargeRegulatedMotor(portA);
	static EV3LargeRegulatedMotor motorLeft = new EV3LargeRegulatedMotor(portD);
	static NXTRegulatedMotor motorB = new NXTRegulatedMotor(portB);

	static SampleProvider distSensor = uSensor.getDistanceMode();
	static float[] sampleDist = new float[distSensor.sampleSize()];
	static Keys buttons = LocalEV3.get().getKeys();
	
	static double theta = 0.0;
	static double deltaS = 0.0;
	static double x = 0.0;
	static double y = 0.0;
	static double deltaTheta = 0.0;

	static int counter = 0;
	static int position = 0;
	static boolean reverse = false;
	
	static int currentRightTacho, currentLeftTacho;
	static int prevRightTacho = 0, prevLeftTacho = 0;
	static int rightSpeed = 200, leftSpeed = 200;
	
	static double radius = 1.9;
	static double width = 17.5;
	static double wheelDistance = (3.14 * 2 * radius) / 360;


	public static void main(String[] args) {

		System.out.print("Hit a button!!");
		buttons.waitForAnyPress();
		int count = 0;
		theta = 0;
		deltaTheta = 0;
		deltaS = 0;
		LCD.clearDisplay();
		
		for(int x = 0; x < 3; x++) {
			// get data consistently for freshest readings
			distSensor.fetchSample(sampleDist, 0);
			obstacle_data[x] = sampleDist[0];
			if(x<2) {
				motorB.rotate(45);
			}
		}
		motorB.rotate(-90);
		Delay.msDelay(500);
		
		while (count < 1) {
			LCD.drawString("goal x:"+goal[1], 0, 6);
			LCD.drawString("goal y:"+goal[0], 0, 7);
			
			if(        Math.abs(x) >= (Math.abs(goal[0] - 4)) 
					&& Math.abs(x) <= (Math.abs(goal[0] + 4)) 
					&& Math.abs(y) >= (Math.abs(goal[1] - 4)) 
					&& Math.abs(y) <= (Math.abs(goal[1] + 4))) {
				motorRight.stop();
				motorLeft.stop();
				Sound.beepSequenceUp();
				count++;
				Delay.msDelay(3000);
			}
			else {
				currentLeftTacho = motorLeft.getTachoCount();
				currentRightTacho = motorRight.getTachoCount();

				localize(currentRightTacho, currentLeftTacho);
			}
		}

	}

	public static void localize(int rightTacho, int leftTacho) {
		double Sr = 0, Sl = 0;
		double goalTheta = 0;
		double kp = 100;
		double ke = 200;
		double avoidError = 0;
		
		// get data consistently for freshest readings
		distSensor.fetchSample(sampleDist, 0);
		obstacle_data[position] = sampleDist[0];
		System.out.println("position: "+position);
		System.out.println("sample: "+sampleDist[0]);
		if(position == 2) {
			reverse = true;
			motorB.rotate(-45);
			position--;
		}
		else if (position == 0){
			reverse = false;
			motorB.rotate(45);
			position++;
		}
		else {
			if(reverse) {
				motorB.rotate(-45);
				position--;
			}
			else {
				motorB.rotate(45);
				position++;
			}
		}
		
		// get goal direction
		// find distances traveled by wheels since last math
		Sr = (rightTacho - prevRightTacho)*wheelDistance;//maybe get tacho count
		Sl = (leftTacho - prevLeftTacho)*wheelDistance;
		prevRightTacho = rightTacho;
		prevLeftTacho = leftTacho;
		
		double deltaS = (Sr+Sl)/2;
		double deltaTheta = (Sr-Sl)/width;
		
		double deltaX = deltaS*Math.cos(theta+(deltaTheta/2));
		double deltaY = deltaS*Math.sin(theta+(deltaTheta/2));
		
		theta += deltaTheta;
		x += deltaX;
		y += deltaY;
		
		LCD.drawString("X: "+y, 0, 3);
		LCD.drawString("Y: "+x, 0, 4);
		System.out.println("X: "+y);
		System.out.println("Y: "+x);
		
		goalTheta = Math.atan2(goal[1] - y, goal[0] - x);

		double error = goalTheta - theta;
		if((x <= goal[0] + 20 && x >= goal[0] - 20) && (y <= goal[1] + 20 && y >= goal[1] - 20)) {
			avoidError = 0;
			LCD.drawString("          ", 0, 5);
		}
		else {
			//avoidError = 0;
			avoidError = avoid();
			LCD.drawString("AVOID: "+avoidError, 0, 5);
		}
		
		int currRight = (int) (rightSpeed + (kp*error+ke*avoidError));
		int currLeft = (int) (leftSpeed - (kp*error+ke*avoidError));
		System.out.println("speedRight:"+currRight+"\nspeedLEft:"+currLeft);
		motorRight.setSpeed(currRight);
		motorLeft.setSpeed(currLeft);
		
		if(currRight > 0) {
			motorRight.forward();
		}
		else {
			motorRight.setSpeed((int) Math.abs(currRight));
			motorRight.backward();
		}
		if(currLeft > 0) {
			motorLeft.forward();
		}
		else {
			motorLeft.setSpeed((int) Math.abs(currLeft));
			motorLeft.backward();
		}
		counter++;
	}
	
	public static double avoid() {
		double avoidError = 0;
		/*if(obstacle_data[4] < .45 && obstacle_data[4] != 0) {
			avoidError += (obstacle_data[4] - .45);
			System.out.println("4: "+avoidError);
		}*/
		if(obstacle_data[2] < .60 && obstacle_data[2] != 0) {
			avoidError += (obstacle_data[2] - .60);
			System.out.println("2: "+avoidError);
		}
		if(obstacle_data[1] < .80 && obstacle_data[1] != 0) {
			avoidError += (1 - obstacle_data[1]);
			System.out.println("1: "+avoidError);
		}
		if(obstacle_data[0] < .60 && obstacle_data[0] != 0) {
			avoidError += (.60 - obstacle_data[0]);
			System.out.println("0: "+avoidError);
		}
		/*if(obstacle_data[0] < .45 && obstacle_data[0] != 0) {
			avoidError += (.45 - obstacle_data[0]);
			System.out.println("0: "+avoidError);
		}*/
		System.out.println("Error: "+avoidError);
		return avoidError;
	}
}
