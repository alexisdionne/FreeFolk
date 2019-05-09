import lejos.robotics.SampleProvider;
import lejos.utility.Delay;
import lejos.hardware.Keys;
import lejos.hardware.Sound;
import lejos.hardware.ev3.LocalEV3;
import lejos.hardware.lcd.LCD;
import lejos.hardware.port.Port;
import lejos.hardware.motor.*;
import lejos.hardware.sensor.NXTUltrasonicSensor;

// CMPT 412 Robotics
// Final Project - Potential Field Path Finding and Localization
// Created by: Alexis Dionne, Nic Mays, and Jack Hueber

public class Driver{
	
	static int[] goal = {600, -250}; // it actually goes {y, x} when inserting coordinates
	static float[] obstacle_data = new float[3];
	
	// initialize ports
	static Port port4 = LocalEV3.get().getPort("S4");
	static Port portA = LocalEV3.get().getPort("A");  // right
	static Port portB = LocalEV3.get().getPort("B");
	static Port portD = LocalEV3.get().getPort("D");  // left
	static NXTUltrasonicSensor uSensor = new NXTUltrasonicSensor(port4);
	static EV3LargeRegulatedMotor motorRight = new EV3LargeRegulatedMotor(portA);
	static EV3LargeRegulatedMotor motorLeft = new EV3LargeRegulatedMotor(portD);
	static NXTRegulatedMotor motorB = new NXTRegulatedMotor(portB);

	//initialize sensor
	static SampleProvider distSensor = uSensor.getDistanceMode();
	static float[] sampleDist = new float[distSensor.sampleSize()];
	static Keys buttons = LocalEV3.get().getKeys();
	
	// Math variables are global
	static double theta = 0.0;
	static double deltaS = 0.0;
	static double x = 0.0;
	static double y = 0.0;
	static double deltaTheta = 0.0;

	// variables to keep track of the sensor position
	static int counter = 0;
	static int position = 0;
	static boolean reverse = false;
	
	// wheel variables
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
			// grab data around robot before moving out to watch for close obstacles
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
				// within error range of goal
				motorRight.stop();
				motorLeft.stop();
				Sound.beepSequenceUp();
				count++;
				Delay.msDelay(3000);
			}
			else if(Math.abs(x) > Math.abs(goal[0]) && Math.abs(y) > Math.abs(goal[1])) {
				// oops we passed it so stop instead of flailing
				motorRight.stop();
				motorLeft.stop();
				Sound.beepSequence();
				count++;
				Delay.msDelay(3000);
			}
			else if (buttons.readButtons() == Keys.ID_ENTER) {
				// button was pressed to end this run
				count++;
			}
			else {
				// get newest movement measurements and localize
				currentLeftTacho = motorLeft.getTachoCount();
				currentRightTacho = motorRight.getTachoCount();

				localize(currentRightTacho, currentLeftTacho);
			}
		}

	}

	public static void localize(int rightTacho, int leftTacho) {
		// find the shortest path to a goal location without hitting any objects
		
		double Sr = 0, Sl = 0;
		double goalTheta = 0;
		double kp = 100; // theta constant
		double ke = 250; // avoid error constant
		double avoidError = 0;
		
		// get data every time for freshest readings
		distSensor.fetchSample(sampleDist, 0);
		obstacle_data[position] = sampleDist[0];

		// update ultrasonic sensor head direction
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
		// find distances traveled by wheels since last calculation
		Sr = (rightTacho - prevRightTacho)*wheelDistance;
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
		
		// goal theta is the direction to want to turn to re-orient with the end location
		goalTheta = Math.atan2(goal[1] - y, goal[0] - x);
		double error = goalTheta - theta;
		
		if((x <= goal[0] + 20 && x >= goal[0] - 20) && (y <= goal[1] + 20 && y >= goal[1] - 20)) {
			// if we're close to the end goal, go for it and don't check for other obstacles
			avoidError = 0;
			LCD.drawString("          ", 0, 5);
		}
		else {
			avoidError = avoid();
			LCD.drawString("AVOID: "+avoidError, 0, 5);
		}
		
		// calculate speeds to adjust to when combining values of goal error and avoid error
		// left values are negative
		int currRight = (int) (rightSpeed + (kp*error+ke*avoidError));
		int currLeft = (int) (leftSpeed - (kp*error+ke*avoidError));

		motorRight.setSpeed(currRight);
		motorLeft.setSpeed(currLeft);
		
		if(currRight > 0) {
			motorRight.forward();
		}
		if(currLeft > 0) {
			motorLeft.forward();
		}
		counter++;
	}
	
	public static double avoid() {
		// avoid is calculated by taking the data (if it matters) and adding it all 
		// to make a solid avoid error value. Left obstacles result in negative values
		double avoidError = 0;
		
		if(obstacle_data[2] < .60 && obstacle_data[2] != 0) {
			avoidError += (obstacle_data[2] - .60);
		}
		if(obstacle_data[1] < .80 && obstacle_data[1] != 0) {
			// robot favors avoiding left
			avoidError += (1 - obstacle_data[1]);
		}
		if(obstacle_data[0] < .60 && obstacle_data[0] != 0) {
			avoidError += (.60 - obstacle_data[0]);
		}
		return avoidError;
	}
}
