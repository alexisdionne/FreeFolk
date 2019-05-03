import lejos.robotics.SampleProvider;
import lejos.robotics.chassis.Chassis;
import lejos.robotics.chassis.Wheel;
import lejos.robotics.chassis.WheeledChassis;
import lejos.robotics.navigation.MovePilot;
import lejos.hardware.Keys;
import lejos.hardware.ev3.LocalEV3;
import lejos.hardware.lcd.LCD;
import lejos.hardware.port.Port;
import lejos.hardware.motor.*;
import lejos.hardware.sensor.NXTUltrasonicSensor;

public class Driver{
	
	double[] pose = {0.0, 0.0, 1.5708}; // [x, y, theta(radians)]
	double[] goal = {100.0, 100.0};
	double circumference = 3.5*3.14;//maybe 3.5
	
	float[] obstacle_data = new float[3];
	int dataCounter = 0;
	
	//SampleProvider distLeft;
	//SampleProvider distRight;
	//float[] sampleDistLeft;
	//float[] sampleDistRight;
	SampleProvider distSensor;
	float[] sampleDist;
	
	//NXTUltrasonicSensor uSensorLeft;
	//NXTUltrasonicSensor uSensorRight;
	NXTUltrasonicSensor uSensor;
	EV3LargeRegulatedMotor motorA;
	NXTRegulatedMotor motorB;
	EV3LargeRegulatedMotor motorD;
	static Keys buttons = LocalEV3.get().getKeys();
	
	Chassis chassis;
	MovePilot pilot;
	
	public Driver() {
		
		// initialize ports
		Port portS1 = LocalEV3.get().getPort("S1");
		Port portS4 = LocalEV3.get().getPort("S4");
		Port portA = LocalEV3.get().getPort("A");  // right
		Port portB = LocalEV3.get().getPort("B");
		Port portD = LocalEV3.get().getPort("D");  // left

		// assign sensors to ports
		//uSensorLeft = new NXTUltrasonicSensor(portS4);
		//uSensorRight = new NXTUltrasonicSensor(portS1);
		uSensor = new NXTUltrasonicSensor(portS4);
		
		// initialize motors and pilot
		motorA = new EV3LargeRegulatedMotor(portA);
		motorD = new EV3LargeRegulatedMotor(portD);
		motorB = new NXTRegulatedMotor(portB);
		//motorB.rotateTo(90);
		motorA.setSpeed(200);
		motorD.setSpeed(200);
		motorA.forward();
		motorD.forward();
		Wheel wheelA = WheeledChassis.modelWheel(motorA, 43.2).offset(-72);
		Wheel wheelD = WheeledChassis.modelWheel(motorD, 43.2).offset(72);
		chassis = new WheeledChassis(new Wheel[]{wheelA, wheelD}, WheeledChassis.TYPE_DIFFERENTIAL);
		pilot = new MovePilot(chassis);
		
		//distLeft = uSensorLeft.getDistanceMode();
		//sampleDistLeft = new float[distLeft.sampleSize()];
		//distRight = uSensorRight.getDistanceMode();
		//sampleDistRight = new float[distRight.sampleSize()];
		distSensor = uSensor.getDistanceMode();
		sampleDist = new float[distSensor.sampleSize()];
		
		
		localize();
	}

	public static void main(String[] args) {

		System.out.print("Hit a button!!");
		buttons.waitForAnyPress();
		LCD.clearDisplay();

		new Driver();
	}

	public void localize() {
		boolean end = false;
		int counter = 0;
		int position = 0;
		double Sr = 0, Sl = 0, lastSrTach = 0, lastSlTach = 0;
		double goalTheta = 0;
		while(end == false) {
			// loop through algorithm until a button is pressed
			
			if(counter % 20 == 0) {
				// gather fresh data
				//distLeft.fetchSample(sampleDistLeft, 0);
				//distRight.fetchSample(sampleDistRight, 0);
				
				//obstacle_data[0] = sampleDistLeft[0];
				//obstacle_data[1] = sampleDistRight[0];
				
				distSensor.fetchSample(sampleDist, 0);
				obstacle_data[position] = sampleDist[0];
				//System.out.println("pos "+position+" "+sampleDist[0]);
				
				if(position == 2) {
					motorB.rotate(-180);
					position = 0;
				}
				else {
					motorB.rotate(90);
					position++;
				}

				
			}
			// get goal direction
			// find distances traveled by wheels since last math
			Sr = circumference*((motorA.getTachoCount()-lastSrTach)/360);//maybe get tacho count
			Sl = circumference*((motorD.getTachoCount()-lastSlTach)/360);
			lastSrTach = motorA.getTachoCount();
			lastSlTach = motorD.getTachoCount();
			System.out.println(Sr+" "+Sl);
			
			double deltaS = (Sr+Sl)/2;
			double deltaTheta = (Sr-Sl)/70;
			
			double deltaX = deltaS*Math.cos(pose[2]+(deltaTheta/2));
			double deltaY = deltaS*Math.sin(pose[2]+(deltaTheta/2));
			goalTheta = Math.atan2(goal[1]-deltaY, goal[0]-deltaX);
			
			//if(Math.toDegrees(goalTheta) {
			int speedA = (int) Math.sqrt(Math.pow((goal[1]-deltaX), 2) + Math.pow((deltaY-goal[1]), 2));
			int speedD = (int) Math.sqrt(Math.pow((goal[1]-deltaX), 2) + Math.pow((deltaY-goal[1]), 2));

			// get objects in the path to push away from
			
			//System.out.println("x:"+deltaX+" y:"+deltaY+" theta"+goalTheta);
			//System.out.println("speed right:"+speedA+" left: "+speedD);
	
			motorA.setSpeed(speedA);
			motorD.setSpeed(speedD);
			
			if(buttons.readButtons() != 0) {
				end = true;
			}
			counter++;
		}
	}
}
